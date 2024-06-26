package org.gradle.fixtures.executer;

import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.util.GradleVersion;

public class DaemonGradleExecuter extends NoDaemonGradleExecuter {
    public DaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion gradleVersion, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext);
        super.requireDaemon();
    }
}
