package org.gradle.fixtures.executer

import org.gradle.fixtures.file.TestDirectoryProvider
import org.gradle.initialization.StartParameterBuildOptions
import org.gradle.util.GradleVersion

class IsolatedProjectsGradleExecuter extends DaemonGradleExecuter {

    static final List<String> ISOLATED_PROJECTS_ARGS = [
            "-D${StartParameterBuildOptions.IsolatedProjectsOption.PROPERTY_NAME}=true",
            "-D${StartParameterBuildOptions.ConfigurationCacheMaxProblemsOption.PROPERTY_NAME}=0",
            "-Dorg.gradle.configuration-cache.internal.load-after-store=${testWithLoadAfterStore()}"
    ].collect { it.toString() }

    static boolean testWithLoadAfterStore() {
        return !Boolean.getBoolean("org.gradle.configuration-cache.internal.test-disable-load-after-store")
    }

    IsolatedProjectsGradleExecuter(
            GradleDistribution distribution,
            TestDirectoryProvider testDirectoryProvider,
            GradleVersion gradleVersion,
            IntegrationTestBuildContext buildContext
    ) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext)
    }

    @Override
    protected List<String> getAllArgs() {
        def args = super.getAllArgs()
        // Don't enable if CC is disabled
        if (args.contains("--no-configuration-cache")) {
            return args
        }
        // Don't enable if IP explicitly disabled
        if (args.contains("-D${StartParameterBuildOptions.IsolatedProjectsOption.PROPERTY_NAME}=false")) {
            return args
        }
        return args + ISOLATED_PROJECTS_ARGS
    }
}
