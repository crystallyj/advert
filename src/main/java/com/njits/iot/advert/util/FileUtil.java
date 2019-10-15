package com.njits.iot.advert.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static String writeFileContent(File file, String key, String value) {
		String jsonString = FileUtil.readTxt(file, "utf-8");
		Map<String, String> map;
		if(StringUtil.isNullEmpty(jsonString)) {
			map = new HashMap<>();
		} else {
			map = JSONUtil.toMap(jsonString);
		}
		map.put(key, value);
		return JSONUtil.toJson(map);
	}
	
	/**
	 * 读取文本文件内容
	 * 
	 * @param filePathAndName 带有完整绝对路径的文件名
	 * @param encoding        文本文件打开的编码方式
	 * @return 返回文本文件的内容
	 */
	public static String readTxt(File file, String encoding) {
		encoding = encoding.trim();
		StringBuilder str = new StringBuilder("");
		String st = "";
		FileInputStream fs = null;
		InputStreamReader isr = null;
		try {
			fs = new FileInputStream(file);

			if (encoding.equals("")) {
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding);
			}
			BufferedReader br = new BufferedReader(isr);
			try {
				String data = "";
				while ((data = br.readLine()) != null) {
					str.append(data + " ");
				}
			} catch (Exception e) {
				str.append(e.toString());
			} finally {
				br.close();
			}
			st = str.toString();
		} catch (IOException es) {
			st = "";
		} finally {
			if (null != fs) {
				try {
					fs.close();
				} catch (IOException e) {
					logger.error("Error reading text, {}", e.getMessage());
				}
			}
			if (null != isr) {
				try {
					isr.close();
				} catch (IOException e) {
					logger.error("Error reading text, {}", e.getMessage());
				}
			}
		}
		return st;
	}

	/**
	 * 向文件末尾写入字符串数据
	 * 
	 * @param data   数据
	 * @param file   文件对象
	 * @param append 追加还是覆盖模式 true-追加 ，false-覆盖
	 * @return 写入结果
	 * @throws IOException
	 */
	public static boolean writeFile(String data, File file, boolean append) {
		boolean success = true;
		OutputStream os = null;
		OutputStreamWriter writer = null;
		BufferedWriter bw = null;
		try {
			os = new FileOutputStream(file, append);
			writer = new OutputStreamWriter(os);
			bw = new BufferedWriter(writer);
			bw.write(data);
			bw.flush();
		} catch (Exception e) {
			success = false;
			logger.error("Failed to write file, {}", e.getMessage());
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
				if (writer != null) {
					writer.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				logger.error("Failed to close file write stream, {}", e.getMessage());
			}
		}
		return success;
	}
}
