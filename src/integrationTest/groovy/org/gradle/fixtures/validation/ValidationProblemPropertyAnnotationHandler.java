package org.gradle.fixtures.validation;

import org.gradle.api.problems.Severity;
import org.gradle.api.problems.internal.DefaultProblemGroup;
import org.gradle.internal.deprecation.Documentation;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet;
import org.gradle.internal.properties.PropertyValue;
import org.gradle.internal.properties.PropertyVisitor;
import org.gradle.internal.properties.annotations.AbstractPropertyAnnotationHandler;
import org.gradle.internal.properties.annotations.PropertyMetadata;
import org.gradle.internal.reflect.annotations.AnnotationCategory;
import org.gradle.internal.reflect.validation.TypeValidationContext;

class ValidationProblemPropertyAnnotationHandler extends AbstractPropertyAnnotationHandler {
    public ValidationProblemPropertyAnnotationHandler() {
        super(ValidationProblem.class, Kind.OTHER, ImmutableSet.of());
    }

    @Override
    public boolean isPropertyRelevant() {
        return true;
    }

    @Override
    public void visitPropertyValue(String propertyName, PropertyValue value, PropertyMetadata propertyMetadata, PropertyVisitor visitor) {
    }

    @Override
    public void validatePropertyMetadata(PropertyMetadata propertyMetadata, TypeValidationContext validationContext) {
        validationContext.visitPropertyProblem(problem ->
                problem
                        .forProperty(propertyMetadata.getPropertyName())
                        .id("test-problem", "test problem", new DefaultProblemGroup("root", "root"))
                        .documentedAt(Documentation.userManual("id", "section"))
                        .severity(annotationValue(propertyMetadata))
                        .details("this is a test")
        );
    }

    private Severity annotationValue(PropertyMetadata propertyMetadata) {
        return propertyMetadata.getAnnotationForCategory(AnnotationCategory.TYPE)
                .map(ValidationProblem.class::cast)
                .map(ValidationProblem::value)
                .orElse(Severity.WARNING);
    }
}
