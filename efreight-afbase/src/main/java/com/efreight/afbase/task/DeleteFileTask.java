package com.efreight.afbase.task;

import java.io.File;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.efreight.afbase.utils.FilePathUtils;


@Component
@EnableScheduling
public class DeleteFileTask {
	
	@Scheduled(cron = "0 30 02 * * ?")	
	public void deleteFileTask() {
		try {
			String path=FilePathUtils.filePath+"/PDFtemplate/temp";
//			String path="D:/html/PDFtemplate/temp";
//			String path="/var/www/html/PDFtemplate/temp";
			delAllFile(path); // 删除完里面所有内容
			System.out.println("删除完里面所有内容");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除文件夹
	 * @param folderPath
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 删除文件夹下的所有文件
	 * @param path
	 * @return
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

}
