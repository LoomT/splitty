package utils;

import client.utils.IOInterface;

import java.io.*;

public class TestIO implements IOInterface {
    private final Writer writer;

    /**
     * @param writer writer with initial config data
     */
    public TestIO(Writer writer) {
        this.writer = writer;
    }

    /**
     * Returns a reader with the current buffer
     *
     * @return reader
     */
    @Override
    public Reader read() {
        return new StringReader(writer.toString());
    }

    /**
     * Flush the buffer and return an empty writer
     *
     * @return writer
     * @throws IOException if writer can not be flushed
     */
    @Override
    public Writer write() throws IOException {
        writer.flush();
        return writer;
    }

    /**
     * Returns the writer for testing purposes
     *
     * @return the writer
     */
    public Writer getWriter() {
        return writer;
    }
}