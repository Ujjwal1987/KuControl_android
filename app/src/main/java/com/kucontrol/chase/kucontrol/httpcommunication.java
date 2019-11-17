package com.kucontrol.chase.kucontrol;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class httpcommunication {
    public static String responsestring;

    public void connection(String url, final Cookie cookie, final Activity context){
        final OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        final ArrayList<Cookie> oneCookie = new ArrayList<>(1);
                        oneCookie.add(cookie);
                        return oneCookie;
                    }
                })
                .build();
        final Request request= new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                responsestring = "";
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responsestring = response.body().string();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("test", responsestring);
                    }
                });
            }
        });
        Log.d("test", responsestring);
    }
}
