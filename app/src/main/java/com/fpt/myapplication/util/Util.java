// com.fpt.myapplication.util.Util
package com.fpt.myapplication.util;

import com.fpt.myapplication.dto.ResponseError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class Util {
    private static final Gson gson = new Gson();

    private Util() {}

    /** Parse error body -> ResponseError (fallback thông minh nếu JSON khác cấu trúc). */
    public static ResponseError parseError(Response<?> resp) {
        int httpCode = resp.code();
        ResponseBody eb = resp.errorBody();
        if (eb == null) return new ResponseError(httpCode, "HTTP " + httpCode);

        try {
            // LƯU Ý: .string() chỉ đọc được 1 lần
            String raw = eb.string();

            // 1) Thử parse trực tiếp sang ResponseError ({"code":..,"message":".."})
            ResponseError err = null;
            try {
                err = gson.fromJson(raw, ResponseError.class);
            } catch (Exception ignore) { /* sẽ thử cách khác bên dưới */ }

            // 2) Nếu backend bọc trong { "error": { code, message } } hoặc { "message": ... }
            if (err == null || (err.message == null && raw != null)) {
                try {
                    JsonObject root = JsonParser.parseString(raw).getAsJsonObject();

                    if (root.has("error") && root.get("error").isJsonObject()) {
                        JsonObject e = root.getAsJsonObject("error");
                        Integer code = e.has("code") ? e.get("code").getAsInt() : httpCode;
                        String msg = e.has("message") ? e.get("message").getAsString() : ("HTTP " + httpCode);
                        err = new ResponseError(code, msg);
                    } else if (root.has("message")) {
                        err = new ResponseError(httpCode, root.get("message").getAsString());
                    }
                } catch (Exception ignore) {
                    // không phải JSON hợp lệ -> sẽ fallback raw
                }
            }

            // 3) Fallback cuối: dùng raw text
            if (err == null) err = new ResponseError(httpCode, (raw == null || raw.isEmpty()) ? ("HTTP " + httpCode) : raw);
            if (err.code == 0) err.code = httpCode;
            return err;
        } catch (Exception ex) {
            return new ResponseError(httpCode, "HTTP " + httpCode);
        } finally {
            eb.close();
        }
    }
}
