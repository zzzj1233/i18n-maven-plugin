package com.github.zzzj1233;

import java.io.*;
import java.util.*;

/**
 * 中文简繁体互转
 * 
 */
public class ChineseUtil {

	private Properties charMap = new Properties();
	private Set<String> conflictingSets = new HashSet<>();

	public static final int TRADITIONAL = 0;
	public static final int SIMPLIFIED = 1;
	private static final int NUM_OF_CONVERTERS = 2;
	private static final ChineseUtil[] converters = new ChineseUtil[NUM_OF_CONVERTERS];
	private static final String[] propertyFiles = new String[2];

	static {
		propertyFiles[TRADITIONAL] = "zh2Hant.properties";// 简转繁字典
		propertyFiles[SIMPLIFIED] = "zh2Hans.properties";// 繁转简字典
	}

	/**
	 * @param converterType 0 for traditional and 1 for simplified
	 * @return
	 */
	public static ChineseUtil getInstance(int converterType) {

		if (converterType >= 0 && converterType < NUM_OF_CONVERTERS) {

			if (converters[converterType] == null) {
				synchronized (ChineseUtil.class) {
					if (converters[converterType] == null) {
						converters[converterType] = new ChineseUtil(propertyFiles[converterType]);
					}
				}
			}
			return converters[converterType];

		} else {
			return null;
		}
	}

	/**
	 * 简体繁体互转
	 * 
	 * @param text          待转换的文本内容
	 * @param converterType 0转成繁体 1 转成简体
	 * @return
	 */
	public static String convert(String text, int converterType) {
		ChineseUtil instance = getInstance(converterType);
		return instance.convert(text);
	}

	// 获取字典库
	private ChineseUtil(String propertyFile) {
		InputStream is = null;
		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile);
		if (is != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
				charMap.load(reader);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (reader != null)
						reader.close();
					if (is != null)
						is.close();
				} catch (IOException e) {
				}
			}
		}
		initializeHelper();
	}

	private void initializeHelper() {
		Map<String, Integer> stringPossibilities = new HashMap<>();
		Iterator<?> iter = charMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (key.length() >= 1) {

				for (int i = 0; i < (key.length()); i++) {
					String keySubstring = key.substring(0, i + 1);
					if (stringPossibilities.containsKey(keySubstring)) {
						Integer integer = (Integer) (stringPossibilities.get(keySubstring));
						stringPossibilities.put(keySubstring, new Integer(integer.intValue() + 1));// 多意字字池

					} else {
						stringPossibilities.put(keySubstring, new Integer(1));
					}

				}
			}
		}

		iter = stringPossibilities.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (((Integer) (stringPossibilities.get(key))).intValue() > 1) {
				conflictingSets.add(key);
			}
		}
	}

	/**
	 * 内容转换
	 * 
	 * @param in
	 * @return
	 */
	public String convert(String in) {
		StringBuilder outString = new StringBuilder();// 转换后的内容
		StringBuilder stackString = new StringBuilder();// 压栈池(临时)
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			String key = "" + c;
			stackString.append(key);
			if (conflictingSets.contains(stackString.toString())) {// 多意字先跳过,直接先入栈
			} else if (charMap.containsKey(stackString.toString())) { // 栈内容如果直接属于字典库，那么直接进行转换,并且清理栈内的东西
				outString.append(charMap.get(stackString.toString()));
				stackString.setLength(0);
			} else { // 当且不是多意字,在字典里也找不到，则需要把栈内除最后一个字去掉后，再次分词/字查找转换
				CharSequence sequence = stackString.subSequence(0, stackString.length() - 1);
				stackString.delete(0, stackString.length() - 1);
				flushStack(outString, new StringBuilder(sequence));
			}
		}

		flushStack(outString, stackString);
		return outString.toString();
	}

	private void flushStack(StringBuilder outString, StringBuilder stackString) {
		while (stackString.length() > 0) {
			if (charMap.containsKey(stackString.toString())) {
				outString.append(charMap.get(stackString.toString()));
				stackString.setLength(0);
			} else {
				outString.append("" + stackString.charAt(0));
				stackString.delete(0, 1);
			}
		}
	}

}