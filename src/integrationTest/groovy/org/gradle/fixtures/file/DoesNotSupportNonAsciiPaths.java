package org.gradle.fixtures.file;

import java.lang.annotation.*;

/**
 * Annotation to mark tests that don't support non-ascii characters on the path of the test directory.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DoesNotSupportNonAsciiPaths {
    String reason();
}
