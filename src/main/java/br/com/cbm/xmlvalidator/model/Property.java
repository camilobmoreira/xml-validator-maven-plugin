package br.com.cbm.xmlvalidator.model;


import java.util.HashSet;
import java.util.Set;


/**
 * Class that represents the property of a tag of a xml document
 *
 * @author camilobmoreira
 * @since 1.0
 */
public class Property {

    public static final String GENERIC_PROPERTIES = "genericProperties";
    private String name;
    private Set<Rule> rules = new HashSet<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Rule> getRules() {
        return this.rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }
}
