package org.gradle.fixtures.executer

import org.gradle.fixtures.file.TestDirectoryProvider
import org.gradle.initialization.StartParameterBuildOptions
import org.gradle.util.GradleVersion

class ConfigurationCacheGradleExecuter extends DaemonGradleExecuter {

    static final List<String> CONFIGURATION_CACHE_ARGS = [
            "--${StartParameterBuildOptions.ConfigurationCacheOption.LONG_OPTION}",
            "-D${StartParameterBuildOptions.ConfigurationCacheQuietOption.PROPERTY_NAME}=true",
            "-D${StartParameterBuildOptions.ConfigurationCacheMaxProblemsOption.PROPERTY_NAME}=0",
            "-Dorg.gradle.configuration-cache.internal.load-after-store=${testWithLoadAfterStore()}"
    ].collect { it.toString() }

    static boolean testWithLoadAfterStore() {
        return !Boolean.getBoolean("org.gradle.configuration-cache.internal.test-disable-load-after-store")
    }

    ConfigurationCacheGradleExecuter(
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
        if (args.contains("--no-configuration-cache")) { // Don't enable if explicitly disabled
            return args
        } else {
            return args + CONFIGURATION_CACHE_ARGS
        }
    }
}
