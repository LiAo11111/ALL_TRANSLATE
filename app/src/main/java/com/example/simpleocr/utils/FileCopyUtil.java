package com.example.simpleocr.utils;

// 文件复制工具类

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopyUtil {

    public static void copyFileFromAssets(Context context, String assetName, String savePath, String saveName) {
        // 若目标文件夹不存在，则创建
        File dir = new File(savePath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.d("FileUtils", "mkdir error: " + savePath);
                return;
            }
        }

        // 拷贝文件
        String filename = savePath + "/" + saveName;
        File file = new File(filename);
        if (!file.exists()) {
            try {
                InputStream inStream = context.getAssets().open(assetName);
                FileOutputStream fileOutputStream = new FileOutputStream(filename);

                int byteread;
                byte[] buffer = new byte[2048];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, byteread);
                }
                fileOutputStream.flush();
                inStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("FileUtils", "[copyFileFromAssets] copy asset file: " + assetName + " to : " + filename);
        } else {
            Log.d("FileUtils", "[copyFileFromAssets] file exists: " + filename);
        }
    }

    private static void copyFileFromAssets(Context appCtx, String srcPath, File dstFile) {
        if (srcPath.isEmpty()) {
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(appCtx.getAssets().open(srcPath));

            os = new BufferedOutputStream(new FileOutputStream(dstFile));
            byte[] buffer = new byte[2048];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void copyDirInDirFromAssets(Context context, String assetsPath, String savePath) {
        try {
            // 获取assets指定目录下的所有文件
            String[] dirList = context.getAssets().list(assetsPath);
            if (dirList != null && dirList.length > 0) {
                File file = new File(savePath);
                // 如果目标路径文件夹不存在，则创建
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Log.d("FileUtils", "mkdir error: " + savePath);
                        return;
                    }
                }
                for (String dirName : dirList) {
                    String[] fileList = context.getAssets().list(assetsPath + "/"  + dirName);
                    for(String fileName: fileList) {
                        copyFileFromAssets(context, assetsPath + "/" + dirName + "/" + fileName, savePath, fileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyDirFromAssets(Context context, String assetsPath, String savePath) {
        try {
            // 获取assets指定目录下的所有文件
            String[] fileList = context.getAssets().list(assetsPath);
            if (fileList != null && fileList.length > 0) {
                File file = new File(savePath);
                // 如果目标路径文件夹不存在，则创建
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Log.d("FileUtils", "mkdir error: " + savePath);
                        return;
                    }
                }
                for (String fileName : fileList) {
                    copyFileFromAssets(context, assetsPath + "/"  + fileName, savePath, fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
