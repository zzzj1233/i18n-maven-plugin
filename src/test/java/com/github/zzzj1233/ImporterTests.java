package com.github.zzzj1233;

import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.EnumConstant;
import io.protostuff.compiler.parser.LocalFileReader;
import io.protostuff.compiler.parser.ProtoContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

/**
 * @author zzzj
 * @create 2023-01-09 18:19
 */
public class ImporterTests extends AbstractTest {

    private ProtoContext context;

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

        File file = getFileForClasspathResource("test.proto");

        this.context = importer.importFile(new LocalFileReader(file.toPath()), file.getAbsolutePath());
    }

    @Test
    public void localFileReaderTest() throws Exception {
        Assertions.assertNotNull(this.context.getProto().getEnums());
    }

    @Test
    public void readComment() throws Exception {
        Enum resultCode = context.getProto().getEnum("ResultCode");

        Assertions.assertNotNull(resultCode);

        List<EnumConstant> constants = resultCode.getConstants();

        Assertions.assertEquals(6, constants.size());

        EnumConstant enumConstant = constants.get(0);

        Assertions.assertEquals(0, enumConstant.getValue());

        Assertions.assertEquals(2, enumConstant.getCommentLines().size());

        Assertions.assertEquals("zh:成功", enumConstant.getCommentLines().get(0));

        Assertions.assertEquals("en:success", enumConstant.getCommentLines().get(1));
    }

}
