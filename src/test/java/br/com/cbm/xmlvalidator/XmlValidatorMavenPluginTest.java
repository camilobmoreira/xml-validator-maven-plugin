package br.com.cbm.xmlvalidator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import br.com.cbm.xmlvalidator.model.Property;
import br.com.cbm.xmlvalidator.model.Rule;
import br.com.cbm.xmlvalidator.model.Tag;
import br.com.cbm.xmlvalidator.model.ValidationJson;


public class XmlValidatorMavenPluginTest {

    private XmlValidatorMavenPlugin xmlValidatorMavenPlugin = new XmlValidatorMavenPlugin();

    @Test
    public void teste() {
        this.xmlValidatorMavenPlugin.registerJsonsAndCreateGson();

        File file = new File("./");
        Set<File> allXmlFiles = this.xmlValidatorMavenPlugin.findAllFiles(file, ".xml");

        File xmlFile = null;
        for (File allXmlFile : allXmlFiles) {
            if (allXmlFile.getName().toLowerCase().equals("example.xml")) {
                xmlFile = allXmlFile;
            }
        }

        Document doc = null;
        try {
            doc = this.xmlValidatorMavenPlugin.parseXml(xmlFile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        File basicRules = new File("./src/main/resources/basic-rules");
        Set<File> allJsonFiles = this.xmlValidatorMavenPlugin.findAllFiles(basicRules, ".json");
        Set<ValidationJson> allValidationJsons = new HashSet<>();
        try {
            for (File jsonFile : allJsonFiles) {
                allValidationJsons.add(this.xmlValidatorMavenPlugin.parseValidationJson(jsonFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            for (ValidationJson validationJson : allValidationJsons) {
                this.xmlValidatorMavenPlugin.validate(validationJson, doc);
            }
        } catch (MojoExecutionException e) {
            e.printStackTrace();
        }

        Assert.assertFalse(allXmlFiles.isEmpty());
    }
}

