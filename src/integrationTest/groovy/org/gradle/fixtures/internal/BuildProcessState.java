package org.gradle.fixtures.internal;

import org.gradle.internal.agents.AgentStatus;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.concurrent.CompositeStoppable;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.ServiceRegistryBuilder;
import org.gradle.internal.service.scopes.GlobalScopeServices;
import org.gradle.internal.service.scopes.GradleUserHomeScopeServiceRegistry;
import org.gradle.internal.service.scopes.Scope;

import java.io.Closeable;

public class BuildProcessState implements Closeable {
    private final ServiceRegistry services;

    public BuildProcessState(
            final boolean longLiving,
            AgentStatus agentStatus,
            ClassPath additionalModuleClassPath,
            ServiceRegistry... parents
    ) {
        ServiceRegistryBuilder builder = ServiceRegistryBuilder.builder()
                .scope(Scope.Global.class)
                .displayName("Global services")
                .provider(new GlobalScopeServices(longLiving, agentStatus, additionalModuleClassPath))
                .provider(new BuildProcessScopeServices());
        for (ServiceRegistry parent : parents) {
            builder.parent(parent);
        }
        addProviders(builder);
        services = builder.build();
    }

    protected void addProviders(ServiceRegistryBuilder builder) {
    }

    public ServiceRegistry getServices() {
        return services;
    }

    @Override
    public void close() {
        // Force the user home services to be stopped first, because the dependencies between the user home services and the global services are not preserved currently
        CompositeStoppable.stoppable(services.get(GradleUserHomeScopeServiceRegistry.class), services).stop();
    }
}
