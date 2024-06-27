package org.gradle.fixtures.dsl

import groovy.transform.CompileStatic

@CompileStatic
enum GradleDsl {

    GROOVY(".gradle"),
    KOTLIN(".gradle.kts"),
    DECLARATIVE(".gradle.dcl");

    private final String fileExtension;

    GradleDsl(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    String fileNameFor(String fileNameWithoutExtension) {
        return fileNameWithoutExtension + fileExtension;
    }

    String getLanguageCodeName() {
        return name().toLowerCase(Locale.US)
    }

    static List<String> languageCodeNames() {
        return values().collect { GradleDsl val -> val.languageCodeName }
    }
}