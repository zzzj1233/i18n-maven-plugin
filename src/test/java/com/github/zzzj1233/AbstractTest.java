package com.github.zzzj1233;

import com.google.inject.Guice;
import io.protostuff.compiler.ParserModule;
import io.protostuff.compiler.parser.Importer;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;

/**
 * @author zzzj
 * @create 2023-01-09 17:18
 */
public class AbstractTest {

    protected Importer importer;

    @BeforeEach
    void setUp() throws Exception {
        this.importer = Guice.createInjector(new ParserModule()).getInstance(Importer.class);
    }

    protected static File getFileForClasspathResource(String resource)
            throws FileNotFoundException {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        URL resourceUrl = cloader.getResource(resource);

        if (resourceUrl == null) {
            throw new FileNotFoundException("Unable to find: " + resource);
        }

        return new File(URI.create(resourceUrl.toString().replaceAll(" ", "%20")));
    }

}
