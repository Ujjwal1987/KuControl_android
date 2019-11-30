package com.kucontrol.chase.kucontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class itemview extends ArrayAdapter<String> {
    private String[] itemname;
    private String[] status;
    private Activity context;

    public itemview(String[] itemname, String[] status, Activity context) {
        super(context, R.layout.layout_items, itemname);
        this.status = status;
        this.itemname = itemname;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View r = convertView;
        Viewholder viewholder;
        if(r==null){
            LayoutInflater layoutInflater=context.getLayoutInflater();
            r=layoutInflater.inflate(R.layout.layout_items,null, true);
            viewholder = new Viewholder(r);
            r.setTag(viewholder);
        }else{
            viewholder = (Viewholder) r.getTag();
        }
        viewholder.textView.setText(itemname[position]);
        Log.d("getview", "getView: " + status[0]);
        if(status[position].equals("ON")){
            viewholder.relativeLayout.setBackgroundColor(Color.rgb(255, 165, 0));
        }else{
            Log.d("getview", "getView: i am here else");
            viewholder.relativeLayout.setBackgroundColor(Color.WHITE);
            //viewholder.relativeLayout.setBackgroundColor(Color.rgb(32,178,170));
        }
        return r;
    }
    class Viewholder{
        TextView textView;
        RelativeLayout relativeLayout;
        Viewholder(View view)
        {
            textView = view.findViewById(R.id.Item_name);
            relativeLayout = view.findViewById(R.id.itemrelativelayout);
        }
    }
}
