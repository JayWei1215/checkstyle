package org.gradle.fixtures.executer;

import org.gradle.fixtures.ExecutionResult;
import org.hamcrest.Matcher;

import java.util.function.Consumer;

public interface ExecutionFailure extends ExecutionResult {
    /**
     * {@inheritDoc}
     */
    ExecutionFailure getIgnoreBuildSrc();

    ExecutionFailure assertHasLineNumber(int lineNumber);

    ExecutionFailure assertHasFileName(String filename);

    interface Failure {
        /**
         * Asserts that this failure has the given number of direct causes.
         */
        void assertHasCauses(int count);

        /**
         * Asserts that this failure has the given cause
         */
        void assertHasCause(String message);

        /**
         * Asserts that this failure has the given first cause
         */
        void assertHasFirstCause(String message);
    }

}
