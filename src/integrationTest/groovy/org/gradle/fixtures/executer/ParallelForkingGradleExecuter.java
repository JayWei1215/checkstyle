package org.gradle.fixtures.executer;

import org.gradle.api.Action;
import org.gradle.fixtures.ExecutionResult;
import org.gradle.internal.Factory;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.util.GradleVersion;
import org.gradle.fixtures.file.TestDirectoryProvider;

import java.util.ArrayList;
import java.util.List;

class ParallelForkingGradleExecuter extends DaemonGradleExecuter {
    public ParallelForkingGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion gradleVersion, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext);
    }

    @Override
    protected List<String> getAllArgs() {
        List<String> args = new ArrayList<String>();
        args.addAll(super.getAllArgs());
        if (getDistribution().getVersion().compareTo(GradleVersion.version("2.3")) <= 0) {
            args.add("--parallel-threads=4");
        } else {
            args.add("--parallel");
            maybeSetMaxWorkers(args);
        }
        return args;
    }

    private void maybeSetMaxWorkers(List<String> args) {
        for (String arg : args) {
            if (arg.startsWith("--max-workers")) {
                return;
            }
        }
        args.add("--max-workers=4");
    }

    @Override
    protected ForkingGradleHandle createForkingGradleHandle(Action<ExecutionResult> resultAssertion, String encoding, Factory<? extends AbstractExecHandleBuilder> execHandleFactory) {
        return new ParallelForkingGradleHandle(getStdinPipe(), isUseDaemon(), resultAssertion, encoding, execHandleFactory, getDurationMeasurement());
    }
}
