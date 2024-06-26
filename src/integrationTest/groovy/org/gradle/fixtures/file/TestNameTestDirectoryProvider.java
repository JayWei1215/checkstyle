package org.gradle.fixtures.file;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;

public class TestNameTestDirectoryProvider extends AbstractTestDirectoryProvider {
    public TestNameTestDirectoryProvider(Class<?> klass) {
        super(new TestFile(new File("build/tmp/" + determineTestDirectoryName(klass))), klass);
    }

    public TestNameTestDirectoryProvider(TestFile root, Class<?> klass) {
        super(root, klass);
    }

    private static String determineTestDirectoryName(Class<?> klass) {
        // NOTE: the space in the directory name is intentional to shake out problems with paths that contain spaces
        // NOTE: and so is the "s with circumflex" character (U+015D), to shake out problems with non-ASCII folder names
        return shouldUseNonAsciiPath(klass)
                ? "teŝt files"
                : "test files";
    }

    private static boolean shouldUseNonAsciiPath(Class<?> klass) {
        // TODO Remove this parameter and fix encoding problems on Windows, too
        return !OperatingSystem.current().isWindows()
                && !klass.isAnnotationPresent(DoesNotSupportNonAsciiPaths.class);
    }
}
