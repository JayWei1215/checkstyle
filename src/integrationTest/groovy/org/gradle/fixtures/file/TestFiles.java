package org.gradle.fixtures.file;

import org.gradle.api.internal.file.DefaultFileLookup;
import org.gradle.internal.file.PathToFileResolver;

public class TestFiles {
    private static final DefaultFileLookup FILE_LOOKUP = new DefaultFileLookup();
    /**
     * Returns a resolver with no base directory.
     */
    public static PathToFileResolver pathToFileResolver() {

        return FILE_LOOKUP.getPathToFileResolver();
    }
}
