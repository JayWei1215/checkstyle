package org.gradle.fixtures.internal;

import com.google.errorprone.annotations.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to register service factory methods on a {@link ServiceRegistrationProvider}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Keep
public @interface Provides {
}
