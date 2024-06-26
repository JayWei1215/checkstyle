package org.gradle.fixtures.executer;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.regex.Pattern;

public abstract class ExpectedDeprecationWarning {

    private final int numLines;

    public ExpectedDeprecationWarning(int numLines) {
        this.numLines = numLines;
    }

    public static ExpectedDeprecationWarning withMessage(String message) {
        Preconditions.checkArgument(message != null && !message.isEmpty(), "message must not be null or empty");
        int numLines = message.split("\n").length;
        return new ExpectedDeprecationWarning(numLines) {
            @Override
            protected boolean matchesNextLines(String nextLines) {
                return message.equals(nextLines);
            }

            @Override
            public String toString() {
                return message;
            }
        };
    }

    public static ExpectedDeprecationWarning withSingleLinePattern(String pattern) {
        Preconditions.checkArgument(pattern != null && !pattern.isEmpty(), "pattern must not be null or empty");
        return withPattern(Pattern.compile(pattern), 1);
    }

    public static ExpectedDeprecationWarning withMultiLinePattern(String pattern, int numLines) {
        Preconditions.checkArgument(pattern != null && !pattern.isEmpty(), "pattern must not be null or empty");
        return withPattern(Pattern.compile("(?m)" + pattern), numLines);
    }

    private static ExpectedDeprecationWarning withPattern(Pattern pattern, int numLines) {
        return new ExpectedDeprecationWarning(numLines) {
            @Override
            protected boolean matchesNextLines(String nextLines) {
                return pattern.matcher(nextLines).matches();
            }

            @Override
            public String toString() {
                return pattern.toString();
            }
        };
    }

    /**
     * Get the number of lines that the expected message spans.
     *
     * @return the number of lines in this message
     */
    public int getNumLines() {
        return numLines;
    }

    /**
     * Check if the given lines, starting at the given index, match the expected message.
     *
     * @param lines the lines to check
     * @param startIndex the index of the first line to check
     * @return {@code true} if the lines match the expected message, {@code false} otherwise
     */
    public boolean matchesNextLines(List<String> lines, int startIndex) {
        String nextLines = numLines == 1
                ? lines.get(startIndex)
                : String.join("\n", lines.subList(startIndex, Math.min(startIndex + numLines, lines.size())));
        return matchesNextLines(nextLines);
    }

    protected abstract boolean matchesNextLines(String nextLines);
}
