package br.com.cbm.xmlvalidator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;


public class XmlValidatorMavenPluginTest {

    XmlValidatorMavenPlugin xmlValidatorMavenPlugin = new XmlValidatorMavenPlugin();

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
        int length = doc.getElementsByTagName("createTable").item(0).getAttributes().item(0).getNodeValue().length();

        File basicRules = new File("./src/main/resources/basic-rules");
        Set<File> allJsonFiles = this.xmlValidatorMavenPlugin.findAllFiles(basicRules, ".json");
        Set<Object> json = new HashSet<>();
        for (File jsonFile : allJsonFiles) {
            try {
                json.add(this.xmlValidatorMavenPlugin.parseValidationJson(jsonFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        Assert.assertFalse(allXmlFiles.isEmpty());
    }


}

