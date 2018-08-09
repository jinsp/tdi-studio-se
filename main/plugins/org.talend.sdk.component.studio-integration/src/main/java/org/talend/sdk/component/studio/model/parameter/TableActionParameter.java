package org.talend.sdk.component.studio.model.parameter;

import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.model.action.ActionParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class TableActionParameter extends ActionParameter {

    private final TableElementParameter elementParameter;

    TableActionParameter(final TableElementParameter elementParameter, final String actionParameter) {
        super(elementParameter.getName(), actionParameter, null);
        this.elementParameter = elementParameter;
    }

    @Override
    public Collection<Pair<String, String>> parameters() {
        final List<Map<String, String>> value = (List<Map<String, String>>) elementParameter.getValue();
        final List<Pair<String, String>> parameters = new ArrayList<>();
        for (int i=0; i<value.size(); i++) {
            final Map<String, String> row = value.get(i);
            for(Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                key = key.replace("[]", "[" + i + "]").replace(elementParameter.getName(), getParameter());
                final String paramValue = removeQuotes(entry.getValue());
                final Pair parameter = new Pair(key, paramValue);
                parameters.add(parameter);
            }
        }
        return parameters;
    }
}
