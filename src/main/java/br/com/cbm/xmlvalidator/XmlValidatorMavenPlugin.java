package br.com.cbm.xmlvalidator;


import br.com.cbm.xmlvalidator.model.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@Mojo(name = "execute", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class XmlValidatorMavenPlugin extends AbstractMojo {
    //TODO JAVADOC
    static final String DOT_JSON = ".json";
    static final String DOT_XML = ".xml";
    private static final String DOT_JAR = ".jar";
    private static final String RESOURCES = "resources";
    private static final String PLUGIN_DESCRIPTOR = "pluginDescriptor";

    @Parameter(defaultValue = "${project.build.directory}", property = "targetDirectory", readonly = true)
    private File targetDirectory;
    @Parameter(defaultValue = "${project.build.directory}", property = "inputDirectory")
    private File inputDirectory;
    @Parameter(defaultValue = "true", property = "useResourcesDirectory")
    private Boolean useResourcesDirectory;
    @Parameter(defaultValue = "true", property = "useBasicRules")
    private Boolean useBasicRules;
    @Parameter(defaultValue = "false", property = "useCustomRules")
    private Boolean useCustomRules;

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private Gson gson;

    public void execute() throws MojoExecutionException {
        this.registerJsonsAndCreateGson();
        Set<File> allJsonFiles = new HashSet<>();
        if (this.useBasicRules != null && this.useBasicRules) {
            for (String filePath : this.findFilesInResourses(DOT_JSON)) {
                allJsonFiles.add(this.parseFilesInResources(filePath));
            }
        }

        if (this.useCustomRules != null && this.useCustomRules) {
            allJsonFiles.addAll(this.findAllFiles(this.inputDirectory, DOT_JSON));
        }

        Set<ValidationJson> allValidationJsons = new HashSet<>();
        for (File jsonFile : allJsonFiles) {
            try {
                allValidationJsons.add(this.parseValidationJson(jsonFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Set<File> allXmlFiles = new HashSet<>();
        if (this.useResourcesDirectory != null && this.useResourcesDirectory) {
            File resourcesFolder = this.findFileByName(this.targetDirectory.getParentFile(), RESOURCES);
            allXmlFiles.addAll(this.findAllFiles(resourcesFolder, DOT_XML));
        } else {
            allXmlFiles.addAll(this.findAllFiles(this.inputDirectory, DOT_XML));
        }
        for (File file : allXmlFiles) {
            try {
                Document doc = this.parseXml(file);
                for (ValidationJson validationJson : allValidationJsons) {
                    this.validate(validationJson, doc);
                }
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            } catch (XmlValidationException e) {
                e.setFileErrorName(file.getName());
                throw new MojoExecutionException(e.buildMessage(), e);
            }
        }
    }

    private File findFileByName(File file, String fileName) {
        if (file == null || file.listFiles() == null) {
            return null;
        }
        for (File f : file.listFiles()) {
            if (f.getName().equalsIgnoreCase(fileName)) {
                return f;
            } else if (f.isDirectory()) {
                return this.findFileByName(f, fileName);
            }
        }
        return null;
    }

    private File parseFilesInResources(String filePath) {
        try {
            File file = File.createTempFile(filePath, null);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] byteArray = new byte[1024];
            int i;
            InputStream inputStream = super.getClass().getClassLoader().getResourceAsStream(filePath);
            if (inputStream == null) {
                return null;
            }
            //While the input stream has bytes
            while ((i = inputStream.read(byteArray)) > 0) {
                //Write the bytes to the output stream
                fileOutputStream.write(byteArray, 0, i);
            }
            //Close streams to prevent errors
            inputStream.close();
            fileOutputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<String> findFilesInResourses(String fileExtension) {
        PluginDescriptor pluginDescriptor = (PluginDescriptor) this.getPluginContext().get(PLUGIN_DESCRIPTOR);
        File file = new File(pluginDescriptor.getSource());
        Set<String> filesPath = new HashSet<>();
        if (file.getName().endsWith(DOT_JAR)) {
            try {
                Enumeration<JarEntry> entries = new JarFile(file).entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(fileExtension)) {
                        filesPath.add(entry.getName());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filesPath;
    }

    void registerJsonsAndCreateGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Rule.class, new InterfaceAdapter());
        this.gson = gsonBuilder.create();
    }

    Set<File> findAllFiles(File file, String fileExtension) {
        Set<File> files = new HashSet<>();
        if (file == null || file.listFiles() == null) {
            return files;
        }
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                files.addAll(this.findAllFiles(f, fileExtension));
            } else if (f.getName().toLowerCase().endsWith(fileExtension)) {
                files.add(f);
            }
        }
        return files;
    }

    Document parseXml(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = this.documentBuilderFactory.newDocumentBuilder();
        return builder.parse(file);
    }

    ValidationJson parseValidationJson(File file) throws FileNotFoundException {
        ValidationJson json = this.gson.fromJson(new FileReader(file), ValidationJson.class);
        this.addGenericRulesToGenericProperties(json.getGenericRules(), json.getGenericProperties());
        this.parseGenericRules(json.getTags(), json.getGenericRules());
        this.parseGenericProperties(json.getTags(), json.getGenericProperties());
        json.setName(file.getName());
        return json;
    }

    private void addGenericRulesToGenericProperties(Set<Rule> genericRules, Set<Property> genericProperties) {
        for (Property property : genericProperties) {
            property.getRules().addAll(genericRules);
        }
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

    void validate(ValidationJson validationJson, Document xmlDocument) throws XmlValidationException {
        for (Tag tag : validationJson.getTags()) {
            NodeList tagsToBeValidated = xmlDocument.getElementsByTagName(tag.getName());
            for (int i = 0; i < tagsToBeValidated.getLength(); i++) {
                Node currentTag = tagsToBeValidated.item(i);
                try {
                    this.validateProperties(tag, currentTag.getAttributes());
                } catch (XmlValidationException e) {
                    e.setValidationJsonName(validationJson.getName());
                    throw e;
                }
            }
        }
    }

    private void validateProperties(Tag tag, NamedNodeMap propertiesToBeValidated) throws XmlValidationException {
        for (int j = 0; j < propertiesToBeValidated.getLength(); j++) {
            Node currentProperty = propertiesToBeValidated.item(j);
            try {
                this.validateRules(tag.getRules(), currentProperty.getNodeName());
                this.validadeProperty(tag.getProperties(), currentProperty);
            } catch (XmlValidationException e) {
                e.setTag(tag);
                throw e;
            }

        }
    }

    private void validadeProperty(Set<Property> properties, Node toBeValidated) throws XmlValidationException {
        String propertyName = toBeValidated.getNodeName();
        String propertyValue = toBeValidated.getNodeValue();
        for (Property property : properties) {
            if (property.getName().equalsIgnoreCase(propertyName)) {
                try {
                    this.validateRules(property.getRules(), propertyValue);
                } catch (XmlValidationException e) {
                    e.setProperty(property);
                    throw e;
                }
            }
        }
    }

    private void validateRules(Set<Rule> rules, String value) throws XmlValidationException {
        for (Rule rule : rules) {
            if (!rule.accepts(value)) {
                XmlValidationException exception = new XmlValidationException();
                exception.setRule(rule);
                exception.setValue(value);
                throw exception;
            }
        }
    }
}
