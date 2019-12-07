# xml-validator-maven-plugin
A Maven Plugin to validate xml files during build lifecycle


### Configuration:

To configure this plugin and add it to your project, you can declare it in your pom file like this:

```
</plugins>
    <plugin>
        <groupId>br.com.cbm</groupId>
        <artifactId>xml-validator-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
      </plugin>
</plugins>
```

#### Build Lifecycle
You can attach the plugin to the build lifecycle like this: 
```
<executions>
  <execution>
    <phase>test</phase>
    <goals>
      <goal>execute</goal>
    </goals>
  </execution>
</executions>
```


#### Parameters:

You can configure some parameters to be used by the plugin like this:

```
<configuration>
    <useBasicRules>false</useBasicRules>
</configuration>
``` 

The accepted parameters are:

* inputDirectory: the directory to search for the xml files. Default: resources folder. 
    * **DO NOT** use this and the **useResourcesDirectory** parameter together.

* useResourcesDirectory: if the plugin should look for xml files to be validated in the resources folder. Else, it's 
    gonna use the target folder. Unless the **inputDirectory** is defined. Default: true

* useBasicRules: if the rules defined in the [basic rules folder][1] should be used. Default: true.

* useCustomRules: if the plugin should look for rules files inside the resource folder of your project. Default: false.


Obs: you can find an example JSON file inside the [basic rules folder][1] 

#### Running:
Or if you want to run just this plugin, you can call it like this:
``` 
mvn xml-validator:execute
```



---
##### Ps: this plugin is still under development, so there might be bugs in it.




[1]: https://github.com/camilobmoreira/xml-validator-maven-plugin/tree/master/src/main/resources/basic-rules