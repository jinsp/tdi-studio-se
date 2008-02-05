// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.wizard.creatcomponent;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.model.enumtype.LanguageType;

/**
 * @author rli
 * 
 */
public class WizardComponentFolderPage extends AbstractComponentPage {

    private Text componentFamily;

    private Text componentLongName;

    private Button useJavaLangButton;

    private Button usePerlLangButton;

    private Text componentFolderText;

    /**
     * @param pageName
     * @param componentPref
     */
    public WizardComponentFolderPage(String pageName, ComponentPref componentPref) {
        super(pageName, componentPref);
    }

    @Override
    protected void initialize() {
        if (this.componentPref.getName() == null) {
            useJavaLangButton.setSelection(true);
            componentPref.setLanguageType(LanguageType.JAVALANGUAGETYPE);
            componentFolderText.addModifyListener(new ModifyListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                public void modifyText(ModifyEvent e) {
                    setPageComplete(validatePage());
                    componentPref.setName(componentFolderText.getText());
                }

            });
            componentLongName.addModifyListener(new ModifyListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                public void modifyText(ModifyEvent e) {
                    setPageComplete(validatePage());
                    componentPref.setLongName(componentLongName.getText());
                }

            });
            componentFamily.addModifyListener(new ModifyListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                public void modifyText(ModifyEvent e) {
                    setPageComplete(validatePage());
                    componentPref.setFamily(componentFamily.getText());
                }

            });
            this.setPageComplete(validatePage());
        } else {
            this.componentFolderText.setText(componentPref.getName());
            this.componentFolderText.setEnabled(false);
            switch (componentPref.getLanguageType()) {
            case PERLLANGUAGETYPE:
                this.usePerlLangButton.setSelection(true);
                this.useJavaLangButton.setSelection(false);
                break;
            case JAVALANGUAGETYPE:
                this.usePerlLangButton.setSelection(false);
                this.useJavaLangButton.setSelection(true);
                break;
            case BOTHLANGUAGETYPE:
                this.usePerlLangButton.setSelection(true);
                this.useJavaLangButton.setSelection(true);
                break;
            default:
            }
            this.setPageComplete(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageContent(Composite parent) {

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        this.createComponentGroup(composite);
        this.setControl(composite);
    }

    private void createComponentGroup(Composite parent) {
        // component specification group
        Group optionsGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, true);
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        Label nameLabel = new Label(optionsGroup, SWT.NONE);
        nameLabel.setText("Name");
        componentFolderText = new Text(optionsGroup, SWT.BORDER | SWT.LEFT);
        componentFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label longNameLabel = new Label(optionsGroup, SWT.NONE);
        longNameLabel.setText("Long Name (mouseover tooltip)");
        componentLongName = new Text(optionsGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.LEFT);
        componentLongName.setLayoutData(new GridData(225, 60));

        Label familyLabel = new Label(optionsGroup, SWT.NONE);
        familyLabel.setText("Family");
        componentFamily = new Text(optionsGroup, SWT.BORDER | SWT.LEFT);
        componentFamily.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // create the language selection check button
        Label availability = new Label(optionsGroup, SWT.LEFT);
        availability.setText("Available in");

        Composite languageGroup = new Composite(optionsGroup, SWT.NONE);
        languageGroup.setLayout(layout);
        // languageGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        useJavaLangButton = new Button(languageGroup, SWT.CHECK | SWT.RIGHT);
        useJavaLangButton.setText("Java");
        usePerlLangButton = new Button(languageGroup, SWT.CHECK | SWT.RIGHT);
        usePerlLangButton.setText("Perl");

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageComplete(validatePage());
                LanguageType type = LanguageType.find(useJavaLangButton.getSelection(), usePerlLangButton.getSelection());
                componentPref.setLanguageType(type);
                // propertyChangeBean.firePropertyChange(PluginConstant.LANGUAGE_PROPERTY, null, type);
            }
        };
        useJavaLangButton.addSelectionListener(listener);
        usePerlLangButton.addSelectionListener(listener);
    }

    @Override
    protected boolean validatePage() {
        if (useJavaLangButton != null && usePerlLangButton != null) {
            if (!(useJavaLangButton.getSelection() || usePerlLangButton.getSelection())) {
                setErrorMessage("The component language have not been selected");
                return false;
            }
        }
        if (this.componentFolderText.isEnabled()) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PluginConstant.COMPONENT_PROJECT);
            String componentName = componentFolderText.getText();
            if (componentName.equals(PluginConstant.EMPTY_STRING)) {
                setErrorMessage("The component Name is empty");
                return false;
            }
            IFolder componentFolder = project.getFolder(componentName);
            if (componentFolder.exists()) {
                setErrorMessage("This component already exists");
                return false;
            }
        }

        if (this.componentLongName.isEnabled()) {
            String longName = componentLongName.getText();
            if (longName.equals(PluginConstant.EMPTY_STRING)) {
                setErrorMessage("The component Long Name is empty");
                return false;
            }
        }

        if (this.componentFamily.isEnabled()) {
            String family = componentFamily.getText();
            if (family.equals(PluginConstant.EMPTY_STRING)) {
                setErrorMessage("The component Family is empty");
                return false;
            }
        }
        setErrorMessage(null);
        return true;
    }

    /**
     * Getter for componentName.
     * 
     * @return the componentName
     */
    public String getComponentFolderName() {
        return componentFolderText.getText();
    }

    /**
     * Getter for componentFamily.
     * 
     * @return the componentFamily
     */
    public Text getComponentFamily() {
        return componentFamily;
    }

    /**
     * Getter for componentLongName.
     * 
     * @return the componentLongName
     */
    public Text getComponentLongName() {
        return componentLongName;
    }
}
