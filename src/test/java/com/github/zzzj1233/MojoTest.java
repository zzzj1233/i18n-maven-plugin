package com.github.zzzj1233;

import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author zzzj
 * @create 2023-01-09 19:09
 */
public class MojoTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        ProtoI18NMojo mojo = new ProtoI18NMojo();
        mojo.setProtoFile(getFileForClasspathResource("test.proto"));
        mojo.setEnumName("ResultCode");
        mojo.setI18nKeyPrefix("RESULT_CODE_");
        mojo.setClearI18NFile(true);
        mojo.setI18nZHCN(new File("i18n.zhcn.properties"));
        mojo.setI18nENUS(new File("i18n.enus.properties"));
        mojo.setI18nZHTC(new File("i18n.zhtc.properties"));
        mojo.setSkip(false);
        mojo.setTranslation(true);
        // mojo.setCharsetName("unicode");
        mojo.setZHCNCommentPrefix("zh");
        mojo.setZHTCCommentPrefix("zhtc");
        mojo.setENUSCommentPrefix("en");
        mojo.setCommentSeparator(":");

        mojo.execute();

    }


}
