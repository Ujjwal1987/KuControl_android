package com.kucontrol.chase.kucontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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

public class Group extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    String cookiestring;
    String url = "https://ku-control.com/rest/items";
    Response response;

    SwipeRefreshLayout swipeRefreshLayout;
    String json;
    JSONArray jsonArray;
    JSONObject jsonObject;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Intent intent = getIntent();
        swipeRefreshLayout = findViewById(R.id.ref1);
        String selectedsitemap = intent.getStringExtra("SelectedSitemap");
        cookiestring = retreivecookies();
        String[]temp = cookiestring.split(":");
        swipeRefreshLayout= findViewById(R.id.ref1);
        progressBar = findViewById(R.id.progressbar3);
        progressBar.setVisibility(View.VISIBLE);
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
        final Request request= new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressBar.setVisibility(View.GONE);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String temp = response.body().string();
                Group.this.runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        try {
                            jsonArray = new JSONArray(temp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONArray jsonArraytemp = new JSONArray();
                        JSONObject jsontemp;
                        for(int i=0; i<jsonArray.length(); i++){
                            try {
                                if(i==0){
                                    continue;
                                }else {
                                    jsontemp = jsonArray.getJSONObject(i);
                                    if (jsontemp.getString("type").equals("Group")) {
                                        jsonArraytemp.put(jsontemp);
                                    } else {
                                        continue;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Listview(jsonArraytemp);
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
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String temp = response.body().string();
                            Group.this.runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    try {
                                        jsonArray = new JSONArray(temp);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    JSONArray jsonArraytemp = new JSONArray();
                                    JSONObject jsontemp;
                                    for(int i=0; i<jsonArray.length(); i++){
                                        try {
                                            if(i==0){
                                                continue;
                                            }else {
                                                jsontemp = jsonArray.getJSONObject(i);
                                                if (jsontemp.getString("type").equals("Group")) {
                                                    jsonArraytemp.put(jsontemp);
                                                } else {
                                                    continue;
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Listview(jsonArraytemp);
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

//        response = Httpcommunication.getResponse1();

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

    public void Updatecookie(){
        File dir = getFilesDir();
        File file = new File(dir, "cookies.dat");
        file.delete();
    }
    public void Listview(JSONArray groups){
//        HashMap<String, String> sitemapname = new HashMap<>();
        ListView listView;
        Integer[] groupicons = new Integer[groups.length()];
        final String [] groupname = new String[groups.length()];
        try {
            for(int i=0; i<groups.length(); i++){
                JSONObject temp = groups.getJSONObject(i);
                groupname[i] = temp.getString("name");
                if(groupname[i].contains("Study")||groupname[i].contains("office")){
                    groupicons[i] = R.drawable.office;
                }else if(groupname[i].contains("Balcony")||groupname[i].contains("garden")){
                    groupicons[i] = R.drawable.garden;
                }else if(groupname[i].contains("Bed")){
                    groupicons[i] = R.drawable.bedroom;
                }else if(groupname[i].contains("garage")||groupname[i].contains("store")){
                    groupicons[i] = R.drawable.garage;
                }else if(groupname[i].contains("Lobby")||groupname[i].contains("corridor")){
                    groupicons[i] = R.drawable.corridor;
                } else if(groupname[i].contains("floor")){
                    groupicons[i] = R.drawable.groundfloor;
                } else if(groupname[i].contains("garage")||groupname[i].contains("cellar")){
                    groupicons[i] = R.drawable.garage;
                } else if(groupname[i].contains("terrace")){
                    groupicons[i] = R.drawable.terrace;
                } else{
                    groupicons[i] = R.drawable.firstfloor;
                }
            }
            listView = findViewById(R.id.list_group);
            groupview gmv = new groupview(this, groupname,groupicons);
            listView.setAdapter(gmv);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedgroup;
                    selectedgroup = groupname[position];
                    Intent intent = new Intent(Group.this, Items.class);
                    intent.putExtra("SelectedGroup", selectedgroup);
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout){
            String[]temp = cookiestring.split(":");
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
            Request request= new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Group.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Updatecookie();
                            Intent intent = new Intent(Group.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
