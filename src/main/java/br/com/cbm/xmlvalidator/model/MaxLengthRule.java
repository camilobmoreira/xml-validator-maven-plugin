package br.com.cbm.xmlvalidator.model;


/**
 * Rule that represents the maximum length of a field
 *
 * @author camilobmoreira
 * @since 1.0
 */
public class MaxLengthRule implements Rule {

    private Integer value;

    /**
     * @return o valor da propriedade value
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * @param value o novo valor de value
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean accepts(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        return this.value >= ((String) value).length();
    }
}
