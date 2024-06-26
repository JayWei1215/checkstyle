package org.gradle.fixtures.executer;

import org.gradle.internal.UncheckedException;
import org.gradle.internal.impldep.org.apache.commons.io.output.CloseShieldOutputStream;
import org.gradle.internal.impldep.org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class OutputCapturer {
    private final ByteArrayOutputStream buffer;
    private final OutputStream outputStream;
    private final String outputEncoding;

    public OutputCapturer(OutputStream standardStream, String outputEncoding) {
        this.buffer = new ByteArrayOutputStream();
        this.outputStream = new CloseShieldOutputStream(new TeeOutputStream(standardStream, buffer));
        this.outputEncoding = outputEncoding;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getOutputAsString() {
        try {
            return buffer.toString(outputEncoding);
        } catch (UnsupportedEncodingException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }
}
