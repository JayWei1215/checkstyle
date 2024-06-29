package org.gradle.fixtures.file

class ExecOutput {
    ExecOutput(int exitCode, String rawOutput, String error) {
        this.exitCode = exitCode
        this.rawOutput = rawOutput
        this.out = rawOutput.replaceAll("\r\n|\r", "\n")
        this.error = error
    }

    int exitCode
    String rawOutput
    String out
    String error
}
