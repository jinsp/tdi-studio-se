// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class ChangeMode2HAPI4HL7Output extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = { "tHL7Output" }; //$NON-NLS-1$ 

        IComponentConversion changeDBName4Hive = new IComponentConversion() {

            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType modes = ComponentUtilities.getNodeProperty(node, "GENERATION_MODE"); //$NON-NLS-1$

                if (modes == null) {
                    ComponentUtilities.addNodeProperty(node, "GENERATION_MODE", "CLOSED_LIST");//$NON-NLS-1$ //$NON-NLS-2$
                }
                String mode = ComponentUtilities.getNodePropertyValue(node, "GENERATION_MODE"); //$NON-NLS-1$
                if (mode == null || "".equals(mode.trim())) { //$NON-NLS-1$
                    ComponentUtilities.setNodeValue(node, "GENERATION_MODE", "HAPI"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

        };

        for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeDBName4Hive));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 9, 30, 17, 0, 0);
        return gc.getTime();
    }
}
