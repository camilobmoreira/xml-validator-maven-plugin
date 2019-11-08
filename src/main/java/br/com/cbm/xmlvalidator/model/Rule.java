package br.com.cbm.xmlvalidator.model;

public interface Rule {

    boolean accepts(Object value);
}
