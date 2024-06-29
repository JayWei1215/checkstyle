package org.gradle.fixtures.executer;

import org.gradle.internal.impldep.junit.framework.AssertionFailedError;

import java.util.List;

public class AbstractFailure implements ExecutionFailure.Failure {
    final String description;
    final List<String> causes;

    public AbstractFailure(String description, List<String> causes) {
        this.description = description;
        this.causes = causes;
    }

    @Override
    public void assertHasCause(String message) {
        if (!causes.contains(message)) {
            throw new AssertionFailedError(String.format("Expected cause '%s' not found in %s", message, causes));
        }
    }

    @Override
    public void assertHasFirstCause(String message) {
        if (causes.isEmpty()) {
            throw new AssertionFailedError(String.format("Expected first cause '%s', got none", message));
        }
        String firstCause = causes.get(0);
        if (!firstCause.equals(message)) {
            throw new AssertionFailedError(String.format("Expected first cause '%s', got '%s'", message, firstCause));
        }
    }

    @Override
    public void assertHasCauses(int count) {
        if (causes.size() != count) {
            throw new AssertionFailedError(String.format("Expecting %d cause(s), got %d", count, causes.size()));
        }
    }
}
