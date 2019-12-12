package io.github.camilobmoreira.xmlvalidator.model;


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
