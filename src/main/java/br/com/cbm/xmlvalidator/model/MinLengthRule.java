package br.com.cbm.xmlvalidator.model;


/**
 * Rule that represents the minimum length of a field
 *
 * @author camilobmoreira
 * @since 1.0
 */
public class MinLengthRule implements Rule {

    private Integer value;

    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean accepts(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        return this.value <= ((String) value).length();
    }
}
