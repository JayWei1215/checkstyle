package org.gradle.fixtures.executer;

import org.gradle.internal.nativeintegration.console.TestConsoleMetadata;

public enum ConsoleAttachment {
    NOT_ATTACHED("not attached to a console", null),
    ATTACHED("console attached to both stdout and stderr", TestConsoleMetadata.BOTH),
    ATTACHED_STDOUT_ONLY("console attached to stdout only", TestConsoleMetadata.STDOUT_ONLY),
    ATTACHED_STDERR_ONLY("console attached to stderr only", TestConsoleMetadata.STDERR_ONLY);

    private final String description;
    TestConsoleMetadata consoleMetaData;

    ConsoleAttachment(String description, TestConsoleMetadata consoleMetaData) {
        this.description = description;
        this.consoleMetaData = consoleMetaData;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStderrAttached() {
        return consoleMetaData != null && consoleMetaData.isStdErr();
    }

    public boolean isStdoutAttached() {
        return consoleMetaData != null && consoleMetaData.isStdOut();
    }

    public TestConsoleMetadata getConsoleMetaData() {
        return consoleMetaData;
    }
}
