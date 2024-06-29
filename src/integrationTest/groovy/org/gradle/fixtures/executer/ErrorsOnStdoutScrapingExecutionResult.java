package org.gradle.fixtures.executer;

import org.gradle.fixtures.ExecutionResult;

public class ErrorsOnStdoutScrapingExecutionResult implements ExecutionResult {
    private final ExecutionResult delegate;

    public ErrorsOnStdoutScrapingExecutionResult(ExecutionResult delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getOutput() {
        return delegate.getOutput();
    }

    @Override
    public String getNormalizedOutput() {
        return delegate.getNormalizedOutput();
    }

    @Override
    public String getError() {
        return delegate.getError();
    }

    @Override
    public ExecutionResult assertTasksExecuted(Object... taskPaths) {
        delegate.assertTasksExecuted(taskPaths);
        return this;
    }

    @Override
    public void assertResultVisited() {
        delegate.assertResultVisited();
    }

    @Override
    public ExecutionResult assertOutputEquals(String expectedOutput, boolean ignoreExtraLines, boolean ignoreLineOrder) {
        delegate.assertOutputEquals(expectedOutput, ignoreExtraLines, ignoreLineOrder);
        return this;
    }
}
