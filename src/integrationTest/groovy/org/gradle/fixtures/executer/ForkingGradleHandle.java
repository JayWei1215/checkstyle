package org.gradle.fixtures.executer;

import com.google.common.base.Joiner;
import org.gradle.api.Action;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCachesProvider;
import org.gradle.fixtures.ExecutionResult;
import org.gradle.fixtures.file.TestFile;
import org.gradle.internal.Factory;
import org.gradle.internal.io.NullOutputStream;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.process.internal.ExecHandle;
import org.gradle.process.internal.ExecHandleState;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static org.gradle.util.internal.TextUtil.getPlatformLineSeparator;

class ForkingGradleHandle extends OutputScrapingGradleHandle {

    final private Factory<? extends AbstractExecHandleBuilder> execHandleFactory;

    private final OutputCapturer standardOutputCapturer;
    private final OutputCapturer errorOutputCapturer;
    private final Action<ExecutionResult> resultAssertion;
    private final PipedOutputStream stdinPipe;
    private final boolean isDaemon;

    private final DurationMeasurement durationMeasurement;
    private final AtomicReference<ExecHandle> execHandleRef = new AtomicReference<>();

    public ForkingGradleHandle(PipedOutputStream stdinPipe, boolean isDaemon, Action<ExecutionResult> resultAssertion, String outputEncoding, Factory<? extends AbstractExecHandleBuilder> execHandleFactory, DurationMeasurement durationMeasurement) {
        this.resultAssertion = resultAssertion;
        this.execHandleFactory = execHandleFactory;
        this.isDaemon = isDaemon;
        this.stdinPipe = stdinPipe;
        this.durationMeasurement = durationMeasurement;
        this.standardOutputCapturer = outputCapturerFor(System.out, outputEncoding, durationMeasurement);
        this.errorOutputCapturer = outputCapturerFor(System.err, outputEncoding, durationMeasurement);
    }

    private static OutputCapturer outputCapturerFor(PrintStream stream, String outputEncoding, DurationMeasurement durationMeasurement) {
        return new OutputCapturer(durationMeasurement == null ? stream : NullOutputStream.INSTANCE, outputEncoding);
    }

    public GradleHandle start() {
        ExecHandle execHandle = buildExecHandle();
        if (this.execHandleRef.getAndSet(execHandle) != null) {
            throw new IllegalStateException("you have already called start() on this handle");
        }

        checkDistributionExists();
        printExecHandleSettings();

        execHandle.start();
        if (durationMeasurement != null) {
            durationMeasurement.start();
        }
        return this;
    }

    private void checkDistributionExists() {
        //noinspection ResultOfMethodCallIgnored
        new TestFile(getExecHandle().getCommand()).getParentFile().assertIsDir();
    }

    private ExecHandle buildExecHandle() {
        AbstractExecHandleBuilder builder = execHandleFactory.create();
        assert builder != null;
        return builder
                .setStandardOutput(standardOutputCapturer.getOutputStream())
                .setErrorOutput(errorOutputCapturer.getOutputStream())
                .build();
    }

    private void printExecHandleSettings() {
        ExecHandle execHandle = getExecHandle();
        Map<String, String> environment = execHandle.getEnvironment();
        println("Starting build with: " + execHandle.getCommand() + " " + Joiner.on(" ").join(execHandle.getArguments()));
        println("Working directory: " + execHandle.getDirectory());
        println("Environment vars:");
        println(format("    JAVA_HOME: %s", environment.get("JAVA_HOME")));
        println(format("    GRADLE_HOME: %s", environment.get("GRADLE_HOME")));
        println(format("    GRADLE_USER_HOME: %s", environment.get("GRADLE_USER_HOME")));
        println(format("    JAVA_OPTS: %s", environment.get("JAVA_OPTS")));
        println(format("    GRADLE_OPTS: %s", environment.get("GRADLE_OPTS")));
        String roCache = environment.get(ArtifactCachesProvider.READONLY_CACHE_ENV_VAR);
        if (roCache != null) {
            println(format("    %s: %s", ArtifactCachesProvider.READONLY_CACHE_ENV_VAR, roCache));
        }
    }

    private static void println(String s) {
        System.out.println(s);
    }

    private ExecHandle getExecHandle() {
        ExecHandle handle = execHandleRef.get();
        if (handle == null) {
            throw new IllegalStateException("you must call start() before calling this method");
        }
        return handle;
    }
}
