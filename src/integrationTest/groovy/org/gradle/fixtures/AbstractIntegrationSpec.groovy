package org.gradle.fixtures

import org.gradle.api.Action
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.problems.internal.DefaultProblemProgressDetails
import org.gradle.fixtures.dsl.GradleDsl
import org.gradle.fixtures.executer.ExecutionFailure
import org.gradle.fixtures.executer.GradleContextualExecuter
import org.gradle.fixtures.executer.GradleDistribution
import org.gradle.fixtures.executer.GradleExecuter
import org.gradle.fixtures.executer.IntegrationTestBuildContext
import org.gradle.fixtures.executer.UnderDevelopmentGradleDistribution
import org.gradle.fixtures.file.TestFile
import org.gradle.fixtures.problems.ReceivedProblem
import org.gradle.internal.impldep.org.junit.Rule

import org.gradle.util.internal.VersionNumber
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.opentest4j.AssertionFailedError
import spock.lang.Specification
import org.gradle.fixtures.file.TestNameTestDirectoryProvider

import java.nio.file.Files
import java.util.regex.Pattern

import static org.gradle.fixtures.dsl.GradleDsl.GROOVY

@SuppressWarnings("IntegrationTestFixtures")
abstract class AbstractIntegrationSpec extends Specification {
    private TestFile testDirOverride = null
    @Rule
    public final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

    private GradleExecuter executor
    private boolean ignoreCleanupAssertions

    GradleDistribution distribution = new UnderDevelopmentGradleDistribution(getBuildContext())
    private ExecutionResult currentResult
    private ExecutionFailure currentFailure
    private List<ReceivedProblem> receivedProblems

    static String mavenCentralRepository(GradleDsl dsl = GROOVY) {
        RepoScriptBlockUtil.mavenCentralRepository(dsl)
    }

    GradleExecuter getExecuter() {
        if (executor == null) {
            executor = createExecuter()
            if (ignoreCleanupAssertions) {
                executor.ignoreCleanupAssertions()
            }
        }
        return executor
    }

    GradleExecuter createExecuter() {
        new GradleContextualExecuter(distribution, temporaryFolder, getBuildContext())
    }

    TestFile getTestDirectory() {
        if (testDirOverride != null) {
            return testDirOverride
        }
        temporaryFolder.testDirectory
    }

    TestFile file(Object... path) {
        if (path.length == 1 && path[0] instanceof TestFile) {
            return path[0] as TestFile
        }
        getTestDirectory().file(path)
    }

    protected ExecutionResult succeeds(String... tasks) {
        resetProblemApiCheck()

        result = executer.withTasks(*tasks).run()
        return result
    }

    ExecutionResult getResult() {
        if (currentResult == null) {
            throw new IllegalStateException("No build result is available yet.")
        }
        return currentResult
    }

    def resetProblemApiCheck() {
        // By nulling out the receivedProblems, upon calling getReceivedProblems() we will re-fetch the problems from the build operations fixture.
        receivedProblems = null
    }

    void setResult(ExecutionResult result) {
        currentFailure = null
        currentResult = result
    }

    IntegrationTestBuildContext getBuildContext() {
        return IntegrationTestBuildContext.INSTANCE
    }
}
