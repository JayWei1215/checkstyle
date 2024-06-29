package org.gradle.fixtures.executer;

import org.gradle.api.Action;
import org.gradle.fixtures.ExecutionResult;
import org.gradle.internal.Factory;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.util.internal.IncubationLogger;

import java.io.PipedOutputStream;

import static java.lang.String.format;

public class ParallelForkingGradleHandle extends ForkingGradleHandle {

    public ParallelForkingGradleHandle(PipedOutputStream stdinPipe, boolean isDaemon, Action<ExecutionResult> resultAssertion, String outputEncoding, Factory<? extends AbstractExecHandleBuilder> execHandleFactory, DurationMeasurement durationMeasurement) {
        super(stdinPipe, isDaemon, resultAssertion, outputEncoding, execHandleFactory, durationMeasurement);
    }

    @Override
    protected ExecutionResult toExecutionResult(String output, String error) {
        return new ParallelExecutionResult(output, error);
    }

    @Override
    protected ExecutionResult toExecutionFailure(String output, String error) {
        return new ParallelExecutionResult(output, error);
    }

    /**
     * Need a different output comparator for parallel execution.
     */
    private static class ParallelExecutionResult extends OutputScrapingExecutionFailure {
        public ParallelExecutionResult(String output, String error) {
            super(output, error, true);
        }

        @Override
        public String getNormalizedOutput() {
            String output = super.getNormalizedOutput();
            String parallelWarningPrefix = String.format(IncubationLogger.INCUBATION_MESSAGE, ".*");
            return output.replaceFirst(format("(?m)%s.*$\n", parallelWarningPrefix), "");
        }

        @Override
        public ExecutionResult assertOutputEquals(String expectedOutput, boolean ignoreExtraLines, boolean ignoreLineOrder) {
            // We always ignore line order for matching out of parallel builds
            super.assertOutputEquals(expectedOutput, ignoreExtraLines, true);
            return this;
        }
    }
}
