package org.gradle.fixtures.executer;

import org.gradle.fixtures.ExecutionResult;
import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.util.GradleVersion;

public abstract class AbstractDelegatingGradleExecuter extends AbstractGradleExecuter {

    protected AbstractDelegatingGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, GradleVersion.current(), buildContext);
    }

    @Override
    protected ExecutionResult doRun() {
        return configureExecuter().run();
    }

    @Override
    public void assertCanExecute() throws AssertionError {
        configureExecuter().assertCanExecute();
    }

    protected abstract GradleExecuter configureExecuter();
}
