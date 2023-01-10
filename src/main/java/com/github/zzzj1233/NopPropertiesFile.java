package com.github.zzzj1233;

import java.io.IOException;

/**
 * @author zzzj
 * @create 2023-01-10 15:36
 */
public class NopPropertiesFile implements PropertiesFile {

    @Override
    public void setProperty(String key, String value) {

    }

    @Override
    public void store() throws IOException {

    }

    @Override
    public void close()  {

    }

}
