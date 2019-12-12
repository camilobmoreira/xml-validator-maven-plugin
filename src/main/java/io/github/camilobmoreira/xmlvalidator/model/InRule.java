package io.github.camilobmoreira.xmlvalidator.model;


import java.util.Set;


/**
 * Rule for when a field should be contained in a list of elements
 *
 * @author camilobmoreira
 * @since 1.0
 */
public class InRule implements Rule {

    private Set<Object> value;

    public Set<Object> getValue() {
        return this.value;
    }

    public void setValue(Set<Object> value) {
        this.value = value;
    }

    @Override
    public boolean accepts(Object value) {
        if (this.value.contains(value)) {
            return true;
        }
        for (Object o : this.value) {
            if (o.equals(value) || (o instanceof String && value instanceof String && ((String) o)
                    .equalsIgnoreCase((String) value))) {
                return true;
            }
        }
        return false;
    }
}
