package org.gradle.fixtures.executer

import org.gradle.fixtures.logging.ArtifactResolutionOmittingOutputNormalizer
import org.gradle.internal.SystemProperties
import org.gradle.util.internal.TextUtil

class SequentialOutputMatcher {
    private static final String NL = SystemProperties.instance.lineSeparator

    public void assertOutputMatches(String expected, String actual, boolean ignoreExtraLines) {
        List actualLines = new ArtifactResolutionOmittingOutputNormalizer().normalize(actual).readLines().findAll { !it.isEmpty() }
        List expectedLines = expected.readLines().findAll { !it.isEmpty() }
        assertOutputLinesMatch(expectedLines, actualLines, ignoreExtraLines, actual)
    }

    protected void assertOutputLinesMatch(List<String> expectedLines, List<String> actualLines, boolean ignoreExtraLines, String actual) {
        int pos = 0
        for (; pos < actualLines.size() && pos < expectedLines.size(); pos++) {
            String expectedLine = expectedLines[pos]
            String actualLine = actualLines[pos]
            boolean matches = compare(expectedLine, actualLine)
            if (!matches) {
                if (expectedLine.contains(actualLine)) {
                    Assert.fail("Missing text at line ${pos + 1}.${NL}Expected: ${expectedLine}${NL}Actual: ${actualLine}${NL}---${NL}Actual output:${NL}$actual${NL}---")
                }
                if (actualLine.contains(expectedLine)) {
                    Assert.fail("Extra text at line ${pos + 1}.${NL}Expected: ${expectedLine}${NL}Actual: ${actualLine}${NL}---${NL}Actual output:${NL}$actual${NL}---")
                }
                Assert.fail("Unexpected value at line ${pos + 1}.${NL}Expected: ${expectedLine}${NL}Actual: ${actualLine}${NL}---${NL}Actual output:${NL}$actual${NL}---")
            }
        }
        if (pos == actualLines.size() && pos < expectedLines.size()) {
            Assert.fail("Lines missing from actual result, starting at line ${pos + 1}.${NL}Expected: ${expectedLines[pos]}${NL}Actual output:${NL}$actual${NL}---")
        }
        if (!ignoreExtraLines && pos < actualLines.size() && pos == expectedLines.size()) {
            Assert.fail("Extra lines in actual result, starting at line ${pos + 1}.${NL}Actual: ${actualLines[pos]}${NL}Actual output:${NL}$actual${NL}---")
        }
    }

    protected boolean compare(String expected, String actual) {
        if (actual == expected) {
            return true
        }

        if (expected == 'Total time: 1 secs') {
            return actual.matches('Total time: .+ secs')
        }

        // Normalise default object toString() values
        actual = actual.replaceAll('(\\w+(\\.\\w+)*)@\\p{XDigit}+', '$1@12345')
        // Normalise file separators
        actual = TextUtil.normaliseFileSeparators(actual)

        return actual == expected
    }
}
