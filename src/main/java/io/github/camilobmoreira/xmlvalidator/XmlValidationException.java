package io.github.camilobmoreira.xmlvalidator;


import io.github.camilobmoreira.xmlvalidator.model.Property;
import io.github.camilobmoreira.xmlvalidator.model.Rule;
import io.github.camilobmoreira.xmlvalidator.model.Tag;


/**
 * Exception thrown when the validation of a xml file failed.
 * It contains all the info about the failure, such as the value, property, tag and file that failed the validation,
 * as the rule and the accepted values for that rule and where it was declared (a.k.a. the name of the validation json).
 *
 * @author camilobmoreira
 * @since 1.0
 */
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

    /**
     * Builds the message for the excpetion, containing all the info necessary for finding where the error is in the xml
     * file
     *
     * @return the error message for the exception
     */
    public String buildMessage() {
        StringBuilder message = new StringBuilder("\nError during validation of the file: ")
                .append(this.getFileErrorName()).append(" ");

        if (this.getProperty() != null) {
            message.append("(property: ").append(this.getProperty().getName()).append(" inside ");
        } else {
            message.append("(");
        }

        message.append("tag: ").append(this.getTag().getName()).append(")")
                .append(" due to the rule: ").append(this.getRule().getClass().getSimpleName()).append(".")
                .append(". Accepted value(s): ").append(this.getRule().getValue())
                .append(". Actual value: ").append(this.getValue()).append(".")
                .append(" This rule/values can be found in the following validation file: ")
                .append(this.getValidationJsonName()).append(".");

        return message.toString();
    }
}
