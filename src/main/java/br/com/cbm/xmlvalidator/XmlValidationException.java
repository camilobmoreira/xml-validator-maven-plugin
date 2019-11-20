package br.com.cbm.xmlvalidator;


import br.com.cbm.xmlvalidator.model.Property;
import br.com.cbm.xmlvalidator.model.Rule;
import br.com.cbm.xmlvalidator.model.Tag;


public class XmlValidationException extends Exception {

    private String fileErrorName;
    private String validationJsonName;
    private Rule rule;
    private Property property;
    private Tag tag;
    private String value;

    public String getFileErrorName() {
        return this.fileErrorName;
    }

    public void setFileErrorName(String fileErrorName) {
        this.fileErrorName = fileErrorName;
    }

    public String getValidationJsonName() {
        return this.validationJsonName;
    }

    public void setValidationJsonName(String validationJsonName) {
        this.validationJsonName = validationJsonName;
    }

    public Rule getRule() {
        return this.rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Property getProperty() {
        return this.property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Tag getTag() {
        return this.tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String buildMessage() {
        //TODO
        return "ERROR";
    }
}
