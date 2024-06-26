package org.gradle

import org.gradle.api.reporting.Report
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class CheckstyleTest extends Specification {
    def project = ProjectBuilder.builder().build()
    def checkstyle = project.tasks.create("checkstyle", org.gradle.Checkstyle)

    def "default configuration"() {
        expect:
        with(checkstyle) {
            checkstyleClasspath == null
            classpath == null
            configFile == null
            config == null
            configProperties == [:]
            !reports.xml.required.get()
            !reports.xml.outputLocation.isPresent()
            reports.xml.outputType == Report.OutputType.FILE
            !reports.html.required.get()
            !reports.html.outputLocation.isPresent()
            reports.html.outputType == Report.OutputType.FILE
            !reports.sarif.required.get()
            !reports.sarif.outputLocation.isPresent()
            reports.sarif.outputType == Report.OutputType.FILE
            !ignoreFailures
            showViolations
            maxErrors == 0
            maxWarnings == Integer.MAX_VALUE
            !minHeapSize.isPresent()
            !maxHeapSize.isPresent()
        }
    }

    def "can use legacy configFile property"() {
        checkstyle.configFile = project.file("config/file.txt")

        expect:
        checkstyle.configFile == project.file("config/file.txt")
        checkstyle.config.inputFiles.singleFile == project.file("config/file.txt")
    }
}
