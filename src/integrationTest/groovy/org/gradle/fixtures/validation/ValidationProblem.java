package org.gradle.fixtures.validation;

import org.gradle.api.problems.Severity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ValidationProblem {
    Severity value() default Severity.WARNING;
}
