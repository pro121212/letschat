package com.xinsane.letschat.data.item;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.data.Item;

import org.litepal.crud.DataSupport;

public class SelfText extends DataSupport implements Item {
    private String info, text;

    @Override
    public int resource() {
        return R.layout.item_self_text;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.infoView.setText(info);
        holder.textView.setText(text);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("self_text").setSelfText(this).save();
        return is;
    }

    public static RecyclerView.ViewHolder onCreateViewHolder(View view) {
        return new ViewHolder(view);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView infoView, textView;

        private ViewHolder(View view) {
            super(view);
            infoView = view.findViewById(R.id.info);
            textView = view.findViewById(R.id.text);
        }
    }


    // Constructor、getter、setter
    public SelfText(String info, String text) {
        this.info = info;
        this.text = text;
    }
    public String getInfo() {
        return info;
    }
    public SelfText setInfo(String info) {
        this.info = info;
        return this;
    }
    public String getText() {
        return text;
    }
    public SelfText setText(String text) {
        this.text = text;
        return this;
    }
}
