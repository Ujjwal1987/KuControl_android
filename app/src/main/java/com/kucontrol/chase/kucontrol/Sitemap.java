package com.kucontrol.chase.kucontrol;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.view.Menu;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Sitemap extends AppCompatActivity {
    String cookiestring;
    JSONArray jsonArray;
    private HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView ErrorMessage;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sitemap);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        progressBar = (ProgressBar) findViewById(R.id.pro);
        ErrorMessage = findViewById(R.id.errorMessage);
        progressBar.setVisibility(View.VISIBLE);
        if (url.contains("ku-control")) {
        cookiestring = retreivecookies();
        String[] temp = cookiestring.split(":");
        swipeRefreshLayout = findViewById(R.id.ref);
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
        url = "https://ku-control.com/rest/sitemaps";

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String output = response.body().string();
                Sitemap.this.runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        try {
                            progressBar.setVisibility(View.GONE);

                            if (output.equals("Unauthorized")) {
                                Updatecookie();
                                Intent intent = new Intent(Sitemap.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                if (output.equals("Your KuControl gateway is offline")) {
                                    ErrorMessage.setVisibility(View.VISIBLE);
                                    ErrorMessage.setText("Kucontrol gateway offline");
                                } else {
                                    ErrorMessage.setVisibility(View.GONE);
                                    jsonArray = new JSONArray(output);
                                    JSONObject jsonObject;
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        jsonObject = jsonArray.getJSONObject(i);
                                        if (jsonObject.getString("name").equals("_default")) {
                                            jsonArray.remove(i);
                                        } else {
                                            continue;
                                        }
                                    }
                                    Listview(jsonArray);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final String output = response.body().string();
                        Sitemap.this.runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void run() {
                                try {
                                    progressBar.setVisibility(View.GONE);
                                    if (output.equals("Unauthorized")) {
                                        Updatecookie();
                                        Intent intent = new Intent(Sitemap.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        if (output.equals("Your KuControl gateway is offline")) {
                                            ErrorMessage.setVisibility(View.VISIBLE);
                                            ErrorMessage.setText("Kucontrol gateway offline");
                                        } else {
                                            ErrorMessage.setVisibility(View.GONE);
                                            jsonArray = new JSONArray(output);
                                            JSONObject jsonObject;
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                jsonObject = jsonArray.getJSONObject(i);
                                                if (jsonObject.getString("name").equals("_default")) {
                                                    jsonArray.remove(i);
                                                } else {
                                                    continue;
                                                }
                                            }
                                            Listview(jsonArray);
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000);
            }
        });
    }else{
            swipeRefreshLayout = findViewById(R.id.ref);
            final OkHttpClient client = new OkHttpClient.Builder()
                    .build();
            url = "http://"+url+":8080/rest/sitemaps";

            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final String output = response.body().string();
                    Sitemap.this.runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void run() {
                            try {
                                progressBar.setVisibility(View.GONE);

                                if (output.equals("Unauthorized")) {
                                    Updatecookie();
                                    Intent intent = new Intent(Sitemap.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    if (output.equals("Your KuControl gateway is offline")) {
                                        ErrorMessage.setVisibility(View.VISIBLE);
                                        ErrorMessage.setText("Kucontrol gateway offline");
                                    } else {
                                        ErrorMessage.setVisibility(View.GONE);
                                        jsonArray = new JSONArray(output);
                                        JSONObject jsonObject;
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            jsonObject = jsonArray.getJSONObject(i);
                                            if (jsonObject.getString("name").equals("_default")) {
                                                jsonArray.remove(i);
                                            } else {
                                                continue;
                                            }
                                        }
                                        Listview(jsonArray);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            });
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            final String output = response.body().string();
                            Sitemap.this.runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void run() {
                                    try {
                                        progressBar.setVisibility(View.GONE);
                                        if (output.equals("Unauthorized")) {
                                            Updatecookie();
                                            Intent intent = new Intent(Sitemap.this, MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            if (output.equals("Your KuControl gateway is offline")) {
                                                ErrorMessage.setVisibility(View.VISIBLE);
                                                ErrorMessage.setText("Kucontrol gateway offline");
                                            } else {
                                                ErrorMessage.setVisibility(View.GONE);
                                                jsonArray = new JSONArray(output);
                                                JSONObject jsonObject;
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    jsonObject = jsonArray.getJSONObject(i);
                                                    if (jsonObject.getString("name").equals("_default")) {
                                                        jsonArray.remove(i);
                                                    } else {
                                                        continue;
                                                    }
                                                }
                                                Listview(jsonArray);
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 5000);
                }
            });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
        return;
    }

    public void Updatecookie(){
        File dir = getFilesDir();
        File file = new File(dir, "cookies.dat");
        file.delete();
        /*FileOutputStream fos;
           try {
                fos = openFileOutput("cookies.dat", MODE_PRIVATE);
                fos.write("".getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    public void Listview(JSONArray sitemaps){
//        HashMap<String, String> sitemapname = new HashMap<>();
        ListView listView;
        Integer[] sitemapicons = new Integer[sitemaps.length()];
        final String [] sitemapname = new String[sitemaps.length()];
        try {
            for(int i=0; i<sitemaps.length(); i++){
                JSONObject temp = sitemaps.getJSONObject(i);

                    sitemapname[i] = temp.getString("name");
                    sitemapicons[i] = R.drawable.sitemap;

            }
            listView = findViewById(R.id.Listview);
            sitemapview smv = new sitemapview(this, sitemapname,sitemapicons);
            listView.setAdapter(smv);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedsitemap;
                        selectedsitemap = sitemapname[position];
                        Intent intent = new Intent(Sitemap.this, Group.class);
                        intent.putExtra("SelectedSitemap", selectedsitemap);
                        intent.putExtra("url",url);
                        startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
       public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout){
            if(url.contains("ku-control")) {
                String[] temp = cookiestring.split(":");
                final Cookie cookie = new Cookie.Builder()
                        .domain(temp[2])
                        .path(temp[3])
                        .name(temp[0])
                        .value(temp[1])
                        .httpOnly()
                        .build();

                OkHttpClient client = new OkHttpClient.Builder()
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
                String url = "https://ku-control.com/logout";
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Sitemap.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Updatecookie();
                                Intent intent = new Intent(Sitemap.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }else{
                this.finishAffinity();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}