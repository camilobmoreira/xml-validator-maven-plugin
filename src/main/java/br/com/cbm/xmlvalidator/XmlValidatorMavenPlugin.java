package br.com.cbm.xmlvalidator;


import br.com.cbm.xmlvalidator.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;



@Mojo(name = "validate", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class XmlValidatorMavenPlugin extends AbstractMojo {

    //@Parameter(defaultValue = "${project.basedir}/src/main/resources", property = "outputDir", required = true) //fixme
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File inputDirectory;
    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private Gson gson;

    public void execute() throws MojoExecutionException {
        this.registerJsonsAndCreateGson();

        File inputDirectory = this.inputDirectory;
        Set<File> allXmlFiles = this.findAllFiles(inputDirectory, ".xml");

        File xmlFile = null;
        for (File allXmlFile : allXmlFiles) {
            if (allXmlFile.getName().toLowerCase().equals("example.xml")) {
                xmlFile = allXmlFile;
            }
        }

        Document doc = null;
        try {
            doc = this.parseXml(xmlFile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        File basicRules = new File("./src/main/resources/basic-rules");
        Set<File> allJsonFiles = this.findAllFiles(basicRules, ".json");
        Set<ValidationJson> allValidationJsons = new HashSet<>();
        for (File jsonFile : allJsonFiles) {
            try {
                allValidationJsons.add(this.parseValidationJson(jsonFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        for (ValidationJson validationJson : allValidationJsons) {
            this.validate(validationJson, doc);
        }

    }

    protected void registerJsonsAndCreateGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Rule.class, new InterfaceAdapter());
        this.gson = gsonBuilder.create();
    }

    protected Set<File> findAllFiles(File file, String name) {
        Set<File> files = new HashSet<>();
        if (file.listFiles() == null) {
            return files;
        }
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                files.addAll(this.findAllFiles(f, name));
            } else if (f.getName().toLowerCase().endsWith(name)){
                files.add(f);
            }
        }
        return files;
    }

    protected Document parseXml(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = null;
        Document doc = null;
        builder = this.factory.newDocumentBuilder();
        doc = builder.parse(file);
        return doc;
    }

    protected ValidationJson parseValidationJson(File file) throws FileNotFoundException {
        ValidationJson validationJson = this.gson.fromJson(new FileReader(file), ValidationJson.class);
        this.parseGenericRules(validationJson.getTags(), validationJson.getGenericRules());
        this.parseGenericProperties(validationJson.getTags(), validationJson.getGenericProperties());
        return validationJson;
    }

    private void parseGenericRules(Set<Tag> tags, Set<Rule> genericRules) {
        for (Tag tag : tags) {
            this.checkRulesForGenericRule(tag.getRules(), genericRules);
            for (Property property : tag.getProperties()) {
                this.checkRulesForGenericRule(property.getRules(), genericRules);
            }
        }
    }

    private void checkRulesForGenericRule(Set<Rule> rules, Set<Rule> genericRules) {
        for (Rule rule : rules) {
            if (rule instanceof GenericRule) {
                rules.addAll(genericRules);
                return;
            }
        }
    }

    private void parseGenericProperties(Set<Tag> tags, Set<Property> genericProperties) {
        for (Tag tag : tags) {
            for (Property property : tag.getProperties()) {
                if (property.getName().equalsIgnoreCase(Property.GENERIC_PROPERTIES)) {
                    tag.getProperties().addAll(genericProperties);
                    break;
                }
            }
        }
    }

    protected void validate(ValidationJson validationJson, Document xmlDocument) throws MojoExecutionException {
        for (Tag tag : validationJson.getTags()) {
            NodeList tagsToBeValidated = xmlDocument.getElementsByTagName(tag.getName());
            for (int i = 0; i < tagsToBeValidated.getLength(); i++) {
                Node currentTag = tagsToBeValidated.item(i);
                this.validateProperties(tag, currentTag.getAttributes());
            }
        }
    }

    private void validateProperties(Tag tag, NamedNodeMap propertiesToBeValidated) throws MojoExecutionException {
        for (int j = 0; j < propertiesToBeValidated.getLength(); j++) {
            Node currentProperty = propertiesToBeValidated.item(j);
            this.validateRules(tag.getRules(), currentProperty.getNodeName(), "deu rum 1");

            this.validadeProperty(tag.getProperties(), currentProperty);
        }
    }

    private void validadeProperty(Set<Property> properties, Node toBeValidated) throws MojoExecutionException {
        String propertyName = toBeValidated.getNodeName();
        String propertyValue = toBeValidated.getNodeValue();
        for (Property property : properties) {
            if (property.getName().equalsIgnoreCase(propertyName)) {
                this.validateRules(property.getRules(), propertyValue, "deu rum 2 ");
            }
        }
    }

    private void validateRules(Set<Rule> rules, String value, String message) throws MojoExecutionException {
        for (Rule rule : rules) {
            if (!rule.accepts(value)) {
                throw new MojoExecutionException(message + value);//fixme exceção correta?
            }
        }
    }
}
