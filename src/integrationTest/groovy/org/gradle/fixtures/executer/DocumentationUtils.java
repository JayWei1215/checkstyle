package org.gradle.fixtures.executer;

import static org.gradle.api.internal.DocumentationRegistry.BASE_URL;

public class DocumentationUtils {
    public static final String DOCS_GRADLE_ORG = "https://docs.gradle.org/";
    public static final String PATTERN = DOCS_GRADLE_ORG + "current/";

    public static String normalizeDocumentationLink(String message) {
        return message.replace(PATTERN, BASE_URL + "/");
    }
}
