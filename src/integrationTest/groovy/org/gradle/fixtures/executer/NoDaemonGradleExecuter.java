package org.gradle.fixtures.executer;

import org.gradle.api.Action;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCachesProvider;
import org.gradle.fixtures.ExecutionResult;
import org.gradle.fixtures.file.TestFile;
import org.gradle.internal.Factory;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.process.internal.DefaultExecHandleBuilder;
import org.gradle.process.internal.JvmOptions;
import org.gradle.util.GradleVersion;
import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.fixtures.internal.NativeServicesTestFixture;
import org.gradle.fixtures.file.TestFiles;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.gradle.internal.impldep.org.junit.Assert.fail;

public class NoDaemonGradleExecuter extends AbstractGradleExecuter{
    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider) {
        super(distribution, testDirectoryProvider);
    }

    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion gradleVersion, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext);
    }

    @Override
    protected ExecutionResult doRun() {
        return createGradleHandle().waitForFinish();
    }

    @Override
    protected GradleHandle createGradleHandle() {
        return createForkingGradleHandle(getResultAssertion(), getDefaultCharacterEncoding(), getExecHandleFactory()).start();
    }

    protected ForkingGradleHandle createForkingGradleHandle(Action<ExecutionResult> resultAssertion, String encoding, Factory<? extends AbstractExecHandleBuilder> execHandleFactory) {
        return new ForkingGradleHandle(getStdinPipe(), isUseDaemon(), resultAssertion, encoding, execHandleFactory, getDurationMeasurement());
    }

    protected Factory<? extends AbstractExecHandleBuilder> getExecHandleFactory() {
        return new Factory<DefaultExecHandleBuilder>() {
            @Override
            public DefaultExecHandleBuilder create() {
                TestFile gradleHomeDir = getDistribution().getGradleHomeDir();
                if (gradleHomeDir != null && !gradleHomeDir.isDirectory()) {
                    fail(gradleHomeDir + " is not a directory.\n"
                            + "The test is most likely not written in a way that it can run with the embedded executer.");
                }

                NativeServicesTestFixture.initialize();
                DefaultExecHandleBuilder builder = new DefaultExecHandleBuilder(TestFiles.pathToFileResolver(), Executors.newCachedThreadPool()) {
                    @Override
                    public File getWorkingDir() {
                        // Override this, so that the working directory is not canonicalised. Some int tests require that
                        // the working directory is not canonicalised
                        return NoDaemonGradleExecuter.this.getWorkingDir();
                    }
                };

                // Clear the user's environment
                builder.environment("GRADLE_HOME", "");
                builder.environment("JAVA_HOME", "");
                builder.environment("GRADLE_OPTS", "");
                builder.environment("JAVA_OPTS", "");
                builder.environment(ArtifactCachesProvider.READONLY_CACHE_ENV_VAR, "");

                GradleInvocation invocation = buildInvocation();

                builder.environment(invocation.environmentVars);
                builder.workingDir(getWorkingDir());
                builder.setStandardInput(connectStdIn());

                builder.args(invocation.args);

                ExecHandlerConfigurer configurer = OperatingSystem.current().isWindows() ? new WindowsConfigurer() : new UnixConfigurer();
                configurer.configure(builder);
                getLogger().debug(String.format("Execute in %s with: %s %s", builder.getWorkingDir(), builder.getExecutable(), builder.getArgs()));
                return builder;
            }
        };
    }

    @Override
    public void assertCanExecute() throws AssertionError {
        if (!getDistribution().isSupportsSpacesInGradleAndJavaOpts()) {
            Map<String, String> environmentVars = buildInvocation().environmentVars;
            for (String envVarName : Arrays.asList("JAVA_OPTS", "GRADLE_OPTS")) {
                String envVarValue = environmentVars.get(envVarName);
                if (envVarValue == null) {
                    continue;
                }
                for (String arg : JvmOptions.fromString(envVarValue)) {
                    if (arg.contains(" ")) {
                        throw new AssertionError(String.format("Env var %s contains arg with space (%s) which is not supported by Gradle %s", envVarName, arg, getDistribution().getVersion().getVersion()));
                    }
                }
            }
        }
    }

}
