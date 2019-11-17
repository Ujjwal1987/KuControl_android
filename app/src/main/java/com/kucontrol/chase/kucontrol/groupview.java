package com.kucontrol.chase.kucontrol;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class groupview extends ArrayAdapter<String> {

    private String[] groupname;
    private Integer[] groupicons;
    private Activity context;
    public groupview(Activity context, String[] groupname, Integer[] groupicons) {
        super(context, R.layout.layout_group, groupname);
        this.context = context;
        this.groupname = groupname;
        this.groupicons = groupicons;
    }
    @NonNull
    @Override
    public View getView(@NonNull int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View r = convertView;
        Viewholder viewholder;
        if(r==null){
            LayoutInflater layoutInflater=context.getLayoutInflater();
            r=layoutInflater.inflate(R.layout.layout_group,null, true);
            viewholder = new Viewholder(r);
            r.setTag(viewholder);
        }else{
            viewholder = (Viewholder) r.getTag();

        }
        viewholder.imageView.setImageResource(groupicons[position]);
        viewholder.textView.setText(groupname[position]);
        return r;
    }

    class Viewholder{
        TextView textView;
        ImageView imageView;
        Viewholder(View view)
        {
            textView = view.findViewById(R.id.groupname);
            imageView = view.findViewById(R.id.groupicons);
        }
    }

}
