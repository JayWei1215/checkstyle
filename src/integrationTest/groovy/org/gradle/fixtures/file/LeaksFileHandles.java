package org.gradle.fixtures.file;

import java.lang.annotation.*;

/**
 * Declares that the test holds files open and therefore not to error if the test workspace can't be cleaned up.
 *
 * @see AbstractTestDirectoryProvider
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface LeaksFileHandles {
    String value() default "";
}
