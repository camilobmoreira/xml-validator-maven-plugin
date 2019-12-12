package io.github.camilobmoreira.xmlvalidator.model;

/**
 * Class that represents the field genericRules in the validation json file.
 * Every rule declared inside this field in the json is gonna be used in every other tag/property that has this
 * class/field among its rules
 *
 * @author camilobmoreira
 * @since 1.0
 */
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
