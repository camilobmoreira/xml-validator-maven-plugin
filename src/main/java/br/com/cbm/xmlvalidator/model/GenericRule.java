package br.com.cbm.xmlvalidator.model;


public class GenericRule implements Rule {

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean accepts(Object value) {
        return true;
    }
}
