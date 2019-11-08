package br.com.cbm.xmlvalidator;


import br.com.cbm.xmlvalidator.model.Property;
import br.com.cbm.xmlvalidator.model.Rule;
import br.com.cbm.xmlvalidator.model.Tag;
import br.com.cbm.xmlvalidator.model.ValidationJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
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

        File f = this.inputDirectory;

        Set<File> xmlFiles = this.findAllFiles(f, ".xml");



        for (File xmlFile : xmlFiles) {
            this.getLog().info(xmlFile.getAbsolutePath());
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

    }

    private void parseGenericProperties(Set<Tag> tags, Set<Property> genericProperties) {

    }
}
