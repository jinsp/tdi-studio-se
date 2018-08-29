/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.sleep;
import static java.util.Collections.emptyEnumeration;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.sdk.component.studio.lang.LocalLock;
import org.talend.sdk.component.studio.lang.StringPropertiesTokenizer;
import org.talend.sdk.component.studio.logging.JULToOsgiHandler;
import org.talend.sdk.component.studio.mvn.Mvn;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class ProcessManager implements AutoCloseable {

    private final String groupId;

    private final Function<String, File> mvnResolver;

    private int port;

    private Thread hook;

    private volatile AutoCloseable instance;

    private volatile CountDownLatch ready;

    private URLClassLoader loader;

    private Thread serverThread;

    public ProcessManager(final String groupId, final Function<String, File> resolver) {
        this.groupId = groupId;
        this.mvnResolver = resolver;
    }

    public void waitForServer(final Runnable healthcheck) {
        // useful for the client, to ensure we are ready
        final int steps = 250;
        for (int i = 0; i < TimeUnit.MINUTES.toMillis(10) / steps; i++) {
            try {
                if (ready.await(steps, TimeUnit.MILLISECONDS)) {
                    healthcheck.run();
                    return;
                }
                if (i > 0 && i % 12 == 0) {
                    final Thread starterThread = serverThread;
                    if (starterThread == null || !starterThread.isAlive()) {
                        try {
                            System.err.println("Component server didn\'t start properly, please check the log before");
                            Lookups.configuration().disable();
                            break;
                        } catch (final IllegalThreadStateException itse) {
                        }
                    }
                    // expected
                    System.out.println("Component server not yet ready, will wait again"); // no logger!
                }
                sleep(steps);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (final RuntimeException re) {
                try {
                    sleep(500); // wait and retry, the healthcheck failed
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    synchronized public String reload() {
        final Thread thread = Thread.currentThread();
        final ClassLoader old = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            reloadProperties();
            updateProperties();
            final Class<?> containerClazz = loader.loadClass("org.talend.sdk.component.container.Container");
            final Class<?> actionsClazz = loader.loadClass("org.talend.sdk.component.container.ContainerManager$Actions");
            final Class<?> containerManagerClazz = loader.loadClass("org.talend.sdk.component.runtime.manager.ComponentManager");

            final Object manager = containerManagerClazz.getMethod("instance").invoke(null);
            final Field containerField = containerManagerClazz.getDeclaredField("container");
            if (!containerField.isAccessible()) {
                containerField.setAccessible(true);
            }

            final Object container = containerField.get(manager);
            final Method findAll = container.getClass().getMethod("findAll");
            final Method getData = containerClazz.getMethod("get", Class.class);

            final Method reload = actionsClazz.getMethod("reload");
            final Method getId = containerClazz.getMethod("getId");
            final Collection<?> containers = new ArrayList<>(Collection.class.cast(findAll.invoke(container)));
            return containers.stream().map(it -> {
                try {
                    final Object actions = getData.invoke(it, actionsClazz);
                    if (actions != null) {
                        reload.invoke(actions);
                    }
                    return String.valueOf(getId.invoke(it)) + ": OK";
                } catch (final IllegalAccessException e) {
                    throw new IllegalStateException(e);
                } catch (final InvocationTargetException e) {
                    try {
                        return String.valueOf(getId.invoke(it)) + ": " + e.getMessage();
                    } catch (final IllegalAccessException e1) {
                        throw new IllegalStateException(e1);
                    } catch (final InvocationTargetException e1) {
                        throw new IllegalStateException(e1.getTargetException());
                    }
                }
            }).collect(joining("\n"));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        } finally {
            thread.setContextClassLoader(old);
        }
    }

    @Override
    public synchronized void close() {
        if (hook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (final IllegalStateException itse) {
                // no-op
            }
            // shutting down already
            hook = null;
        }
        ofNullable(instance).ifPresent(i -> {
            try {
                i.close();
            } catch (final Exception e) {
                // no-op
            } finally {
                // no-op
                instance = null;
            }
        });
        try {
            if (serverThread != null) {
                if (serverThread.isAlive()) {
                    try {
                        serverThread.join(TimeUnit.SECONDS.toMillis(10));
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                    }
                    serverThread.interrupt();
                    try {
                        serverThread.join(TimeUnit.MINUTES.toMillis(1));
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                    }
                }
                serverThread = null;
            }
        } finally {
            if (loader != null) {
                try {
                    loader.close();
                } catch (final IOException e) {
                    // no-op
                } finally {
                    // no-op: not important at that time
                    loader = null;
                }
            }
        }
    }

    public synchronized void start() {
        final Collection<URL> paths;
        try {
            paths = createClasspath();
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
        final List<String> arguments = new StringPropertiesTokenizer(System.getProperty("component.java.arguments", "")).tokens();
        updateProperties();
        loader = new URLClassLoader(paths.toArray(new URL[paths.size()]), rootLoader()) {

            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                if ("META-INF/services/org.apache.juli.logging.Log".equals(name)) {
                    return emptyEnumeration();
                }
                if ("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat".equals(name)) {
                    return emptyEnumeration();
                }
                return super.getResources(name);
            }
        };
        final Lock lock = new LocalLock(ofNullable(System.getProperty("component.lock.location")).map(File::new)
                .orElseGet(() -> new File(System.getProperty("user.home"), ".talend/locks/" + GAV.INSTANCE.getArtifactId() + ".lock")), null);
        lock.lock();
        port = newPort();
        if (!arguments.contains("--http")) {
            arguments.add(0, Integer.toString(port));
            arguments.add(0, "--http");
        }
        if (!arguments.contains("--scanning-exclude")) {
            arguments.add(0,
                    "org.talend,org.apache,component-api,component-spi,component-runtime-manager,component-runtime-impl,component-runtime-design,zipkin,workbench,tomcat-,system-rules,registry,preference,org.jacoco,lz4,jobs,jface,help,draw2d,contentytpe,");
            arguments.add(0, "--scanning-exclude");
        }
        // being embedded and not in app loader we can't use that
        arguments.add(0, "false");
        arguments.add(0, "--log4j2-jul-bridge");
        arguments.add(0, "false");
        arguments.add(0, "--logging-global-setup");
        arguments.add(0, "false");
        arguments.add(0, "--use-shutdown-hook");
        ready = new CountDownLatch(1);
        serverThread = new Thread() {

            // server thread
            {
                setName(ProcessManager.class.getName() + "-server");
                setContextClassLoader(loader);
            }

            @Override
            public void run() {
                System.setProperty("org.apache.tomcat.Logger", "jul");
                configureJUL(); // do it in the right classloader context
                try {
                    final Class<?> cliClass = loader.loadClass("org.talend.sdk.component.server.cli.EnhancedCli");
                    instance = AutoCloseable.class.cast(cliClass.getConstructor(String[].class)
                            .newInstance(new Object[] { arguments.toArray(new String[arguments.size()]) }));
                    Runnable.class.cast(instance).run();
                } catch (final InvocationTargetException ie) {
                    if (!InterruptedException.class.isInstance(ie.getTargetException())) {
                        throw new IllegalStateException(ie.getTargetException());
                    }
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        hook = new Thread() {

            // in case of a ctrl+C/kill+X on the studio
            {
                setName(getClass().getName() + "-shutdown-hook");
            }

            @Override
            public void run() {
                lock.unlock();
                close();
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        serverThread.start();
        // just a healthcheck to be able to ensure the server is up when starting to use
        // it (ou client)
        // opened :)
        // try again
        new Thread() {

            {
                setName(getClass().getName() + "-readiness-checker");
            }

            @Override
            public void run() {
                final long end = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15);
                while (end - System.currentTimeMillis() >= 0) {
                    final Thread serverThread = ProcessManager.this.serverThread;
                    if (serverThread == null || !serverThread.isAlive()) {
                        lock.unlock();
                        throw new IllegalStateException("Component server startup failed");
                    }
                    try (Socket ignored = new Socket("localhost", port)) {
                        new URL("http://localhost:" + port + "/api/v1/environment").openStream().close();
                        lock.unlock();
                        ready.countDown();
                        return;
                    } catch (final IOException e) {
                        try {
                            Thread.sleep(500);
                        } catch (final InterruptedException e1) {
                            Thread.interrupted();
                            break;
                        }
                    }
                    lock.unlock();
                }
                throw new IllegalStateException("Component server didn\'t start in 15mn!");
            }
        }.start();
    }

    private void reloadProperties() {
        try {
            System.setProperty(TaCoKitConst.PROP_COMPONENT, TaCoKitUtil.getInstalledComponentsString(new NullProgressMonitor()));
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void updateProperties() {
        String m2Repo = System.getProperty("component.java.m2");
        if (m2Repo == null) {
            m2Repo = MavenPlugin.getMaven().getLocalRepositoryPath();
        }
        final String components = System.getProperty(TaCoKitConst.PROP_COMPONENT);
        final String registry = System.getProperty(TaCoKitConst.PROP_REGISTRY);
        if (m2Repo != null) {
            System.setProperty("talend.component.server.maven.repository", m2Repo);
        }
        if (components != null) {
            System.setProperty("talend.component.server.component.coordinates", components);
        }
        if (registry != null) {
            System.setProperty("talend.component.server.component.registry", registry);
        }
        // local instance, no need of any security
        System.setProperty("talend.component.server.security.connection.handler", "securityNoopHandler");
        System.setProperty("talend.component.server.security.command.handler", "securityNoopHandler");
    }

    public int getPort() {
        return this.port;
    }

    private void configureJUL() {
        if (!Boolean.getBoolean("component.server.jul.skip")) {
            final Logger global = Logger.getLogger("");
            Stream.of(global.getHandlers()).forEach(global::removeHandler);
            global.addHandler(new JULToOsgiHandler());
        } else if (Boolean.getBoolean("component.server.jul.appendOSGiHandler")) {
            Logger.getLogger("").addHandler(new JULToOsgiHandler());
        }
        if (Boolean.getBoolean("component.server.jul.forceConsole")) {
            final ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(handler);
        }
    }

    private ClassLoader rootLoader() {
        return new ClassLoader(getSystemClassLoader()) {

            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (name == null) {
                    throw new ClassNotFoundException();
                }
                if (name.startsWith("org.")) {
                    final String sub = name.substring("org.".length());
                    if (sub.startsWith("apache.") || sub.startsWith("talend.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("slf4j")) {
                        throw new ClassNotFoundException(name);
                    }
                }
                if (name.startsWith("brave.")) {
                    throw new ClassNotFoundException(name);
                }
                if (name.startsWith("javax.")) {
                    final String sub = name.substring("javax.".length());
                    if (sub.startsWith("annotation.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("inject.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("interceptor.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("ws.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("enterprise.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("decorator.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("json.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("servlet.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("websocket.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (sub.startsWith("security.auth.message.")) {
                        throw new ClassNotFoundException(name);
                    }
                }
                if (name.startsWith("net.jpountz.")) {
                    throw new ClassNotFoundException(name);
                }
                if (name.startsWith("zipkin2.")) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }

            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                if (name.startsWith("log4j2.")) {
                    return emptyEnumeration();
                }
                if (name.startsWith("META-INF/services/org.apache.")) {
                    return emptyEnumeration();
                }
                if (name.startsWith("META-INF/services/javax.servlet.")) {
                    return emptyEnumeration();
                }
                if (name.equals("META-INF/log4j-provider.properties")) {
                    return emptyEnumeration();
                }
                if (name.equals("META-INF/org/apache/")) {
                    return emptyEnumeration();
                }
                if (name.equals("org/slf4j/impl/StaticLoggerBinder.class")) {
                    return emptyEnumeration();
                }
                return super.getResources(name);
            }
        };
    }

    private Collection<URL> createClasspath() throws IOException {
        final File serverJar = mvnResolver.apply(groupId + ":component-server:jar:" + GAV.INSTANCE.getComponentRuntimeVersion());
        if (!serverJar.exists()) {
            throw new IllegalArgumentException(serverJar + " doesn\'t exist");
        }
        final Collection<URL> paths = new HashSet<>(32);
        // we use the Cli as main so we need it
        paths.add(mvnResolver.apply("commons-cli:commons-cli:jar:" + GAV.INSTANCE.getCliVersion()).toURI().toURL());
        paths.add(mvnResolver.apply("org.slf4j:slf4j-jdk14:jar:" + GAV.INSTANCE.getSlf4jVersion()).toURI().toURL());
        // server
        paths.add(serverJar.toURI().toURL());
        Mvn.withDependencies(serverJar, "TALEND-INF/dependencies.txt", false, deps -> {
            aggregateDeps(paths, deps);
            return null;
        });
        // beam if needed
        if (Boolean.getBoolean("components.server.beam.active")) {
            final File beamModule = mvnResolver.apply(groupId + ":component-runtime-beam:" + GAV.INSTANCE.getComponentRuntimeVersion());
            paths.add(beamModule.toURI().toURL());
            Mvn.withDependencies(beamModule, "TALEND-INF/beam.dependencies", true, deps -> {
                aggregateDeps(paths, deps);
                return null;
            });
        }
        return paths;
    }

    private void aggregateDeps(final Collection<URL> paths, final Stream<String> deps) {
        paths.addAll(deps.map(it -> {
            try {
                return mvnResolver.apply(it).toURI().toURL();
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(toSet()));
    }

    private Integer newPort() {
        final Integer port = Integer.getInteger("component.java.port", -1);
        if (port <= 0) {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return port;
    }

}
