/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.metadata.node;

import org.eclipse.swt.graphics.Image;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitFolderRepositoryNode extends AbsTaCoKitRepositoryNode implements ITaCoKitRepositoryNode {

    public TaCoKitFolderRepositoryNode(final IRepositoryViewObject repViewObject, final RepositoryNode parent,
            final ITaCoKitRepositoryNode parentTaCoKitNode, final String label, final ConfigTypeNode configTypeNode)
            throws Exception {
        super(repViewObject, parent, parentTaCoKitNode, label, configTypeNode);
        this.setType(ENodeType.SIMPLE_FOLDER);
    }

    @Override
    public Image getImage() {
        return null;
    }

    // TODO remove it as it has the same impl as the parent
    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public boolean isFolderNode() {
        return true;
    }

}
