package com.kucontrol.chase.kucontrol;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;

public class sitemapview extends ArrayAdapter<String> {
    private String[] sitemapname;
    private Integer[] sitemapicons;
    private Activity context;
    public sitemapview(Activity context, String[] sitemapname, Integer[] sitemapicons) {
        super(context, R.layout.layout, sitemapname);
        this.context = context;
        this.sitemapname = sitemapname;
        this.sitemapicons = sitemapicons;
    }

    @NonNull
    @Override
    public View getView(@NonNull int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View r = convertView;
        Viewholder viewholder;
        if(r==null){
            LayoutInflater layoutInflater=context.getLayoutInflater();
            r=layoutInflater.inflate(R.layout.layout,null, true);
            viewholder = new Viewholder(r);
            r.setTag(viewholder);
        }else{
            viewholder = (Viewholder) r.getTag();

        }
        viewholder.imageView.setImageResource(sitemapicons[position]);
        viewholder.textView.setText(sitemapname[position]);
        return r;
    }

    class Viewholder{
        TextView textView;
        ImageView imageView;
        Viewholder(View view)
        {
            textView = view.findViewById(R.id.sitemapname);
            imageView = view.findViewById(R.id.sitemapicon);
        }
    }
}
