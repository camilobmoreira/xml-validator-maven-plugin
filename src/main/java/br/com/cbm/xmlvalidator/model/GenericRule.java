package br.com.cbm.xmlvalidator.model;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class GenericRule implements Rule {
    @Override
    public boolean accepts(Object value) {
        throw new NotImplementedException();
    }
}
