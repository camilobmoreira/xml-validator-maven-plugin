package br.com.cbm.xmlvalidator.model;

import java.util.Set;

public class InRule implements Rule{

    private Set<Object> value;

    /**
     * @return o valor da propriedade value
     */
    public Set<Object> getValue() {
        return this.value;
    }

    /**
     * @param value o novo valor de value
     */
    public void setValue(Set<Object> value) {
        this.value = value;
    }

    @Override
    public boolean accepts(Object value) {
        for (Object o : this.value) {
            if (!o.equals(value) || (o instanceof String && value instanceof String && !((String) o).equalsIgnoreCase((String) value))) {
                return false;
            }
        }
        return true;
    }
}
