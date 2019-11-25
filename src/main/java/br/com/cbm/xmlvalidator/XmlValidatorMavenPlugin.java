package br.com.cbm.xmlvalidator;


import br.com.cbm.xmlvalidator.model.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.BooleanUtils;
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


/**
 * The main class for the plugin, responsible for searching, reading, parsing and validating all the necessary files
 *
 * @author camilobmoreira
 * @since 1.0
 */
@Mojo(name = "execute", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class XmlValidatorMavenPlugin extends AbstractMojo {

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
    private DocumentBuilder documentBuilder;

    /**
     * Start any and every service or configuration required for the plugin to be run.
     * Any special configuration that always need to be started should be put here.
     */
    void init() {
        this.registerJsonsAndCreateGson();
    }

    /**
     * The main class for the plugin, responsible for searching, reading, parsing and validating all the necessary files
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        this.init();
        Set<File> allJsonFiles = new HashSet<>();

        if (BooleanUtils.isTrue(this.useBasicRules)) {
            for (String filePath : this.findFilesInResourses(DOT_JSON)) {
                allJsonFiles.add(this.parseFilesInResources(filePath));
            }
        }

        if (BooleanUtils.isTrue(this.useCustomRules)) {
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
        if (BooleanUtils.isTrue(this.useResourcesDirectory)) {
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

    /**
     * Parse files in resource of this project
     *
     * @param filePath the path of the file inside the resources folder
     * @return the file itself
     */
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

    /**
     * Find all files inside the resource folder of a given extension/type
     *
     * @param fileExtension extension of the files to be searched for
     * @return a {@link Set} with path of all the files found
     */
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

    /**
     * Register and create an instance of the {@link Gson} used in this class with all the type adapters necessary
     */
    private void registerJsonsAndCreateGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Rule.class, new InterfaceAdapter());
        this.gson = gsonBuilder.create();
    }

    /**
     * Find all files of a given extension/type inside a folder and its children folders.
     * Note that this is a recursive method.
     *
     * @param folder the folder where the search should start
     * @param fileExtension the extension/type of the files to be searched
     * @return a {@link Set} containing all the files found
     */
    Set<File> findAllFiles(File folder, String fileExtension) {
        Set<File> files = new HashSet<>();
        if (folder == null || folder.listFiles() == null) {
            return files;
        }
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                files.addAll(this.findAllFiles(f, fileExtension));
            } else if (f.getName().toLowerCase().endsWith(fileExtension)) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * Find a {@link File} (file or folder) with an specific name
     *
     * @param folder folder where it should start looking for the file
     * @param fileName name of the file (or folder) to be searched for
     * @return the searched file or null if it doesn't find
     */
    private File findFileByName(File folder, String fileName) {
        if (folder == null || folder.listFiles() == null) {
            return null;
        }
        for (File f : folder.listFiles()) {
            if (f.getName().equalsIgnoreCase(fileName)) {
                return f;
            } else if (f.isDirectory()) {
                return this.findFileByName(f, fileName);
            }
        }
        return null;
    }

    /**
     * Convert a xml file to a {@link Document}, so its content can be easily accessed
     *
     * @param file the file to be converted
     * @return a {@link Document} converted
     *
     * @throws ParserConfigurationException by javax.xml.parsers.DocumentBuilderFactory#newDocumentBuilder()
     * @throws IOException by javax.xml.parsers.DocumentBuilder#parse(java.io.File)
     * @throws SAXException by javax.xml.parsers.DocumentBuilder#parse(java.io.File)
     */
    Document parseXml(File file) throws ParserConfigurationException, IOException, SAXException {
        if (this.documentBuilder == null) {
            this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        }
        return this.documentBuilder.parse(file);
    }

    /**
     * Parse and convert a {@link File} to a {@link ValidationJson}, by:
     * <ul>
     *     <li>
     *         Adds all generic rules (a.k.a. {@link GenericRule}) to all {@link Property} in
     *         {@link ValidationJson#getGenericProperties()}(a.k.a. that were declared as a
     *         {@link Property#GENERIC_PROPERTIES}).
     *     </li>
     *     <li>
     *         Goes through every {@link Rule} of {@link Tag} and every {@link Property} looking for an instance of a
     *         {@link GenericRule}. If it finds, it adds all {@link Rule} in {@link ValidationJson#getGenericRules()}
     *         to said {@link Tag} or {@link Property}
     *     </li>
     *     <li>
     *         Goes through every {@link Tag} looking for a {@link Property} which the {@link Property#getName()} is
     *         {@link String#equalsIgnoreCase(String)} to {@link Property#GENERIC_PROPERTIES}. If it finds, it adds all
     *         the {@link Property} in {@link ValidationJson#getGenericProperties()} to said {@link Tag}
     *     </li>
     *     <li>
     *         Sets the name of the file to {@link ValidationJson#getName()}
     *     </li>
     * </ul>
     *
     * @param file file to be converted
     * @return {@link ValidationJson} with all its content (rules and properties) already parsed
     * @throws FileNotFoundException by java.io.FileReader#FileReader(java.io.File)
     */
    ValidationJson parseValidationJson(File file) throws FileNotFoundException {
        ValidationJson json = this.gson.fromJson(new FileReader(file), ValidationJson.class);
        this.addGenericRulesToGenericProperties(json.getGenericRules(), json.getGenericProperties());
        this.parseGenericRules(json.getTags(), json.getGenericRules());
        this.parseGenericProperties(json.getTags(), json.getGenericProperties());
        json.setName(file.getName());
        return json;
    }

    /**
     * Adds all generic rules (a.k.a. {@link GenericRule}) to all {@link Property} from
     * {@link ValidationJson#getGenericProperties()}
     * (a.k.a. that were declared as a {@link Property#GENERIC_PROPERTIES}).
     *
     * @param genericRules {@link Set} of {@link Rule} in {@link ValidationJson#getGenericRules()}
     * @param genericProperties {@link Set} of {@link Property} in {@link ValidationJson#getGenericProperties()}
     */
    private void addGenericRulesToGenericProperties(Set<Rule> genericRules, Set<Property> genericProperties) {
        for (Property property : genericProperties) {
            property.getRules().addAll(genericRules);
        }
    }

    /**
     * Goes through every {@link Rule} of {@link Tag} and every {@link Property} looking for an instance of a
     * {@link GenericRule}. If it finds, it adds all {@link Rule} from {@link ValidationJson#getGenericRules()} to said
     * {@link Tag} or {@link Property}
     *
     * @param tags tags to be searched for {@link GenericRule}
     * @param genericRules generic rules from {@link ValidationJson#getGenericRules()}
     */
    private void parseGenericRules(Set<Tag> tags, Set<Rule> genericRules) {
        for (Tag tag : tags) {
            this.checkRulesForGenericRule(tag.getRules(), genericRules);
            for (Property property : tag.getProperties()) {
                this.checkRulesForGenericRule(property.getRules(), genericRules);
            }
        }
    }

    /**
     * Check if any {@link Rule} is an instance of {@link GenericRule}, if it finds, cobine them.
     *
     * @param rules rules to be searched
     * @param genericRules generic rules to be added to the (other) rules
     */
    private void checkRulesForGenericRule(Set<Rule> rules, Set<Rule> genericRules) {
        for (Rule rule : rules) {
            if (rule instanceof GenericRule) {
                rules.addAll(genericRules);
                return;
            }
        }
    }

    /**
     * Goes through every {@link Tag} looking for a {@link Property} which the {@link Property#getName()} is
     * {@link String#equalsIgnoreCase(String)} to {@link Property#GENERIC_PROPERTIES}. If it finds, it adds all the
     * {@link Property} from {@link ValidationJson#getGenericProperties()} to said {@link Tag}
     *
     * @param tags
     * @param genericProperties
     */
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

    /**
     * Validate a {@link Document} (a.k.a. the xml file) against the {@link ValidationJson}
     *
     * @param validationJson the json with the rules to use for validation
     * @param xmlDocument the xml documento to be validated
     * @throws XmlValidationException in case the validation fails
     */
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

    /**
     * Check if the {@link NamedNodeMap} is of a tag that should be validated comparing its name to each
     * {@link Tag#getName()}. If it should be validated, then check if its value is valid.
     * It also checks each {@link Property} from {@link Tag#getProperties()} to see if they are valid.
     *
     * @param tag
     * @param propertiesToBeValidated
     * @throws XmlValidationException in case the validation fails
     */
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

    /**
     * Check if the {@link Node} is of a tag property that should be validated comparing its name to each
     * {@link Property#getName()}. If it should be validated, then check if its value is valid.
     *
     * @param properties
     * @param toBeValidated
     * @throws XmlValidationException in case the validation fails
     */
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

    /**
     * Check if every {@link Rule} to see if it {@link Rule#accepts(Object)} the value
     *
     * @param rules the rules used to validate
     * @param value the value to be validated
     * @throws XmlValidationException in case the validation fails
     */
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
