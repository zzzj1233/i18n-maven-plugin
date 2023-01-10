package com.github.zzzj1233;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zzzj
 * @create 2023-01-10 15:36
 */
public interface PropertiesFile extends Closeable {

    void setProperty(String key, String value);

    void store() throws IOException;

}
