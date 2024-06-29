package org.gradle.fixtures.daemon

import org.gradle.internal.logging.services.LoggingServiceRegistry
import org.gradle.internal.service.ServiceRegistryBuilder
import org.gradle.internal.service.scopes.BasicGlobalScopeServices
import org.gradle.launcher.daemon.client.DaemonClientGlobalServices
import org.gradle.launcher.daemon.registry.DaemonRegistry
import org.gradle.launcher.daemon.registry.DaemonRegistryServices
import org.gradle.util.GradleVersion

class DaemonLogsAnalyzer implements DaemonsFixture {
    private final File daemonLogsDir
    private final File daemonBaseDir
    private final DaemonRegistry registry
    private final String version

    DaemonLogsAnalyzer(File daemonBaseDir, String version = GradleVersion.current().version) {
        this.version = version
        this.daemonBaseDir = daemonBaseDir
        daemonLogsDir = new File(daemonBaseDir, version)
        def services = ServiceRegistryBuilder.builder()
                .parent(LoggingServiceRegistry.newEmbeddableLogging())
                .parent(NativeServicesTestFixture.getInstance())
                .provider(new BasicGlobalScopeServices())
                .provider(new DaemonClientGlobalServices())
                .provider(new DaemonRegistryServices(daemonBaseDir))
                .build()
        registry = services.get(DaemonRegistry)
    }

    static DaemonsFixture newAnalyzer(File daemonBaseDir, String version = GradleVersion.current().version) {
        return new DaemonLogsAnalyzer(daemonBaseDir, version)
    }

    DaemonRegistry getRegistry() {
        return registry
    }

    void killAll() {
        allDaemons*.kill()
    }


    List<DaemonFixture> getDaemons() {
        getAllDaemons().findAll { !daemonStoppedWithSocketExceptionOnWindows(it) || it.logContains("Starting build in new daemon") }
    }

    List<DaemonFixture> getAllDaemons() {
        if (!daemonLogsDir.exists() || !daemonLogsDir.isDirectory()) {
            return []
        }
        return daemonLogsDir.listFiles().findAll { it.name.endsWith('.log') && !it.name.startsWith('hs_err') }.collect { daemonForLogFile(it) }
    }

//    List<DaemonFixture> getVisible() {
//        return registry.all.collect { daemonForLogFile(new File(daemonLogsDir, "daemon-${it.pid}.out.log")) }
//    }

//    DaemonFixture daemonForLogFile(File logFile) {
//        if (version == GradleVersion.current().version) {
//            return new TestableDaemon(logFile, registry, GradleVersion.version(version))
//        }
//        return new LegacyDaemon(logFile, GradleVersion.version(version))
//    }

    DaemonFixture getDaemon() {
        def daemons = getDaemons()
        assert daemons.size() == 1
        daemons[0]
    }

    File getDaemonBaseDir() {
        return daemonBaseDir
    }

    String getVersion() {
        return version
    }

    void assertNoCrashedDaemon() {
        List<File> crashLogs = findCrashLogs(daemonLogsDir)
        crashLogs.each { println(it.text) }
        assert crashLogs.empty: "Found crash logs: ${crashLogs}"
    }

    static List<File> findCrashLogs(File dir) {
        dir.listFiles()?.findAll { it.name.endsWith('.log') && it.name.startsWith('hs_err') } ?: []
    }
}
