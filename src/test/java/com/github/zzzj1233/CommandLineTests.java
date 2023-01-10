package com.github.zzzj1233;

import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author zzzj
 * @create 2023-01-09 16:42
 */
public class CommandLineTests extends AbstractTest {

    private ProtoI18NMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new ProtoI18NMojo();
    }

    @Test
    public void usage() throws Exception {
        Commandline commandline = new Commandline("protoc --help");

        StringBuilder output = new StringBuilder();

        CommandLineUtils.executeCommandLine(commandline, output::append, line -> {
        });

        Assertions.assertFalse(output.toString().isEmpty());
    }

    @Test
    public void generation() throws Exception {
        String path = getFileForClasspathResource("test.proto").getAbsolutePath();

        String outputFileName = mojo.executeProtoCommand(path);

        Assertions.assertEquals("test.proto.bin", outputFileName);

        File file = new File(outputFileName);

        Assertions.assertTrue(file.exists());

        file.deleteOnExit();
    }


}
