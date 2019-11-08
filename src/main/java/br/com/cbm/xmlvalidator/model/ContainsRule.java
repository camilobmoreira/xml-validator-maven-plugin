package br.com.cbm.xmlvalidator.model;

import java.util.Set;

public class ContainsRule implements Rule {
    private Set<Object> values;

    @Override
    public boolean accepts(Object value) {
        for (Object o : this.values) {
            if (o.equals(value) || (o instanceof String && value instanceof String && ((String) o).equalsIgnoreCase((String) value))) {
                return true;
            }
        }
        return false;
    }
}
