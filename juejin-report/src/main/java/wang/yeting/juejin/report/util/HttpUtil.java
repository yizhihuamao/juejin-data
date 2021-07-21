package wang.yeting.juejin.report.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.util.Map;

/**
 * @author : weipeng
 * @since : 2021-06-21 12:24 下午
 */

public class HttpUtil {

    public static String post(String url, Map<String, String> headers, String body) {
        HttpRequest request = cn.hutool.http.HttpUtil.createPost(url)
                .headerMap(headers, true)
                .body(body);
        HttpResponse response = request.execute();
        return response.body();
    }

    public static String post(String url, String body) {
        HttpRequest request = cn.hutool.http.HttpUtil.createPost(url)
                .body(body);
        HttpResponse response = request.execute();
        return response.body();
    }

    public static String get(String url, Map<String, String> headers, String cookie) {
        HttpRequest request = cn.hutool.http.HttpUtil.createGet(url).cookie(cookie).headerMap(headers, true);
        HttpResponse response = request.execute();
        return response.body();
    }

    public static String get(String url, String cookie) {
        HttpRequest request = cn.hutool.http.HttpUtil.createGet(url).cookie(cookie);
        HttpResponse response = request.execute();
        return response.body();
    }

    public static String get(String url) {
        HttpRequest request = cn.hutool.http.HttpUtil.createGet(url);
        HttpResponse response = request.execute();
        return response.body();
    }

    public static String get(String url, Map<String, String> headers) {
        HttpRequest request = cn.hutool.http.HttpUtil.createPost(url).body("{}")
                .headerMap(headers, true);
        HttpResponse response = request.execute();
        return response.body();
    }


}
