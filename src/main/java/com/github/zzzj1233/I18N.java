package com.github.zzzj1233;

/**
 * @author zzzj
 * @create 2023-01-09 18:34
 */
public class I18N {

    private String zhCN;

    private String zhTC;

    private String enUS;

    private int enumValue;

    public I18N() {
    }

    public I18N(String zhCN, String znTC, String enUS, int index) {
        this.zhCN = zhCN;
        this.zhTC = znTC;
        this.enUS = enUS;
        this.enumValue = index;
    }

    public String getZhCN() {
        return zhCN;
    }

    public void setZhCN(String zhCN) {
        this.zhCN = zhCN;
    }

    public String getZhTC() {
        return zhTC;
    }

    public void setZhTC(String zhTC) {
        this.zhTC = zhTC;
    }

    public String getEnUS() {
        return enUS;
    }

    public void setEnUS(String enUS) {
        this.enUS = enUS;
    }

    public int getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(int enumValue) {
        this.enumValue = enumValue;
    }


    @Override
    public String toString() {
        return "I18NInfo{" +
                "zhCN='" + zhCN + '\'' +
                ", zhTC='" + zhTC + '\'' +
                ", enUS='" + enUS + '\'' +
                ", enumValue=" + enumValue +
                '}';
    }
}
