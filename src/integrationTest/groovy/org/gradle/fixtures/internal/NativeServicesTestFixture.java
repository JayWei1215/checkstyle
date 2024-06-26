package org.gradle.fixtures.internal;

import org.gradle.fixtures.file.TestFile;
import org.gradle.internal.nativeintegration.services.NativeServices;

import java.io.File;

public class NativeServicesTestFixture {
    // Collect this early, as the process' current directory can change during embedded test execution
    private static final TestFile TEST_DIR = new TestFile(new File(".").toURI());
    static NativeServices nativeServices;
    static boolean initialized;

    public static synchronized void initialize() {
        if (!initialized) {
            System.setProperty("org.gradle.native", "true");
            File nativeDir = getNativeServicesDir();
            NativeServices.initializeOnDaemon(nativeDir, NativeServices.NativeServicesMode.fromSystemProperties());
            initialized = true;
        }
    }

    public static synchronized NativeServices getInstance() {
        if (nativeServices == null) {
            initialize();
            nativeServices = NativeServices.getInstance();
        }
        return nativeServices;
    }

    public static File getNativeServicesDir() {
        return TEST_DIR.file("build/native-libs");
    }
}
