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

    private static final String EXAMPLE_XML_FILE_NAME = "example.xml";
    private static final String BASIC_RULES_PATH = "./src/main/resources/basic-rules";
    private XmlValidatorMavenPlugin xmlValidatorMavenPlugin = new XmlValidatorMavenPlugin();

    //TODO more test cases
    @Test(expected = MojoExecutionException.class)
    public void basicTest() throws MojoExecutionException {
        this.xmlValidatorMavenPlugin.registerJsonsAndCreateGson();

        File file = new File("./");
        Set<File> allXmlFiles = this.xmlValidatorMavenPlugin.findAllFiles(file, XmlValidatorMavenPlugin.DOT_XML);
        Assert.assertFalse(allXmlFiles.isEmpty());

        File xmlFile = null;
        for (File currentFile : allXmlFiles) {
            if (currentFile.getName().toLowerCase().equals(EXAMPLE_XML_FILE_NAME)) {
                xmlFile = currentFile;
            }
        }
        Assert.assertNotNull(xmlFile);
        Assert.assertEquals(EXAMPLE_XML_FILE_NAME, xmlFile.getName());

        Document doc = null;
        try {
            doc = this.xmlValidatorMavenPlugin.parseXml(xmlFile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(doc);

        File basicRules = new File(BASIC_RULES_PATH);
        Set<File> allJsonFiles = this.xmlValidatorMavenPlugin.findAllFiles(basicRules, XmlValidatorMavenPlugin.DOT_JSON);
        Set<ValidationJson> allValidationJsons = new HashSet<>();
        try {
            for (File jsonFile : allJsonFiles) {
                allValidationJsons.add(this.xmlValidatorMavenPlugin.parseValidationJson(jsonFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(allValidationJsons.isEmpty());

        for (ValidationJson validationJson : allValidationJsons) {
            try {
                this.xmlValidatorMavenPlugin.validate(validationJson, doc);
            } catch (XmlValidationException e) {
                e.setValidationJsonName(validationJson.getName());
                throw new MojoExecutionException(e.getMessage());
            }
        }
        Assert.fail();
    }
}

