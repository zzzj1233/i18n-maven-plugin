package com.github.zzzj1233;

import java.io.File;
import java.io.IOException;

/**
 * @author zzzj
 * @create 2023-01-10 15:42
 */
public class PropertiesFileFactory {

    public static PropertiesFile newInstance(File file, boolean overlap, boolean clear) throws IOException {
        if (file == null) {
            return new NopPropertiesFile();
        } else {
            return new PropertiesFileImpl(file, overlap, clear);
        }
    }

}
