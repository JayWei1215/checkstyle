package org.gradle

import org.gradle.fixtures.MultiVersionIntegrationSpec
import spock.lang.Specification

class CheckstylePluginClasspathIntegrationTest extends MultiVersionIntegrationSpec{
    def setup() {
        writeBuildFiles()
        writeConfigFile()
        goodCode()
    }

    def "accepts throwing exception from other project"() {
        expect:
        succeeds("checkstyleMain")
    }

    private void writeBuildFiles() {
        file("settings.gradle") << """
include "api"
include "client"
        """

        file("build.gradle") << """
subprojects {
    apply plugin: "java"
    apply plugin: "org.gradle.checkstyle"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        checkstyle 'org.gradle:checkstyle:1.0-SNAPSHOT'
    }

    checkstyle {
        toolVersion = '1.0-SNAPSHOT'
        configFile rootProject.file("checkstyle.xml")
    }
}

project("client") {
    dependencies {
        implementation project(":api")
    }
}
        """
    }

    private void writeConfigFile() {
        file("checkstyle.xml") << """
<!DOCTYPE module PUBLIC
        "-//Puppy Crawl//DTD Check Configuration 1.2//EN"
        "http://www.puppycrawl.com/dtds/configuration_1_2.dtd">
<module name="Checker">
    <module name="TreeWalker">
        <module name="JavadocMethod"/>
    </module>
</module>
        """
    }

    private void goodCode() {
        file("api/src/main/java/org/gradle/FooException.java") << """
package org.gradle;

class FooException extends Exception { }
        """

        file("client/src/main/java/org/gradle/Iface.java") << """
package org.gradle;

interface Iface {
    /**
     * Method Description.
     *
     * @throws FooException whenever
     * @throws IllegalArgumentException otherwise
     */
    void foo() throws FooException, IllegalArgumentException;
}
        """
    }
}
