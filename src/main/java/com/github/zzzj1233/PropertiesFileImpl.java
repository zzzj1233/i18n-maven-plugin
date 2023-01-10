package com.github.zzzj1233;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author zzzj
 * @create 2023-01-10 15:39
 */
public class PropertiesFileImpl implements PropertiesFile {

    private final File file;

    private final Properties properties;

    private final boolean overlap;

    private final boolean clear;

    private boolean anyModify;

    private FileOutputStream fileOutputStream;

    private FileInputStream fileInputStream;

    public PropertiesFileImpl(File file, boolean overlap, boolean clear) throws IOException {
        this.file = file;

        this.properties = new Properties();

        this.clear = clear;

        if (file.exists()) {
            this.fileInputStream = new FileInputStream(file);

            this.properties.load(new FileInputStream(file));
        }

        this.overlap = overlap;
    }

    @Override
    public void setProperty(String key, String value) {
        if (!this.overlap && this.properties.containsKey(key)) {
            return;
        }

        anyModify = true;

        this.properties.setProperty(key, value);
    }

    @Override
    public void store() throws IOException {
        if (anyModify) {
            this.fileOutputStream = new FileOutputStream(file, !clear);
            this.properties.store(fileOutputStream, null);
        }
    }

    @Override
    public void close() {
        try {
            if (this.fileInputStream != null) {
                this.fileInputStream.close();
            }
        } catch (IOException ignore) {

        }
        try {
            if (this.fileOutputStream != null) {
                this.fileOutputStream.close();
            }
        } catch (IOException ignore) {

        }
    }
}
