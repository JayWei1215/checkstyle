package org.gradle.fixtures.executer;

import org.gradle.fixtures.file.TestFile;
import org.gradle.util.GradleVersion;

import javax.annotation.Nullable;
import java.io.File;

public class IntegrationTestBuildContext {
    // Collect this early, as the process' current directory can change during embedded test execution
    public static final TestFile TEST_DIR = new TestFile(new File(".").toURI());
    public static final IntegrationTestBuildContext INSTANCE = new IntegrationTestBuildContext();

    @Nullable
    public TestFile getGradleHomeDir() {
        return optionalFile("integTest.gradleHomeDir");
    }

    public TestFile getSamplesDir() {
        return file("integTest.samplesdir", null);
    }

    public TestFile getCommitDistributionsDir() {
        return file("integTest.commitDistributionsDir", null);
    }

    @Nullable
    public TestFile getNormalizedBinDistribution() {
        return optionalFile("integTest.normalizedDistribution");
    }

    @Nullable
    public TestFile getBinDistribution() {
        return optionalFile("integTest.binDistribution");
    }

    @Nullable
    public TestFile getAllDistribution() {
        return optionalFile("integTest.allDistribution");
    }

    @Nullable
    public TestFile getDocsDistribution() {
        return optionalFile("integTest.docsDistribution");
    }

    @Nullable
    public TestFile getSrcDistribution() {
        return optionalFile("integTest.srcDistribution");
    }

    @Nullable
    public TestFile getLocalRepository() {
        return optionalFile("integTest.localRepository");
    }

    public TestFile getDaemonBaseDir() {
        return file("org.gradle.integtest.daemon.registry", "build/daemon");
    }

    public TestFile getGradleUserHomeDir() {
        return file("integTest.gradleUserHomeDir", "intTestHomeDir/distributions-unknown");
    }

    public TestFile getTmpDir() {
        return file("integTest.tmpDir", "build/tmp");
    }

    public TestFile getNativeServicesDir() {
        return getGradleUserHomeDir().file("native");
    }

    public GradleVersion getVersion() {
        return GradleVersion.current();
    }

    /**
     * The timestamped version used in the docs and the bin and all zips. This should be different to {@link GradleVersion#getVersion()}.
     */
    public GradleVersion getDistZipVersion() {
        return GradleVersion.version(System.getProperty("integTest.distZipVersion", GradleVersion.current().getVersion()));
    }

//    public GradleDistribution distribution(String version) {
//        NativeServicesTestFixture.initialize();
//
//        if (version.equals(getVersion().getVersion())) {
//            return new UnderDevelopmentGradleDistribution();
//        }
//        TestFile previousVersionDir = getGradleUserHomeDir().getParentFile().file("previousVersion");
//
//        if (CommitDistribution.isCommitDistribution(version)) {
//            return new CommitDistribution(version, getCommitDistributionsDir());
//        }
//        return new ReleasedGradleDistribution(version, previousVersionDir.file(version));
//    }

    protected static TestFile file(String propertyName, String defaultPath) {
        TestFile testFile = optionalFile(propertyName);
        if (testFile != null) {
            return testFile;
        }
        if (defaultPath == null) {
            throw new RuntimeException("You must set the '" + propertyName + "' property to run the integration tests.");
        }
        return testFile(defaultPath);
    }

    @Nullable
    private static TestFile optionalFile(String propertyName) {
        String path = System.getProperty(propertyName);
        // MODULE_WORKING_DIR doesn't seem to work correctly and MODULE_DIR seems to be in `.idea/modules/<path-to-subproject>`
        // See https://youtrack.jetbrains.com/issue/IDEA-194910
        return path != null ? new TestFile(new File(path.replace(".idea/modules/", ""))) : null;
    }

    private static TestFile testFile(String path) {
        File file = new File(path);
        return file.isAbsolute()
                ? new TestFile(file)
                : new TestFile(TEST_DIR.file(path).getAbsoluteFile());
    }

}
