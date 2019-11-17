package com.kucontrol.chase.kucontrol;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChangeState {
    String status,itemname, cookiestring;
    RequestBody requestBody;
    Request request;

    public ChangeState(String status, String itemname, String cookiestring) {
        this.status = status;
        this.itemname = itemname;
        this.cookiestring = cookiestring;
    }


    public void changestate() {
        String url = "https://ku-control.com/rest/items/"+itemname;
        senddata sd = new senddata();
        sd.execute(url, status, cookiestring);
    }
    class senddata extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String[]temp = strings[2].split(":");
            MediaType textPlainMT = MediaType.parse("text/plain; charset=utf-8");
            final Cookie cookie = new Cookie.Builder()
                    .domain(temp[2])
                    .path(temp[3])
                    .name(temp[0])
                    .value(temp[1])
                    .httpOnly()
                    .build();
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

            if(status.equals("ON")) {
                request = new Request.Builder()
                        .url(strings[0])
                        .post(RequestBody.create(textPlainMT,"OFF"))
                        .build();
                try {
                    client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
                request = new Request.Builder()
                        .url(strings[0])
                        .post(RequestBody.create(textPlainMT,"ON"))
                        .build();
                try {
                    client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
