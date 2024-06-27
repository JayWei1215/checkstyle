package org.gradle.fixtures.validation;

import com.google.common.collect.ImmutableList;
import org.gradle.fixtures.internal.ServiceRegistrationProvider;
import org.gradle.internal.properties.annotations.PropertyAnnotationHandler;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.scopes.ExecutionGlobalServices;
import org.gradle.fixtures.internal.Provides;

public class ValidationServicesFixture {
    public static ServiceRegistry getServices() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.addProvider(new ServiceRegistrationProvider() {
            @Provides
            ExecutionGlobalServices.AnnotationHandlerRegistration createAnnotationRegistration() {
                return () -> ImmutableList.of(ValidationProblem.class);
            }

            @Provides
            PropertyAnnotationHandler createValidationProblemAnnotationHandler() {
                return new ValidationProblemPropertyAnnotationHandler();
            }
        });
        return registry;
    }
}
