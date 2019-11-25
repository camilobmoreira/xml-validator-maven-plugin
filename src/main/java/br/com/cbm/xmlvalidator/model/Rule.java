package br.com.cbm.xmlvalidator.model;


/**
 * Interface that every rule should implement
 *
 * @author camilobmoreira
 * @since 1.0
 */
public interface Rule {

    Object getValue();

    boolean accepts(Object value);
}
