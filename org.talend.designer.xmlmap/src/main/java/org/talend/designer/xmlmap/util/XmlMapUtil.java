// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.xmlmap.util;

import java.util.List;

import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapUtil {

    public static final String DOCUMENT = "id_Document";

    public static final String XPATH_SEPARATOR = "/";

    public static final String EXPRESSION_SEPARATOR = ".";

    public static final String DEFAULT_DATA_TYPE = "id_String";

    public static final String CHILDREN_SEPARATOR = ":/";

    public static final String EXPRESSION_LEFT = "[";

    public static final String EXPRESSION_RIGHT = "]";

    public static final String XPATH_ATTRIBUTE = "@";

    public static final String XPATH_NAMESPACE = "xmlns";

    public static final int DEFAULT_OFFSET = 5;

    /**
     * 
     * DOC talend Comment method "getXPathLength".
     * 
     * @param xPath
     * @return if return >2 , TreeNode is a child of document node.
     */
    public static int getXPathLength(String xPath) {
        if (xPath == null) {
            return 0;
        }

        if (xPath.indexOf(CHILDREN_SEPARATOR) != -1) {

            String childPath = xPath.substring(xPath.indexOf(CHILDREN_SEPARATOR) + 2, xPath.length());
            return 2 + childPath.split(XPATH_SEPARATOR).length;

        } else {
            return xPath.split(XPATH_SEPARATOR).length;
        }

    }

    public static String getXPath(String parentPath, String label, NodeType nodeType) {
        if (parentPath == null || label == null) {
            throw new IllegalArgumentException("Invalid xpath");
        }
        String newXPath = "";
        String type = "";
        if (NodeType.ATTRIBUT.equals(nodeType)) {
            type = XPATH_ATTRIBUTE;
        } else if (NodeType.NAME_SPACE.equals(nodeType)) {
            type = XPATH_NAMESPACE;
        }
        // parentPath is tree xpath
        if (parentPath.indexOf(CHILDREN_SEPARATOR) != -1) {
            String[] split = parentPath.split(CHILDREN_SEPARATOR);
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid xpath");
            }

            newXPath = parentPath + XPATH_SEPARATOR + type + label;

        }
        // parentPath is normal column xpath
        else {
            if (parentPath.indexOf(XPATH_SEPARATOR) == -1) {
                newXPath = parentPath + XPATH_SEPARATOR + label;
            } else if (parentPath.split(XPATH_SEPARATOR).length == 2) {
                newXPath = parentPath.replace(XPATH_SEPARATOR, EXPRESSION_SEPARATOR) + CHILDREN_SEPARATOR + label;
            }
        }
        return newXPath;
    }

    public static String convertToExpression(String xPath) {
        if (xPath == null) {
            return xPath;
        }

        if (xPath.indexOf(CHILDREN_SEPARATOR) != -1) {
            return EXPRESSION_LEFT + xPath + EXPRESSION_RIGHT;
        } else {
            return xPath.replaceAll(XPATH_SEPARATOR, EXPRESSION_SEPARATOR);
        }

    }

    public static void updateChildrenXPath(TreeNode treeNode, String newName, int rootXpathLength) {
        for (TreeNode child : treeNode.getChildren()) {
            String xpath = child.getXpath();
            if (xpath.split(CHILDREN_SEPARATOR).length == 2) {
                String[] split = xpath.split(CHILDREN_SEPARATOR);
                if (rootXpathLength == 2) {
                    String[] subSplit = split[0].split(EXPRESSION_SEPARATOR);
                    if (subSplit.length == 2) {
                        String newXPath = subSplit[0] + EXPRESSION_SEPARATOR + newName + CHILDREN_SEPARATOR + split[1];
                        child.setXpath(newXPath);
                    }
                } else {
                    String[] subSplit = split[1].split(XPATH_SEPARATOR);
                    if (subSplit.length >= 2) {
                        // change to parent name
                        subSplit[rootXpathLength - 2 - 1] = newName;
                        String newXpath = split[0] + CHILDREN_SEPARATOR;
                        for (String string : subSplit) {
                            newXpath = newXpath + string + XPATH_SEPARATOR;
                        }
                        newXpath = newXpath.substring(0, newXpath.length() - 1);
                        child.setXpath(newXpath);
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid xpath");
            }

            if (!child.getChildren().isEmpty()) {
                updateChildrenXPath(child, newName, rootXpathLength);
            }

        }
    }

    /*
     * convert from output expression to xpath
     */
    public static String convertToXpath(String expression) {
        if (expression == null) {
            return expression;
        }

        if (expression.indexOf(CHILDREN_SEPARATOR) != -1) {
            return expression.substring(1, expression.length() - 1);
        } else {
            return expression.replace(EXPRESSION_SEPARATOR, XPATH_SEPARATOR);
        }

    }

    public static TreeNode getInputTreeNodeRoot(TreeNode model) {
        if (model.eContainer() instanceof InputXmlTree) {
            return model;
        } else if (model.eContainer() instanceof TreeNode) {
            return getInputTreeNodeRoot((TreeNode) model.eContainer());
        }
        return null;
    }

    public static OutputTreeNode getOutputTreeNodeRoot(OutputTreeNode model) {
        if (model.eContainer() instanceof OutputXmlTree) {
            return model;
        } else if (model.eContainer() instanceof OutputTreeNode) {
            return getOutputTreeNodeRoot((OutputTreeNode) model.eContainer());
        }
        return null;
    }

    public static void cleanSubGroup(OutputTreeNode node) {
        for (TreeNode treeNode : node.getChildren()) {
            OutputTreeNode outputNode = (OutputTreeNode) treeNode;
            if (outputNode.isGroup()) {
                outputNode.setGroup(false);
            }
            cleanSubGroup(outputNode);

        }

    }

    public static void detachConnectionsTarget(AbstractNode treeNode, XmlMapData mapData) {
        detachConnectionsTarget(treeNode, mapData, true);
    }

    public static void detachConnectionsTarget(AbstractNode treeNode, XmlMapData mapData, boolean detachChildren) {
        for (Connection connection : treeNode.getOutgoingConnections()) {
            if (connection.getTarget() instanceof OutputTreeNode) {
                AbstractNode target = connection.getTarget();
                if (target.getIncomingConnections().contains(connection)) {
                    target.getIncomingConnections().remove(connection);
                    mapData.getConnections().remove(connection);
                }
            }
        }

        // TreeNode detach children's connections
        if (treeNode instanceof TreeNode) {
            TreeNode inputTreeNode = (TreeNode) treeNode;
            if (detachChildren && !inputTreeNode.getChildren().isEmpty()) {
                for (int i = 0; i < inputTreeNode.getChildren().size(); i++) {
                    TreeNode child = inputTreeNode.getChildren().get(i);
                    detachConnectionsTarget(child, mapData);
                }
            }
        }
    }

    public static void detachConnectionsSouce(AbstractNode treeNode, XmlMapData mapData) {
        detachConnectionsSouce(treeNode, mapData, true);
    }

    public static void detachConnectionsSouce(AbstractNode treeNode, XmlMapData mapData, boolean detachChildren) {
        for (Connection connection : treeNode.getIncomingConnections()) {
            AbstractNode source = connection.getSource();
            if (source.getOutgoingConnections().contains(connection)) {
                source.getOutgoingConnections().remove(connection);
                mapData.getConnections().remove(connection);
            }
        }
        if (treeNode instanceof OutputTreeNode) {
            OutputTreeNode outputTreeNode = (OutputTreeNode) treeNode;
            if (detachChildren && !outputTreeNode.getChildren().isEmpty()) {
                for (int i = 0; i < outputTreeNode.getChildren().size(); i++) {
                    TreeNode child = outputTreeNode.getChildren().get(i);
                    detachConnectionsSouce(child, mapData);
                }
            }
        }
    }

    public static void detachLookupTarget(TreeNode treeNode, XmlMapData mapData) {
        for (LookupConnection connection : treeNode.getLookupOutgoingConnections()) {
            if (connection.getTarget() instanceof TreeNode) {
                TreeNode target = (TreeNode) connection.getTarget();
                if (target.getLookupIncomingConnections().contains(connection)) {
                    target.getLookupIncomingConnections().remove(connection);
                    mapData.getConnections().remove(connection);
                }
            }
        }
    }

    public static void detachLookupSource(TreeNode treeNode, XmlMapData mapData) {
        for (LookupConnection connection : treeNode.getLookupIncomingConnections()) {
            TreeNode source = (TreeNode) connection.getSource();
            if (source.getLookupOutgoingConnections().contains(connection)) {
                source.getLookupOutgoingConnections().remove(connection);
                mapData.getConnections().remove(connection);
            }
        }
    }

    public static void findParentsForLoopNode(TreeNode loopNode, List list) {
        Object container = loopNode.eContainer();
        if (container instanceof TreeNode) {
            TreeNode parent = (TreeNode) container;
            list.add(parent);
            findParentsForLoopNode(parent, list);
        }
    }

}
