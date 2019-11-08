package br.com.cbm.xmlvalidator.model;

import java.util.Set;

public class ValidationJson {
    private Set<Tag> tags;
    private Set<Rule> genericRules;
    private Set<Property> genericProperties;

    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Rule> getGenericRules() {
        return this.genericRules;
    }

    public void setGenericRules(Set<Rule> genericRules) {
        this.genericRules = genericRules;
    }

    public Set<Property> getGenericProperties() {
        return this.genericProperties;
    }

    public void setGenericProperties(Set<Property> genericProperties) {
        this.genericProperties = genericProperties;
    }
}
