package io.github.camilobmoreira.xmlvalidator;


import com.google.inject.internal.util.Lists;
import io.github.camilobmoreira.xmlvalidator.model.MaxLengthRule;
import io.github.camilobmoreira.xmlvalidator.model.ValidationJson;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * Test class for {@link XmlValidatorMavenPlugin}
 *
 * @author camilobmoreira
 * @since 1.0
 */
public class XmlValidatorMavenPluginTest {

    private static final String LIQUIBASE_ORACLE_RULES_JSON = "liquibase-oracle-rules.json";
    private static final String BASIC_TEST_XML = "basicTest.xml";
    private static final String BASIC_RULES_PATH = "./src/main/resources/basic-rules";
    private static final String TEST_DIRECTORY_PATH = "./src/test/";
    private XmlValidatorMavenPlugin xmlValidatorMavenPlugin = new XmlValidatorMavenPlugin();

    @Before
    public void setup() {
        this.xmlValidatorMavenPlugin.init();
    }

    //TODO more test cases
    @Test(expected = MojoExecutionException.class)
    public void basicTest() throws MojoExecutionException {
        Set<File> allXmlFiles = this.findAndAssertAllFiles();
        Set<ValidationJson> allValidationJsons = this.findAndAssertAllValidationJsons();

        for (File file : allXmlFiles) {
            try {
                this.validateXmlFile(file, allValidationJsons);
            } catch (XmlValidationException e) {
                this.assertAndThrowException(e, "basicTest.xml", "column", "name", "email_1312312312312312312123312");
                return;
            }
        }
        Assert.fail();
    }

    @Test
    public void findFileByNameTest() {
        File currentDirectory = new File(TEST_DIRECTORY_PATH);

        File testResources =
                this.xmlValidatorMavenPlugin.findFileByName(currentDirectory, XmlValidatorMavenPlugin.RESOURCES);
        Assert.assertNotNull(testResources);
        Assert.assertTrue(testResources.isDirectory());
        Assert.assertFalse(Lists.newArrayList(testResources.listFiles()).isEmpty());
        Assert.assertEquals("./src/test/resources", testResources.getPath());

        File basicTestXml =
                this.xmlValidatorMavenPlugin.findFileByName(currentDirectory, BASIC_TEST_XML);
        Assert.assertNotNull(basicTestXml);
        Assert.assertFalse(basicTestXml.isDirectory());
        Assert.assertEquals("./src/test/resources/xml-examples/basicTest.xml", basicTestXml.getPath());
    }



    // ******************** HELPER METHODS ********************
    private void assertAndThrowException(XmlValidationException exception, String xmlName, String tagName, String propertyName, String value) throws MojoExecutionException {
        Class<MaxLengthRule> ruleClass = MaxLengthRule.class;
        Assert.assertEquals(LIQUIBASE_ORACLE_RULES_JSON, exception.getValidationJsonName());
        Assert.assertEquals(tagName, exception.getTag().getName());
        Assert.assertEquals(propertyName, exception.getProperty().getName());
        Assert.assertEquals(value, exception.getValue());
        Assert.assertEquals(ruleClass, exception.getRule().getClass());
        Assert.assertEquals(xmlName, exception.getFileErrorName());
        throw new MojoExecutionException(exception.getMessage());
    }

    private void validateXmlFile(File file, Set<ValidationJson> allValidationJsons) throws XmlValidationException {
        for (ValidationJson validationJson : allValidationJsons) {
            try {
                Document doc = this.convertAndAssertDocument(file);
                this.xmlValidatorMavenPlugin.validate(validationJson, doc);
            } catch (XmlValidationException e) {
                e.setValidationJsonName(validationJson.getName());
                e.setFileErrorName(file.getName());
                throw e;
            }
        }
    }

    private Set<ValidationJson> findAndAssertAllValidationJsons() {
        File basicRules = new File(BASIC_RULES_PATH);
        Set<File> allJsonFiles =
                this.xmlValidatorMavenPlugin.findAllFiles(basicRules, XmlValidatorMavenPlugin.DOT_JSON);
        Set<ValidationJson> allValidationJsons = new HashSet<>();
        try {
            for (File jsonFile : allJsonFiles) {
                allValidationJsons.add(this.xmlValidatorMavenPlugin.parseValidationJson(jsonFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(allValidationJsons.isEmpty());
        return allValidationJsons;
    }

    private Document convertAndAssertDocument(File xmlFile) {
        Document doc = null;
        try {
            doc = this.xmlValidatorMavenPlugin.parseXml(xmlFile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(doc);
        return doc;
    }

    private File findAndAssertFileByName(Set<File> allXmlFiles, String xmlName) {
        File xmlFile = null;
        for (File currentFile : allXmlFiles) {
            if (currentFile.getName().equalsIgnoreCase(xmlName)) {
                xmlFile = currentFile;
            }
        }
        Assert.assertNotNull(xmlFile);
        Assert.assertEquals(xmlName, xmlFile.getName());
        return xmlFile;
    }

    private Set<File> findAndAssertAllFiles() {
        File file = new File("./");
        Set<File> allXmlFiles = this.xmlValidatorMavenPlugin.findAllFiles(file, XmlValidatorMavenPlugin.DOT_XML);
        Assert.assertFalse(allXmlFiles.isEmpty());
        return allXmlFiles;
    }
}

