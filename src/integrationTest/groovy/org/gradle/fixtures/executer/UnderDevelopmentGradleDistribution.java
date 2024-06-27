package org.gradle.fixtures.executer;

import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.fixtures.file.TestFile;

public class UnderDevelopmentGradleDistribution extends DefaultGradleDistribution {

    public UnderDevelopmentGradleDistribution() {
        this(IntegrationTestBuildContext.INSTANCE);
    }

    public UnderDevelopmentGradleDistribution(IntegrationTestBuildContext buildContext) {
        this(buildContext, buildContext.getGradleHomeDir());
    }

    public UnderDevelopmentGradleDistribution(IntegrationTestBuildContext buildContext, TestFile gradleHomeDir) {
        super(
                buildContext.getVersion(),
                gradleHomeDir,
                buildContext.getNormalizedBinDistribution()
        );
    }

    @Override
    public GradleExecuter executer(TestDirectoryProvider testDirectoryProvider, IntegrationTestBuildContext buildContext) {
        return new GradleContextualExecuter(this, testDirectoryProvider, buildContext).withWarningMode(null);
    }
}