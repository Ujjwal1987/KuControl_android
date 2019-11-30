package com.kucontrol.chase.kucontrol;

import android.app.Application;

public class GlobalClass extends Application {
    private String url = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
