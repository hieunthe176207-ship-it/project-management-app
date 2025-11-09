package com.fpt.myapplication.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.fpt.myapplication.config.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUtil {

    public static MultipartBody.Part uriToPart(String key, Uri uri, Context ctx) throws IOException {
        ContentResolver resolver = ctx.getContentResolver();
        String mime = resolver.getType(uri);
        InputStream is = resolver.openInputStream(uri);
        byte[] bytes = readAllBytes(is);
        RequestBody reqFile = RequestBody.create(bytes, MediaType.parse(mime));
        return MultipartBody.Part.createFormData(key, "avatar.jpg", reqFile);
    }

    public static MultipartBody.Part stringToPart( String key, String value) {
        RequestBody nameBody = RequestBody.create(
                value,
                MediaType.parse("text/plain")
        );
        return MultipartBody.Part.createFormData(key, null, nameBody);
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
        Log.d("FILE", "GetImageUrl: "+ Constant.BASE_URL + imagePath);
        return Constant.BASE_URL + imagePath;
    }
}
