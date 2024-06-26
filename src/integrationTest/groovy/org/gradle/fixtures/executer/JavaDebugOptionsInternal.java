package org.gradle.fixtures.executer;

import org.gradle.process.internal.JvmOptions;

import static com.google.common.base.Strings.isNullOrEmpty;

public class JavaDebugOptionsInternal {
    private boolean enabled = false;
    private String host = "";
    private int port = 5005;
    private boolean server = true;
    private boolean suspend = true;

    public JavaDebugOptionsInternal(boolean enabled) {
        this.enabled = enabled;
    }

    public JavaDebugOptionsInternal() {
    }

    public String toDebugArgument() {
        return JvmOptions.getDebugArgument(server, suspend, getAddress());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isServer() {
        return server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    public String getAddress(){
        String port = Integer.toString(this.port);
        return isNullOrEmpty(host) ? port : host + ":" + port;
    }

    public void copyTo(JavaDebugOptionsInternal opts) {
        opts.enabled = enabled;
        opts.suspend = suspend;
        opts.server = server;
        opts.port = port;
        opts.host = host;
    }
}
