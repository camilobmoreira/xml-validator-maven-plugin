package br.com.cbm.xmlvalidator.model;

public class MinLengthRule implements Rule {

    private Integer value;

    @Override
    public boolean accepts(Object value) {
        if (!(value instanceof Integer)) {
            return false;
        }
        return this.value > (Integer) value;
    }
}
