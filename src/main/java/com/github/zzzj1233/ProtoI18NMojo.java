package com.github.zzzj1233;

import com.google.inject.Guice;
import io.protostuff.compiler.ParserModule;
import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.EnumConstant;
import io.protostuff.compiler.parser.Importer;
import io.protostuff.compiler.parser.LocalFileReader;
import io.protostuff.compiler.parser.ProtoContext;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzj
 * @create 2023-01-09 15:25
 */
@Mojo(name = "proto-i18N", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class ProtoI18NMojo extends AbstractMojo {

    /**
     * proto文件路径
     */
    @Parameter(
            property = "proto.path",
            required = true
    )
    private File protoFile;


    /**
     * proto文件enum的类名
     */
    @Parameter(
            property = "enum.Name",
            defaultValue = "ResultCode"
    )
    private String enumName;

    /**
     * proto文件enum的类名
     */
    @Parameter(
            property = "i18n.key.prefix",
            defaultValue = "RESULT_CODE_"
    )
    private String i18nKeyPrefix;

    /**
     * 是否在执行之前清空i18n文件
     */
    @Parameter(
            property = "i18n.clear",
            defaultValue = "true"
    )
    private Boolean clear;

    /**
     * 中文的i18n文件
     * <p>
     * 转换后的中文将会追加到这个文件中
     */
    @Parameter(
            property = "i18n.zh-cn"
    )
    private File i18nZHCN;

    /**
     * 英文的i18n文件
     * <p>
     * 转换后的英文将会追加到这个文件中
     */
    @Parameter(
            property = "i18n.en-us"
    )
    private File i18nENUS;

    /**
     * 繁体中文的i18n文件
     * <p>
     * 转换后的繁体中文将会追加到这个文件中
     */
    @Parameter(
            property = "i18n.zh-tc"
    )
    private File i18nZHTC;

    /**
     * 是否跳过, 既不执行
     */
    @Parameter(
            property = "skip",
            defaultValue = "false"
    )
    private Boolean skip;

    /**
     * 如果未提供繁体中文时, 是否自动将简体中文转换为繁体中文
     */
    @Parameter(
            property = "translation",
            defaultValue = "true"
    )
    private Boolean translation;

    /**
     * 写入文件时的编码方式
     */
    // @Parameter(
    //        property = "charset",
    //        defaultValue = "unicode"
    // )
    // private String charsetName;

    /**
     * 简体中文注释前缀
     * <p>
     * e.g. {ZHCNCommentPrefix}{commentSeparator}i18n简体中文内容
     */
    @Parameter(
            property = "comment.zhcn.prefix",
            defaultValue = "zh"
    )
    private String ZHCNCommentPrefix;

    /**
     * 繁体中文注释前缀
     * <p>
     * e.g. {ZHTCCommentPrefix}{commentSeparator}i18n繁体中文内容
     */
    @Parameter(
            property = "comment.zhtc.prefix",
            defaultValue = "zhtc"
    )
    private String ZHTCCommentPrefix;

    /**
     * 英文中文注释前缀
     * <p>
     * e.g. {ENUSCommentPrefix}{commentSeparator}i18n英文内容
     */
    @Parameter(
            property = "comment.enus.prefix",
            defaultValue = "en"
    )
    private String ENUSCommentPrefix;

    /**
     * 注释分隔符
     * <p>
     * e.g. zh{commentSeparator}名字
     */
    @Parameter(
            property = "comment.separator",
            defaultValue = ":"
    )
    private String commentSeparator;

    /**
     * 是否覆盖同名的key
     * <p>
     * 例如 i18n-language.properties
     * 包含了 name=zzzj
     * <p>
     * 解析proto文件得到 name=zzzj1233
     * <p>
     * 是否覆盖之前的 name=zzzj
     * <p>
     * 使 name=zzzj12333
     */
    @Parameter(
            property = "i18n.overlap",
            defaultValue = "true"
    )
    private Boolean overlap;

    private PropertiesFile ZHCNProperties;

    private PropertiesFile ZHTCProperties;

    private PropertiesFile ENUSProperties;

    // private Charset charset;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (Boolean.TRUE.equals(skip)) {
            getLog().info(" ( proto -> i18N ) skipped");
            return;
        }

        // 1. check proto
        if (!protoFile.getName().endsWith(".proto")) {
            throw new MojoExecutionException(String.format("Illegal protoFile : %s , A file path ending in . proto must be declared ", protoFile));
        }

        if (!protoFile.exists()) {
            throw new MojoExecutionException(String.format("protoFile : %s not exists", protoFile));
        }


        String absolutePath = protoFile.getAbsolutePath();

        // importer from guava container
        Importer importer = Guice.createInjector(new ParserModule()).getInstance(Importer.class);

        // 2. initialize protostuff.protoContext
        ProtoContext protoContext = null;
        try {
            protoContext = importer.importFile(new LocalFileReader(new File(absolutePath).toPath()), absolutePath);
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Parse %s error , cause : %s", protoFile, e));
        }

        Enum enumProto = protoContext.getProto().getEnum(enumName);

        if (enumProto == null) {
            throw new MojoExecutionException(String.format("Parse %s error , Enum : %s not found ", protoFile, enumName));
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug(String.format("Enum %s , fields count : %d", enumName, enumProto.getConstants().size()));
        }

        // 3. initialize:  properties
        try {
            initialize();
        } catch (IOException e) {
            throw new MojoExecutionException("Initialize I18N properties file error : " + e);
        }

        List<I18N> i18NS = new ArrayList<>();

        // 4. parse proto file
        for (EnumConstant constant : enumProto.getConstants()) {

            List<String> commentLines = constant.getCommentLines();

            if (commentLines.isEmpty()) {
                continue;
            }

            I18N i18N = new I18N();

            i18N.setEnumValue(constant.getValue());

            for (String commentLine : commentLines) {
                String[] words = commentLine.split(commentSeparator);

                if (words.length < 2) {
                    continue;
                }

                String prefix = words[0];

                if (prefix.equals(ZHCNCommentPrefix)) {
                    i18N.setZhCN(words[1]);
                } else if (prefix.equals(ZHTCCommentPrefix)) {
                    i18N.setZhTC(words[1]);
                } else if (prefix.equals(ENUSCommentPrefix)) {
                    i18N.setEnUS(words[1]);
                } else if (getLog().isDebugEnabled()) {
                    getLog().debug(String.format("Incorrect comment line : %s", commentLine));
                }
            }

            if (translation && StringUtils.isEmpty(i18N.getZhTC()) && StringUtils.isNotEmpty(i18N.getZhCN())) {

                // 简体转繁体
                i18N.setZhTC(ChineseUtil.convert(i18N.getZhCN(), ChineseUtil.TRADITIONAL));
            }

            i18NS.add(i18N);
        }

        if (getLog().isDebugEnabled()) {
            i18NS.stream()
                    .map(Object::toString)
                    .forEach(getLog()::debug);
        }

        if (i18NS.isEmpty()) {
            return;
        }

        // 5. append to I18N file
        try {
            for (I18N i18N : i18NS) {
                setProperty(this.ZHCNProperties, i18N.getEnumValue(), i18N.getZhCN());
                setProperty(this.ZHTCProperties, i18N.getEnumValue(), i18N.getZhTC());
                setProperty(this.ENUSProperties, i18N.getEnumValue(), i18N.getEnUS());
            }
            this.ZHCNProperties.store();
            this.ZHTCProperties.store();
            this.ENUSProperties.store();
        } catch (Exception e) {
            getLog().error("Append proto comment to I18N file error: ", e);
            throw new MojoExecutionException("Append proto comment to I18N file error: ", e);
        }

        IOUtils.closeQuietly(this.ZHCNProperties);
        IOUtils.closeQuietly(this.ZHTCProperties);
        IOUtils.closeQuietly(this.ENUSProperties);

        getLog().info("Append proto comment to I18N file successful");
    }

    void setProperty(PropertiesFile propertiesFile, int enumValue, String message) {
        if (StringUtils.isNotBlank(message)) {
            propertiesFile.setProperty(i18nKeyPrefix + enumValue, message);
        }
    }

    void initialize() throws IOException {
        this.ZHCNProperties = PropertiesFileFactory.newInstance(this.i18nZHCN, overlap, clear);
        this.ZHTCProperties = PropertiesFileFactory.newInstance(this.i18nZHTC, overlap, clear);
        this.ENUSProperties = PropertiesFileFactory.newInstance(this.i18nENUS, overlap, clear);
    }

    FileOutputStream safeOutputStream(File file, boolean append) throws FileNotFoundException {
        if (file == null) {
            return null;
        }
        return new FileOutputStream(file, append);
    }

    String executeProtoCommand(String protoFilePath) throws MojoExecutionException {
        String dirName = FileUtils.dirname(protoFilePath);

        String fileName = FileUtils.filename(protoFilePath);

        String outputFileName = fileName + ".bin";

        String command = new StringBuilder()
                .append("protoc --proto_path=")
                .append(dirName)
                .append(" ")
                .append("--include_source_info --descriptor_set_out=")
                .append(outputFileName)
                .append(" ")
                .append(fileName)
                .toString();

        StringBuilder executeErrorMessage = new StringBuilder();

        try {
            CommandLineUtils.executeCommandLine(
                    new Commandline(command),
                    output -> {
                    },
                    error -> executeErrorMessage.append(error).append("\n")
            );
        } catch (CommandLineException e) {
            throw new MojoExecutionException("Execute protoc command error " + e);
        }

        if (executeErrorMessage.length() > 0) {
            throw new MojoExecutionException("Execute protoc command error , cause : " + executeErrorMessage);
        }

        return outputFileName;
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

    public Boolean getSkip() {
        return skip;
    }

    public void setSkip(Boolean skip) {
        this.skip = skip;
    }

    public Boolean getTranslation() {
        return translation;
    }

    public void setTranslation(Boolean translation) {
        this.translation = translation;
    }

    public String getZHCNCommentPrefix() {
        return ZHCNCommentPrefix;
    }

    public void setZHCNCommentPrefix(String ZHCNCommentPrefix) {
        this.ZHCNCommentPrefix = ZHCNCommentPrefix;
    }

    public String getZHTCCommentPrefix() {
        return ZHTCCommentPrefix;
    }

    public void setZHTCCommentPrefix(String ZHTCCommentPrefix) {
        this.ZHTCCommentPrefix = ZHTCCommentPrefix;
    }

    public String getENUSCommentPrefix() {
        return ENUSCommentPrefix;
    }

    public void setENUSCommentPrefix(String ENUSCommentPrefix) {
        this.ENUSCommentPrefix = ENUSCommentPrefix;
    }

    public String getCommentSeparator() {
        return commentSeparator;
    }

    public void setCommentSeparator(String commentSeparator) {
        this.commentSeparator = commentSeparator;
    }

    public String getI18nKeyPrefix() {
        return i18nKeyPrefix;
    }

    public void setI18nKeyPrefix(String i18nKeyPrefix) {
        this.i18nKeyPrefix = i18nKeyPrefix;
    }

    public Boolean getClearI18NFile() {
        return clear;
    }

    public void setClearI18NFile(Boolean clearI18NFile) {
        this.clear = clearI18NFile;
    }

    public File getProtoFile() {
        return protoFile;
    }

    public void setProtoFile(File protoFile) {
        this.protoFile = protoFile;
    }

    public File getI18nZHCN() {
        return i18nZHCN;
    }

    public void setI18nZHCN(File i18nZHCN) {
        this.i18nZHCN = i18nZHCN;
    }

    public File getI18nENUS() {
        return i18nENUS;
    }

    public void setI18nENUS(File i18nENUS) {
        this.i18nENUS = i18nENUS;
    }

    public File getI18nZHTC() {
        return i18nZHTC;
    }

    public void setI18nZHTC(File i18nZHTC) {
        this.i18nZHTC = i18nZHTC;
    }

    public Boolean getOverlap() {
        return overlap;
    }

    public void setOverlap(Boolean overlap) {
        this.overlap = overlap;
    }

}
