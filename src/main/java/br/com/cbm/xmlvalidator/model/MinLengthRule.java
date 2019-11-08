package br.com.cbm.xmlvalidator.model;

public class MinLengthRule implements Rule {

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
        if (!(value instanceof Integer)) {
            return false;
        }
        return this.value > (Integer) value;
    }
}
