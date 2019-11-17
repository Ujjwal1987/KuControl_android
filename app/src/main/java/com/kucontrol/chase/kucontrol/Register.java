package com.kucontrol.chase.kucontrol;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

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

import me.dm7.barcodescanner.zxing.ZXingScannerView;
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

import static android.app.PendingIntent.getActivity;

public class Register extends AppCompatActivity {
    EditText email,password,retypepassword;
    CardView scan,register;
    String username= null, password1= null, password2 = null, uuid = null, secret = null, Cookies = "", token = "";
    private HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    TextView scanresult;
    String searchstring = "<input type=\"hidden\" name=\"_csrf\" value=";
    String url = "https://www.ku-control.com/";
    private static final int PERMISSION_REQUEST_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else{
        email = (EditText) findViewById(R.id.Email);
        password = (EditText) findViewById(R.id.Password_register);
        retypepassword = (EditText) findViewById(R.id.Retype_password);
        scan = (CardView) findViewById(R.id.scan);
        register = (CardView) findViewById(R.id.register_account);
        scanresult = (TextView) findViewById(R.id.Scanresult);
        SharedPreferences settings = this.getSharedPreferences("QrCode", MODE_PRIVATE);
        String[] temp;
        String qrcode = settings.getString("Code", "no value");
        String permission = settings.getString("Permission", "NA");
        if (!(qrcode.contains(":"))) {
            scan.setEnabled(true);
        } else {
            scan.setEnabled(false);
            temp = qrcode.split(":");
            uuid = temp[0];
            secret = temp[1];
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Code", "");
            editor.commit();
        }
        if (permission.contains("NO")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Permission");
            alert.setMessage("Set permissions in application settings")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    });
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Permission", "");
            editor.commit();
        } else {

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

            } else {
                Updatecookie();
            }
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
                    Register.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            token = retreivetoken(html);
                        }
                    });
                }
            });
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username = email.getText().toString();
                    password1 = password.getText().toString();
                    password2 = retypepassword.getText().toString();
                    if (username.equals(null)) {
                        email.setError("Username field cannot be empty");
                    } else if (password1.equals(null)) {
                        password.setError("Password field cannot be empty");
                    } else if (password2.equals(null)) {
                        retypepassword.setError("Please retype the password");
                    } else if (!(password1.equals(password2))) {
                        retypepassword.setError("Password do not match");
                    } else if ((uuid.equals(null)) || (secret.equals(null))) {
                        Toast.makeText(getApplicationContext(), "Please scan the QR code on Gateway to Register", Toast.LENGTH_LONG).show();
                    } else {
                        final RequestBody requestBody = new FormBody.Builder()
                                .add("username", username)
                                .add("password", password1)
                                .add("_csrf", token)
                                .add("openhabuuid", uuid)
                                .add("openhabsecret", secret)
                                .add("agree", "on")
                                .build();

                        Request postrequest = new Request.Builder()
                                .url("https://ku-control.com/register")
                                .post(requestBody)
                                .build();
                        client.newCall(postrequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final String html = response.body().string();
                                Register.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temptoken = retreivetoken(html);
                                        Log.d("HTML", html);
                                        if (html.contains("UUID is already in use on another account.")) {
                                            email.setError("Gateway already registered");
                                        } else if (html.contains("A user with this e-mail is already registered.")) {
                                            email.setError("A user with this e-mail is already registered.");
                                        } else if (temptoken.equals("")) {
                                            Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT);
                                            Intent intent = new Intent(Register.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Register.this, scan_activity.class);
                    startActivity(intent);
                }
            });
        }
    }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();

                } else {
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onBackPressed() {
        finish();
        return;
    }

    public void Storecookie(HashMap<String, List<Cookie>> cookieStore){
        HttpUrl url = HttpUrl.parse("https://www.ku-control.com/");
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
    public String retreivetoken(String html){
        String token = "";
        if(html.contains(searchstring)){
            int startindex = html.indexOf(searchstring);
            int endindex = searchstring.length() + startindex;
            token = html.substring(endindex+1, endindex+37);
        }
        return token;
    }
    public void Updatecookie(){
        File dir = getFilesDir();
        File file = new File(dir, "cookies.dat");
        file.delete();
    }
}
