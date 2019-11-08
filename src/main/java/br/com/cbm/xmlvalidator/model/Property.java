package br.com.cbm.xmlvalidator.model;

import java.util.HashSet;
import java.util.Set;

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
