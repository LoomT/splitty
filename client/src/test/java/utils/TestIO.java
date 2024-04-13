package utils;

import client.utils.IOInterface;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class TestIO implements IOInterface {
    private StringBuffer buffer;
    private int writes;

    /**
     * Constructs a test IO instance to mock a file IO
     *
     * @param content initial buffer content
     */
    public TestIO(String content) {
        buffer = new StringBuffer(content);
        writes = 0;
    }

    /**
     * Returns a reader with the current buffer
     *
     * @return reader
     */
    @Override
    public Reader read() {
        return new StringReader(buffer.toString());
    }

    /**
     * Flush the buffer and return an empty writer
     *
     * @return writer
     */
    @Override
    public Writer write() {
        writes++;
        StringWriter writer = new StringWriter();
        buffer = writer.getBuffer();
        return writer;
    }

    /**
     * Returns the content of the buffer for testing purposes
     *
     * @return the content of the buffer
     */
    public String getContent() {
        return buffer.toString();
    }


    /**
     * @return number of writes
     */
    public int getWrites() {return writes;}
}
