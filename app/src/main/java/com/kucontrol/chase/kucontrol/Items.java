package com.kucontrol.chase.kucontrol;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Items extends AppCompatActivity {
    String cookiestring;
    Response response;
    SwipeRefreshLayout swipeRefreshLayout;
    String json;
    JSONArray jsonArray;
    JSONObject jsonObject;
    ProgressBar progressBar;
    String selectedgroup;
    private Handler handler = new Handler();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        Intent intent = getIntent();
        selectedgroup = intent.getStringExtra("SelectedGroup");
        swipeRefreshLayout = findViewById(R.id.Item_refresh);
        cookiestring = retreivecookies();
        progressBar = findViewById(R.id.Progress_Items);
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://ku-control.com/rest/items/" + selectedgroup;
        String[]temp = cookiestring.split(":");
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    jsonObject = new JSONObject(response.body().string());
                    jsonArray = jsonObject.getJSONArray("members");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Items.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Listview(jsonArray);
                    }
                });
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String url = "https://ku-control.com/rest/items/" + selectedgroup;
                String[]temp = cookiestring.split(":");
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
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        try {
                            jsonObject = new JSONObject(response.body().string());
                            jsonArray = jsonObject.getJSONArray("members");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Items.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                Listview(jsonArray);
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

    public void Listview(JSONArray items){
//        HashMap<String, String> sitemapname = new HashMap<>();
        final ListView listView;
        final String [] itemname = new String[items.length()];
        final String [] status = new String[items.length()];
        final String [] itemchangename = new String[items.length()];
        try {
            for(int i=0; i<items.length(); i++){
                JSONObject temp = items.getJSONObject(i);
                itemname[i] = temp.getString("label");
                status[i] = temp.getString("state");
                itemchangename[i]=temp.getString("name");
            }
            listView = findViewById(R.id.Item_List);
            itemview imv = new itemview(itemname, status, this);
            listView.setAdapter(imv);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selecteditem;
                    selecteditem = itemname[position];
                    ChangeState cg = new ChangeState(status[position],itemchangename[position], cookiestring);
                    cg.changestate();
                }
            });

            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String url = "https://ku-control.com/rest/items/" + selectedgroup;
                    String[]temp = cookiestring.split(":");
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
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            try {
                                jsonObject = new JSONObject(response.body().string());
                                jsonArray = jsonObject.getJSONArray("members");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Items.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Listview(jsonArray);
                                }
                            });
                        }
                    });
                }
            },100);

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    Items.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Updatecookie();
                            Intent intent = new Intent(Items.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}