package io.github.camilobmoreira.xmlvalidator.model;


import java.util.HashSet;
import java.util.Set;


/**
 * Class that represents a tag of a xml document
 *
 * @author camilobmoreira
 * @since 1.0
 */
public class Tag {

    private String name;
    private Set<Property> properties = new HashSet<>();
    private Set<Rule> rules = new HashSet<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public Set<Rule> getRules() {
        return this.rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }
}
