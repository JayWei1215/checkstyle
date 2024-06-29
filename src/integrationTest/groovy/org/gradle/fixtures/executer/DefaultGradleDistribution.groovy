package org.gradle.fixtures.executer

import org.gradle.api.JavaVersion
import org.gradle.api.internal.artifacts.ivyservice.CacheLayout
import org.gradle.cache.internal.CacheVersion
import org.gradle.fixtures.file.TestDirectoryProvider
import org.gradle.fixtures.file.TestFile
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.util.GradleVersion

class DefaultGradleDistribution implements GradleDistribution {
    private static final String DISABLE_HIGHEST_JAVA_VERSION = "org.gradle.java.version.disableHighest";
    private final GradleVersion version;
    private final TestFile gradleHomeDir;
    private final TestFile binDistribution;

    DefaultGradleDistribution(GradleVersion gradleVersion, TestFile gradleHomeDir, TestFile binDistribution) {
        this.version = gradleVersion;
        this.gradleHomeDir = gradleHomeDir;
        this.binDistribution = binDistribution;
    }

    @Override
    String toString() {
        return version.toString();
    }

    @Override
    TestFile getGradleHomeDir() {
        return gradleHomeDir;
    }

    @Override
    GradleVersion getVersion() {
        return version;
    }

    @Override
    GradleExecuter executer(TestDirectoryProvider testDirectoryProvider, IntegrationTestBuildContext buildContext) {
        return new NoDaemonGradleExecuter(this, testDirectoryProvider, version, buildContext).withWarningMode(null);
    }

    @Override
    boolean isSupportsSpacesInGradleAndJavaOpts() {
        return isSameOrNewer("1.0-milestone-5");
    }

    protected boolean isSameOrNewer(String otherVersion) {
        return isVersion(otherVersion) || version.compareTo(GradleVersion.version(otherVersion)) > 0;
    }

    protected boolean isVersion(String otherVersionString) {
        GradleVersion otherVersion = GradleVersion.version(otherVersionString);
        return version.compareTo(otherVersion) == 0 || (version.isSnapshot() && version.getBaseVersion().equals(otherVersion.getBaseVersion()));
    }
}
