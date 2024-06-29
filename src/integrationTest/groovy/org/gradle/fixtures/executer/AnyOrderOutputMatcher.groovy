package org.gradle.fixtures.executer

import org.gradle.internal.SystemProperties
import org.junit.Assert

class AnyOrderOutputMatcher extends SequentialOutputMatcher {
    private static final String NL = SystemProperties.instance.lineSeparator

    protected void assertOutputLinesMatch(List<String> expectedLines, List<String> actualLines, boolean ignoreExtraLines, String actual) {
        List<String> unmatchedLines = new ArrayList<String>(actualLines)
        expectedLines.removeAll('')
        unmatchedLines.removeAll('')

        expectedLines.each { expectedLine ->
            def matchedLine = unmatchedLines.find { actualLine ->
                compare(expectedLine, actualLine)
            }
            if (matchedLine) {
                unmatchedLines.remove(matchedLine)
            } else {
                Assert.fail("Line missing from output.${NL}${expectedLine}${NL}---${NL}Actual output:${NL}$actual${NL}---")
            }
        }

        if (!(ignoreExtraLines || unmatchedLines.empty)) {
            def unmatched = unmatchedLines.join(NL)
            Assert.fail("Extra lines in output.${NL}${unmatched}${NL}---${NL}Actual output:${NL}$actual${NL}---")
        }
    }
}
