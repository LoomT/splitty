package client.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface IOInterface {
    /**
     * @return a reader
     * @throws FileNotFoundException if file can not be read
     */
    Reader read() throws FileNotFoundException;

    /**
     * @return a writer
     * @throws IOException if file can not be opened for some reason
     */
    Writer write() throws IOException;
}
