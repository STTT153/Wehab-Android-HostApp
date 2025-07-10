package com.example.wehab.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CsvDataWriter {
    private Uri uri;
    private ArrayList<String> buffer;
    private int threshold;
    private Context context;

    public CsvDataWriter(Context context, String fileName, String filePath, int threshold){
        this.context = context;
        this.threshold = threshold;
        createCsvFile(fileName, filePath);
    }

    private boolean isVersionValid(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * 将 CSV 格式的字符串保存到 Downloads 公共目录中（适配 Android 10+）。
     *
     * @param fileName 要保存的文件名，例如 "data.csv"
     * @param filePath 存入的子目录位置，例如 "\MyData"
     */
    public void createCsvFile(String fileName, String filePath){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName); // 文件名
        contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/csv");  // MIME 类型
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + filePath); // 子目录（可选）

        ContentResolver resolver = this.context.getContentResolver();
        this.uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
    }

    public void write(String txt){
        this.buffer.add(txt);
        if (this.buffer.size() >= this.threshold){
            return;

        }
    }

    /**
     * 将缓冲区中的字符串写入指定 Uri 的输出流，写入完成后清空缓冲区。
     * @throws IOException 写入或打开流时可能抛出异常
     */
    private void flush() throws IOException {
        try (OutputStream outputStream = this.context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                for (String txt : this.buffer) {
                    outputStream.write(txt.getBytes(StandardCharsets.UTF_8));
                }
                outputStream.flush();
            } else {
                throw new IOException("Failed to open output stream for Uri: " + uri);
            }

            this.buffer.clear();

        }
    }
}
