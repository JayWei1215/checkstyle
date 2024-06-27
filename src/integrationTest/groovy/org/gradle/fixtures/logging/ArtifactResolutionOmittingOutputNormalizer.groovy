package org.gradle.fixtures.logging

import groovy.transform.CompileStatic
import org.gradle.exemplar.executor.ExecutionMetadata
import org.gradle.exemplar.test.normalizer.OutputNormalizer
import org.gradle.internal.jvm.Jvm

import javax.annotation.Nullable

@CompileStatic
class ArtifactResolutionOmittingOutputNormalizer implements OutputNormalizer {
    @Override
    String normalize(String commandOutput, @Nullable ExecutionMetadata executionMetadata) {
        List<String> lines = commandOutput.readLines()
        if (lines.empty) {
            return ""
        }
        boolean seenWarning = false
        List<String> result = new ArrayList<String>()
        for (String line : lines) {
            if (line.matches('Download .+')) {
                // ignore
            } else if (!seenWarning && !Jvm.current().javaVersion.java7Compatible && line == 'Support for reading or changing file permissions is only available on this platform using Java 7 or later.') {
                // ignore this warning once only on java < 7
                seenWarning = true
            } else {
                result << line
            }
        }
        return result.join("\n")
    }

    String normalize(String output) {
        return normalize(output, null)
    }
}
