package org.gradle.fixtures.executer;

import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.internal.UncheckedException;
import org.gradle.util.GradleVersion;

import java.nio.charset.Charset;
import java.util.Locale;

public class GradleContextualExecuter extends AbstractDelegatingGradleExecuter {

    private GradleExecuter gradleExecuter;
    private Executer executerType;

    private enum Executer {
        embedded(false),
        forking(true),
        noDaemon(true),
        parallel(true, true),
        configCache(true),
        isolatedProjects(true);

        final public boolean forks;
        final public boolean executeParallel;

        Executer(boolean forks) {
            this(forks, false);
        }

        Executer(boolean forks, boolean parallel) {
            this.forks = forks;
            this.executeParallel = parallel;
        }
    }

    @Override
    protected GradleExecuter configureExecuter() {
        if (!getClass().desiredAssertionStatus()) {
            throw new RuntimeException("Assertions must be enabled when running integration tests.");
        }

        if (gradleExecuter == null) {
            gradleExecuter = createExecuter(executerType);
        } else {
            gradleExecuter.reset();
        }
        configureExecuter(gradleExecuter);
        try {
            gradleExecuter.assertCanExecute();
        } catch (AssertionError assertionError) {
            if (gradleExecuter instanceof InProcessGradleExecuter) {
                throw new RuntimeException("Running tests with a Gradle distribution in embedded mode is no longer supported.", assertionError);
            }
            gradleExecuter = new NoDaemonGradleExecuter(getDistribution(), getTestDirectoryProvider());
            configureExecuter(gradleExecuter);
        }

        return gradleExecuter;
    }

    private void configureExecuter(GradleExecuter gradleExecuter) {
        copyTo(gradleExecuter);
    }

    private GradleExecuter createExecuter(Executer executerType) {
        switch (executerType) {
            case embedded:
                return new InProcessGradleExecuter(getDistribution(), getTestDirectoryProvider(), gradleVersion, buildContext);
            case noDaemon:
                return new NoDaemonGradleExecuter(getDistribution(), getTestDirectoryProvider(), gradleVersion, buildContext);
            case parallel:
                return new ParallelForkingGradleExecuter(getDistribution(), getTestDirectoryProvider(), gradleVersion, buildContext);
            case forking:
                return new DaemonGradleExecuter(getDistribution(), getTestDirectoryProvider(), gradleVersion, buildContext);
            case configCache:
                return new ConfigurationCacheGradleExecuter(getDistribution(), getTestDirectoryProvider(), gradleVersion, buildContext);
            case isolatedProjects:
                return new IsolatedProjectsGradleExecuter(getDistribution(), getTestDirectoryProvider(), gradleVersion, buildContext);
            default:
                throw new RuntimeException("Not a supported executer type: " + executerType);
        }
    }



}
