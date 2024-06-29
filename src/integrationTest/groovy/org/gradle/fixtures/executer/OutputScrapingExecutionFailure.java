package org.gradle.fixtures.executer;

import org.gradle.internal.Pair;
import org.gradle.util.internal.TextUtil;
import org.hamcrest.Matcher;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static org.gradle.internal.impldep.org.apache.commons.lang.StringUtils.repeat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class OutputScrapingExecutionFailure extends OutputScrapingExecutionResult implements ExecutionFailure {
    private static final Pattern FAILURE_PATTERN = Pattern.compile("FAILURE: (.+)");
    private static final Pattern CAUSE_PATTERN = Pattern.compile("(?m)(^\\s*> )");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("(?ms)^\\* What went wrong:$(.+?)^\\* Try:$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?ms)^\\* Where:((.+?)'.+?') line: (\\d+)$");
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("(?ms)^\\* Try:$\\n(.+?)\\n(?:Exception is:)?$");
    private final String summary;
    private final List<Problem> problems = new ArrayList<>();
    private final List<Problem> problemsNotChecked = new ArrayList<>();
    private final List<String> lineNumbers = new ArrayList<>();
    private final List<String> fileNames = new ArrayList<>();
    private final String resolution;
    private final LogContent mainContent;

    static boolean hasFailure(String error) {
        return FAILURE_PATTERN.matcher(error).find();
    }

    /**
     * Creates a result from the output of a <em>single</em> Gradle invocation.
     *
     * @param output The raw build stdout chars.
     * @param error The raw build stderr chars.
     * @return A {@link OutputScrapingExecutionResult} for a successful build, or a {@link OutputScrapingExecutionFailure} for a failed build.
     */
    public static OutputScrapingExecutionFailure from(String output, String error) {
        return new OutputScrapingExecutionFailure(output, error, true);
    }

    protected OutputScrapingExecutionFailure(String output, String error, boolean includeBuildSrc) {
        super(LogContent.of(output), LogContent.of(error), includeBuildSrc);

        LogContent withoutDebug = LogContent.of(output).ansiCharsToPlainText().removeDebugPrefix();

        // Find failure section
        Pair<LogContent, LogContent> match = withoutDebug.splitOnFirstMatchingLine(FAILURE_PATTERN);
        if (match == null) {
            // Not present in output, check error output.
            match = LogContent.of(error).ansiCharsToPlainText().removeDebugPrefix().splitOnFirstMatchingLine(FAILURE_PATTERN);
            if (match != null) {
                match = Pair.of(withoutDebug, match.getRight());
            } else {
                // Not present, assume no failure details
                match = Pair.of(withoutDebug, LogContent.empty());
            }
        } else {
            if (match.getRight().countMatches(FAILURE_PATTERN) != 1) {
                throw new IllegalArgumentException("Found multiple failure sections in log output: " + output);
            }
        }

        LogContent failureContent = match.getRight();
        this.mainContent = match.getLeft();

        String failureText = failureContent.withNormalizedEol();

        java.util.regex.Matcher matcher = FAILURE_PATTERN.matcher(failureText);
        if (matcher.lookingAt()) {
            summary = matcher.group(1);
        } else {
            summary = "";
        }

        matcher = LOCATION_PATTERN.matcher(failureText);
        while (matcher.find()) {
            fileNames.add(matcher.group(1).trim());
            lineNumbers.add(matcher.group(3));
        }

        matcher = DESCRIPTION_PATTERN.matcher(failureText);
        while (matcher.find()) {
            String problemStr = matcher.group(1);
            Problem problem = extract(problemStr);
            problems.add(problem);
            problemsNotChecked.add(problem);
        }

        matcher = RESOLUTION_PATTERN.matcher(failureText);
        if (!matcher.find()) {
            resolution = "";
        } else {
            resolution = matcher.group(1).trim();
        }
    }

    @Override
    public ExecutionFailure getIgnoreBuildSrc() {
        return new OutputScrapingExecutionFailure(getOutput(), getError(), false);
    }

    @Override
    public LogContent getMainContent() {
        return mainContent;
    }

    private Problem extract(String problem) {
        java.util.regex.Matcher matcher = CAUSE_PATTERN.matcher(problem);
        String description;
        List<String> causes = new ArrayList<>();
        if (!matcher.find()) {
            description = TextUtil.normaliseLineSeparators(problem.trim());
        } else {
            description = TextUtil.normaliseLineSeparators(problem.substring(0, matcher.start()).trim());
            while (true) {
                int pos = matcher.end();
                int prefix = matcher.group(1).length();
                String prefixPattern = toPrefixPattern(prefix);
                if (matcher.find(pos)) {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos, matcher.start()).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                } else {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                    break;
                }
            }
        }
        return new Problem(normalizeLambdaIds(description), causes);
    }

    private String toPrefixPattern(int prefix) {
        return "(?m)^" + repeat(" ", prefix);
    }

    @Override
    public ExecutionFailure assertHasLineNumber(int lineNumber) {
        assertThat(this.lineNumbers, hasItem(equalTo(String.valueOf(lineNumber))));
        return this;
    }

    @Override
    public ExecutionFailure assertHasFileName(String filename) {
        assertThat(this.fileNames, hasItem(equalTo(filename)));
        return this;
    }

    private void assertHasFailure(Matcher<? super String> matcher, Consumer<? super Failure> action) {
        Set<String> seen = new LinkedHashSet<>();
        for (Problem problem : problems) {
            if (matcher.matches(problem.description)) {
                problemsNotChecked.remove(problem);
                action.accept(problem);
                return;
            }
            seen.add(problem.description);
        }
        failureOnUnexpectedOutput(String.format("No matching failure description found\nExpected: A failure description which is %s\n     but: failure descriptions were %s", matcher, seen));
    }

    private static class Problem extends AbstractFailure {
        private Problem(String description, List<String> causes) {
            super(description, causes);
        }
    }

}
