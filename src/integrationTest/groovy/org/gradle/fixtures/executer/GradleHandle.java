package org.gradle.fixtures.executer;

import java.io.PipedOutputStream;
import org.gradle.fixtures.ExecutionResult;

public interface GradleHandle {
    /**
     * Returns the stream for writing to stdin.
     */
    PipedOutputStream getStdinPipe();

    /**
     * Returns the stdout output currently received from the build. This is live.
     */
    String getStandardOutput();

    /**
     * Returns the stderr output currently received from the build. This is live.
     */
    String getErrorOutput();

    /**
     * Forcefully kills the build and returns immediately. Does not block until the build has finished.
     */
    GradleHandle abort();

    /**
     * Cancel a build that was started as a cancellable build by closing stdin.  Does not block until the build has finished.
     */
    GradleHandle cancel();

    /**
     * Cancel a build that was started as a cancellable build by sending EOT (ctrl-d).  Does not block until the build has finished.
     */
    GradleHandle cancelWithEOT();

    /**
     * Blocks until the build is complete and assert that the build completed successfully.
     */
    ExecutionResult waitForFinish();

    /**
     * Blocks until the build is complete and assert that the build completed with a failure.
     */
    ExecutionFailure waitForFailure();

    /**
     * Blocks until the build is complete and exits, disregarding the result.
     */
    void waitForExit();

    /**
     * Returns true if the build is currently running.
     */
    boolean isRunning();
}
