# Testomatio Java Reporter
[![Maven Central](https://img.shields.io/maven-central/v/io.testomat/testomatio-java-core.svg?label=Maven%20Central)](https://central.sonatype.com/namespace/io.testomat)
#### Almost Beta Testing! 🚀

### How to use
1. Add `testomatio-testng` module to your project
2. Set ENV variable TESTOMATIO_API_KEY with your Testomatio API Key
3. Add testomatio.properties file to your project with the following **required** properties:
    ```properties
    #Host of your Testomatio Instance
    testomatio.host=https://testomat.io
    #Project name
    testomatio.project=java-reporter
    ```
4. Run your Tests

### Properties
- TODO


## Testomatio TestNG Module
### Usage
- TODO

#### Maven
```xml
<dependency>
   <groupId>io.testomat</groupId>
   <artifactId>testomatio-testng</artifactId>
   <version>0.4.1</version>
</dependency>
```
#### Gradle
```groovy
dependencies {
    implementation('io.testomat:testomatio-testng:0.4.1')
}
```

## Testomatio Java Reporter Roadmap
Here is a list of features that are planned for nearest future

### TODO: Testomatio Java Core
- [x] Maven Central Publishing
- [ ] Stepper class with step() methods

### TODO: Testomatio Modules 
- [x] Implement `testomatio-java-core` module
- [x] Implement `testomatio-testng` module
- [ ] Implement `testomatio-junit` module
- [ ] Implement `testomatio-okhttp3` module
- [ ] Implement `testomatio-selenide` module
- [ ] Implement `testomatio-rest-assured` module
- [ ] Implement `testomatio-e2e-tests` module

Created by LolikTest with Love 💚