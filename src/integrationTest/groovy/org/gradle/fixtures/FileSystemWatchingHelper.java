package org.gradle.fixtures;

import org.gradle.initialization.StartParameterBuildOptions;

import static org.gradle.internal.service.scopes.VirtualFileSystemServices.VFS_DROP_PROPERTY;

public class FileSystemWatchingHelper {

    private static final int WAIT_FOR_CHANGES_PICKED_UP_MILLIS = 120;

    public static void waitForChangesToBePickedUp() throws InterruptedException {
        Thread.sleep(WAIT_FOR_CHANGES_PICKED_UP_MILLIS);
    }

    public static String getEnableFsWatchingArgument() {
        return booleanBuildOption(StartParameterBuildOptions.WatchFileSystemOption.LONG_OPTION, true);
    }

    public static String getDisableFsWatchingArgument() {
        return booleanBuildOption(StartParameterBuildOptions.WatchFileSystemOption.LONG_OPTION, false);
    }

    public static String getDropVfsArgument() {
        return getDropVfsArgument(true);
    }

    public static String getDropVfsArgument(boolean drop) {
        return systemProperty(VFS_DROP_PROPERTY.getSystemPropertyName(), drop);
    }

    public static String getVerboseVfsLoggingArgument() {
        return systemProperty(StartParameterBuildOptions.VfsVerboseLoggingOption.GRADLE_PROPERTY, true);
    }

    private static String systemProperty(String key, Object value) {
        return "-D" + key + "=" + value;
    }

    private static String booleanBuildOption(String optionName, boolean enabled) {
        return "--" + (enabled ? "" : "no-") + optionName;
    }
}
