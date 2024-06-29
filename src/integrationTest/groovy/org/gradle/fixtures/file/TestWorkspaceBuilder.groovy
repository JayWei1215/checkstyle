package org.gradle.fixtures.file

/**
 * Used in TestFile.create().
 *
 * Should be inner class of TestFile, but can't because Groovy has issues with inner classes as delegates.
 */
class TestWorkspaceBuilder {
    def TestFile baseDir

    def TestWorkspaceBuilder(TestFile baseDir) {
        this.baseDir = baseDir
    }

    def apply(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    TestFile dir(String name) {
        baseDir.file(name).createDir()
    }

    TestFile dir(String name, @DelegatesTo(value = TestWorkspaceBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> cl) {
        dir(name).create(cl)
    }

    TestFile file(String name) {
        baseDir.file(name).createFile()
    }

    TestFile link(String name, String target) {
        baseDir.file(name).createLink(target)
    }

    def setMode(int mode) {
        baseDir.mode = mode
    }

    def methodMissing(String name, Object args) {
        if (args.length == 1 && args[0] instanceof Closure) {
            baseDir.file(name).create(args[0])
        } else {
            throw new MissingMethodException(name, getClass(), args)
        }
    }
}