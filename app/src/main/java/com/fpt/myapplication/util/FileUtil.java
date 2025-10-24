package com.fpt.myapplication.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUtil {
    private static final String BASE_URL = "http://10.0.2.2:8080";
    public static MultipartBody.Part uriToPart(Uri uri, Context ctx) throws IOException {
        ContentResolver resolver = ctx.getContentResolver();
        String mime = resolver.getType(uri);            // VD: image/jpeg
        InputStream is = resolver.openInputStream(uri);
        byte[] bytes = readAllBytes(is);                // function bên dưới

        RequestBody reqFile = RequestBody.create(bytes, MediaType.parse(mime));
        return MultipartBody.Part.createFormData("avatar", "avatar.jpg", reqFile);
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public static String GetImageUrl(String imagePath) {
        if(imagePath == null){
            return null;
        }
        return BASE_URL + imagePath;
    }
}
