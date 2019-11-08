package br.com.cbm.xmlvalidator.model;

public interface Rule {

    Object getValue();

    boolean accepts(Object value);
}
