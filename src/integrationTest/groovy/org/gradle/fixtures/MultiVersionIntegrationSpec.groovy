package org.gradle.fixtures

import org.gradle.util.internal.VersionNumber

import javax.annotation.Nullable

/**
 * See {@link org.gradle.integtests.fixtures.compatibility.AbstractContextualMultiVersionTestInterceptor} for information on running these tests.
 */
abstract class MultiVersionIntegrationSpec extends AbstractIntegrationSpec {
    static final def CLASSIFIER_PATTERN = /^(.*?)(:.*)?$/

    static def version

    static VersionNumber getVersionNumber() {
        if (version == null) {
            throw new IllegalStateException("No version present")
        }
        def m = version.toString() =~ CLASSIFIER_PATTERN
        VersionNumber.parse(m[0][1])
    }

    @Nullable
    static String getVersionClassifier() {
        if (version == null) {
            throw new IllegalStateException("No version present")
        }
        def m = version.toString() =~ CLASSIFIER_PATTERN
        return m[0][2]
    }
}
