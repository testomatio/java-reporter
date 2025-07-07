# Testomat.io Java Reporter
### [0.1.0]

## Overview

This is the Java implementation of Testomat.io reporter.  
The library is still being developed, so some Testomat.io functionality is to be implemented.


By this time next features are implemented:
 - Out-of-the-box support for JUnit5, TestNG, Cucumber test cases' lifecycle;
 - Autorun with minimal configuration;
 - Customizable test run parameters (see full list of JVM properties below);
 - Async tests handling;
 - Core classes (frameworks' extensions) overriding for user;
 - Test run grouping, merging;

## Installation

- Use **maven dependency** to integrate the library into your project:
```xhtml
<dependency>
    <groupId>io.testomat</groupId>
    <artifactId>java-reporter</artifactId>
    <version>0.x.0</version>
</dependency>
```
- Be aware that the dependency uses **jackson-databind 2.15.2**

## Configuration

### Without core classes overriding

#### JUnit5

- In your `src/main/resources` create the `junit-platform.properties` and add this line:
```properties
    junit.jupiter.extensions.autodetection.enabled = true
```

#### TestNG and Cucumber will run the extensions from library themselves.

### With core classes overriding(not recommended) or customization:
**In addition to the previous configuration for JUnit (if you use it)**  
**NOTE: If you use Cucumber with any other framework - configure only for Cucumber**

The core classes in the library that are responsible for the tests lifecycles are totally customizable and replaceable.  
Core classes: **CucumberListener**, **TestNgListener**, **JunitListener**. 

You can customize them to add any additional
logic based on the tests behavior (like external API calls, etc.)  
by copying to your project or 
extend the one you need and override the methods in the way you want.



- In your `src/main/resources` create the directories `META-INF/services` and add particular file related to your test 
framework:

#### JUnit5:
- Filename:
```properties
    org.junit.jupiter.api.extension.Extension
```
#### TestNG
- Filename:
```properties
    org.testng.ITestNGListener io.cucumber.plugin.Plugin
```

#### Cucumber
- Filename:
```properties
    io.cucumber.plugin.Plugin
```

#### Content to put into the created file:
```properties
    your.extension.file.path (path from source root without .java extension)  
    for example com.testomatio.reporter.core.frameworkintegration.CucumberListener
```

## Usage
In order to report your tests to the Testomat.io api you will need to provide the project API KEY  
(starts with "tstmt_") and you might want to change the run title. All other properties have default values.  

The list of available properties by this time (with default value if it has):
```properties
testomatio.api.key = your project api key. Mandatory.
testomatio.batch.size= test batch size default and minimum = 5
testomatio.batch.flush.interval= batch flush interval default and minimum = 5
testomatio.url= default value = https://app.testomat.io/
testomatio.run.title= default value = default_run_title

testomatio.run.id = add current run test results to some that already exists
testomatio.env = the environment name
testomatio.run.group = group name if you group your runs
testomatio.create = the test that are not yet registered in the run will be created and registered to the current run
testomatio.shared.run = shared run name
testomatio.shared.run.timeout = shared run timeout
testomatio.publish = 1 if you want to get public url to your test run result
```

