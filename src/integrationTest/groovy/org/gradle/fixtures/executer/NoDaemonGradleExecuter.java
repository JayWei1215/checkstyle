package org.gradle.fixtures.executer;

import org.gradle.api.Action;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCachesProvider;
import org.gradle.fixtures.ExecutionResult;
import org.gradle.fixtures.file.TestFile;
import org.gradle.internal.Factory;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.process.internal.DefaultExecHandleBuilder;
import org.gradle.process.internal.ExecHandleBuilder;
import org.gradle.process.internal.JvmOptions;
import org.gradle.process.internal.streams.SafeStreams;
import org.gradle.util.GradleVersion;
import org.gradle.fixtures.file.TestDirectoryProvider;
import org.gradle.fixtures.internal.NativeServicesTestFixture;
import org.gradle.fixtures.file.TestFiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.joining;
import static org.gradle.internal.impldep.org.junit.Assert.fail;

public class NoDaemonGradleExecuter extends AbstractGradleExecuter{
    private interface ExecHandlerConfigurer {
        void configure(ExecHandleBuilder builder);
    }

    private class WindowsConfigurer implements ExecHandlerConfigurer {
        @Override
        public void configure(ExecHandleBuilder builder) {
            String cmd;
            if (getExecutable() != null) {
                cmd = getExecutable().replace('/', File.separatorChar);
            } else {
                cmd = "gradle";
            }
            builder.executable("cmd.exe");

            List<String> allArgs = builder.getArgs();
            String actualCommand = quote(quote(cmd) + " " + allArgs.stream().map(NoDaemonGradleExecuter::quote).collect(joining(" ")));
            builder.setArgs(Arrays.asList("/d", "/c", actualCommand));

            String gradleHome = getDistribution().getGradleHomeDir().getAbsolutePath();

            // NOTE: Windows uses Path, but allows asking for PATH, and PATH
            //       is set within builder object for some things such
            //       as CommandLineIntegrationTest, try PATH first, and
            //       then revert to default of Path if null
            Object path = builder.getEnvironment().get("PATH");
            if (path == null) {
                path = builder.getEnvironment().get("Path");
            }
            path = String.format("%s\\bin;%s", gradleHome, path);
            builder.environment("PATH", path);
            builder.environment("Path", path);
        }
    }

    private class UnixConfigurer implements ExecHandlerConfigurer {
        @Override
        public void configure(ExecHandleBuilder builder) {
            if (getExecutable() != null) {
                File exe = new File(getExecutable());
                if (exe.isAbsolute()) {
                    builder.executable(exe.getAbsolutePath());
                } else {
                    builder.executable(String.format("%s/%s", getWorkingDir().getAbsolutePath(), getExecutable()));
                }
            } else {
                builder.executable(String.format("%s/bin/gradle", getDistribution().getGradleHomeDir().getAbsolutePath()));
            }
        }
    }

    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider) {
        super(distribution, testDirectoryProvider);
    }

    public NoDaemonGradleExecuter(GradleDistribution distribution, TestDirectoryProvider testDirectoryProvider, GradleVersion gradleVersion, IntegrationTestBuildContext buildContext) {
        super(distribution, testDirectoryProvider, gradleVersion, buildContext);
    }

    private static String quote(String arg) {
        if(arg.isEmpty()){
            return "\"\"";
        }
        if (arg.contains(" ")) {
            return "\"" + arg + "\"";

        }
        return arg;
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