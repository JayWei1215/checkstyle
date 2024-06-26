package org.gradle.fixtures.executer;

import org.gradle.fixtures.ExecutionResult;

public abstract class OutputScrapingGradleHandle implements GradleHandle {

    protected ExecutionResult toExecutionResult(String output, String error) {
        return OutputScrapingExecutionResult.from(output, error);
    }

    protected ExecutionResult toExecutionFailure(String output, String error) {
        return OutputScrapingExecutionFailure.from(output, error);
    }
}
