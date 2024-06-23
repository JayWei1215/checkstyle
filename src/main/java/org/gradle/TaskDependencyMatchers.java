package org.gradle;

import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.internal.impldep.org.hamcrest.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.gradle.internal.impldep.org.hamcrest.CoreMatchers.equalTo;


public class TaskDependencyMatchers {
    @Factory
    public static Matcher<Task> dependsOn(final String... tasks) {
        return dependsOn(equalTo(new HashSet<String>(Arrays.asList(tasks))));
    }

    @Factory
    public static Matcher<Task> dependsOn(Matcher<? extends Iterable<String>> matcher) {
        return dependsOn(matcher, false);
    }

    @Factory
    public static Matcher<Task> dependsOnPaths(Matcher<? extends Iterable<String>> matcher) {
        return dependsOn(matcher, true);
    }

    private static Matcher<Task> dependsOn(final Matcher<? extends Iterable<String>> matcher, final boolean matchOnPaths) {
        return new BaseMatcher<Task>() {
            @Override
            public boolean matches(Object o) {
                Task task = (Task) o;
                Set<String> names = new HashSet<String>();
                Set<? extends Task> depTasks = task.getTaskDependencies().getDependencies(task);
                for (Task depTask : depTasks) {
                    names.add(matchOnPaths ? depTask.getPath() : depTask.getName());
                }
                boolean matches = matcher.matches(names);
                if (!matches) {
                    StringDescription description = new StringDescription();
                    matcher.describeTo(description);
                    System.out.println(String.format("expected %s, got %s.", description.toString(), names));
                }
                return matches;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Task that depends on ").appendDescriptionOf(matcher);
            }
        };
    }

    @Factory
    public static <T extends Buildable> Matcher<T> builtBy(String... tasks) {
        return builtBy(equalTo(new HashSet<String>(Arrays.asList(tasks))));
    }

    @Factory
    public static <T extends Buildable> Matcher<T> builtBy(final Matcher<? extends Iterable<String>> matcher) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                Buildable task = (Buildable) o;
                Set<String> names = new HashSet<String>();
                Set<? extends Task> depTasks = task.getBuildDependencies().getDependencies(null);
                for (Task depTask : depTasks) {
                    names.add(depTask.getName());
                }
                boolean matches = matcher.matches(names);
                if (!matches) {
                    StringDescription description = new StringDescription();
                    matcher.describeTo(description);
                    System.out.println(String.format("expected %s, got %s.", description.toString(), names));
                }
                return matches;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a Buildable that is built by ").appendDescriptionOf(matcher);
            }
        };
    }
}
