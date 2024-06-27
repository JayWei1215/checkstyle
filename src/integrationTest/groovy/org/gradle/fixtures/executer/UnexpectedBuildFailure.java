package org.gradle.fixtures.executer;

public class UnexpectedBuildFailure extends RuntimeException {
    public UnexpectedBuildFailure(String message) {
        super(message);
    }

    public UnexpectedBuildFailure(Throwable e) {
        super(e);
    }

    public UnexpectedBuildFailure(String message, Throwable e) {
        super(message, e);
    }
}
