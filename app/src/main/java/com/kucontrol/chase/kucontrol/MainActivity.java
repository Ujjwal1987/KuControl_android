package com.kucontrol.chase.kucontrol;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission_group.CAMERA;
import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {
    String searchstring = "<input type=\"hidden\" name=\"_csrf\" value=";
    ProgressBar progressBar;
    private HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private EditText username, password;
    TextView register;
    int flag;
    CardView Login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String[] User = new String[1];
        final String[] Passwd = new String[1];
        final String[] token = new String[1];
        token[0] = "";
        final String url = "https://ku-control.com/login";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (EditText) findViewById(R.id.Username);
        password = (EditText) findViewById(R.id.Password);
        Login = (CardView) findViewById(R.id.Login);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);
        register = (TextView) findViewById(R.id.Register);
        String credentials = null;
        String Cookies = "";

        final OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
        Cookies = retreivecookies();
        if (!(Cookies.equals(""))) {
            Intent intent = new Intent(MainActivity.this, Sitemap.class);
            startActivity(intent);
        } else {
            //get request
            Request getrequest = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(getrequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String html = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            token[0] = retreivetoken(html);

                        }
                    });
                }
            });

            Login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User[0] = username.getText().toString();
                    Passwd[0] = password.getText().toString();
                    if (TextUtils.isEmpty(User[0])) {
                        username.setError("Username field cannot be empty");
                    } else if (TextUtils.isEmpty(Passwd[0])) {
                        password.setError("Password cannot be empty");
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        //post request
                        RequestBody requestBody = new FormBody.Builder()
                                .add("username", User[0])
                                .add("password", Passwd[0])
                                .add("_csrf", token[0])
                                .build();

                        Request postrequest = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();

                        client.newCall(postrequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                final String html = response.body().string();
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temptoken = retreivetoken(html);
                                        if (temptoken.equals("")) {
                                            if (html.contains("Your KuControl gateway is offline") || html.contains("You are using KuControl")) {

                                                progressBar.setVisibility(View.GONE);
                                                Storecookie(cookieStore);
                                                Intent intent = new Intent(MainActivity.this, Sitemap.class);
                                                startActivity(intent);

                                            }
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            flag = 0;
                                            password.setError("Incorrect username/password");
                                        }

                                    }
                                });
                            }
                        });

                    }
                }
            });
        }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });
    }
    public String retreivetoken(String html){
        String token = "";
            if(html.contains(searchstring)){
                int startindex = html.indexOf(searchstring);
                int endindex = searchstring.length() + startindex;
                token = html.substring(endindex+1, endindex+37);
            }
        return token;
    }

    public void Storecookie(HashMap<String, List<Cookie>> cookieStore){
        HttpUrl url = HttpUrl.parse("https://ku-control.com/login");
        List<Cookie> cookies = cookieStore.get(url.host());
        Cookie tempcookies;
        FileOutputStream fos;
        for(int i=0; i<cookies.size(); i++){
            tempcookies = cookies.get(i);
            try {
                fos = openFileOutput("cookies.dat", MODE_PRIVATE);
                String cookiestring ="";
                cookiestring = tempcookies.name()+":"+tempcookies.value() + ":"+ tempcookies.domain() + ":"+ tempcookies.path();
                fos.write(cookiestring.getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public String retreivecookies(){
        FileInputStream fis;
        String cookiesstring ="";
        try {
            fis = openFileInput("cookies.dat");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            cookiesstring=br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cookiesstring;
    }
}
