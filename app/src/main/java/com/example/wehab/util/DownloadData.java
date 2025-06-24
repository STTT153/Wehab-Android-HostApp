package com.example.wehab.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadData {

    public static void saveStringToCSV(String fileName, String content, Context context) {
        // 添加时间戳到文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // 去掉已有的 .csv 后缀以防重复
        if (fileName.endsWith(".csv")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }

        // 构造完整文件名
        fileName = fileName + "_" + timeStamp + ".csv";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 及以上使用 MediaStore API
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    os.write(content.getBytes());
                    Toast.makeText(context, "保存成功：" + fileName, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "无法创建文件 Uri", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Android 9 及以下使用传统文件系统
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes());
                Toast.makeText(context, "保存成功：" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
