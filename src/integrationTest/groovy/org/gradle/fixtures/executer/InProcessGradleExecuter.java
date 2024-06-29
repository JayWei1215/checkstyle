package org.gradle.fixtures.executer;

import org.gradle.fixtures.FileSystemWatchingHelper;
import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.internal.UncheckedException;
import org.gradle.util.GradleVersion;

public class InProcessGradleExecuter extends DaemonGradleExecuter {
    public InProcessGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion gradleVersion, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext);
        waitForChangesToBePickedUpBeforeExecution();
    }

    private void waitForChangesToBePickedUpBeforeExecution() {
        // File system watching is now on by default, so we need to wait for changes to be picked up before each execution.
        beforeExecute(executer -> {
            try {
                FileSystemWatchingHelper.waitForChangesToBePickedUp();
            } catch (InterruptedException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }
        });
    }
}
