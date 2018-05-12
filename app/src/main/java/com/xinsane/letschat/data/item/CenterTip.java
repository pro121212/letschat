package com.xinsane.letschat.data.item;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.data.Item;

import org.litepal.crud.DataSupport;

public class CenterTip extends DataSupport implements Item {
    private String tip;

    @Override
    public int resource() {
        return R.layout.item_center_tip;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.tipView.setText(tip);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("center_tip").setCenterTip(this).save();
        return is;
    }

    public static RecyclerView.ViewHolder onCreateViewHolder(View view) {
        return new ViewHolder(view);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tipView;

        private ViewHolder(View view) {
            super(view);
            tipView = view.findViewById(R.id.tip);
        }
    }

    // Constructor、getter、setter
    public CenterTip(String tip) {
        this.tip = tip;
    }
    public String getTip() {
        return tip;
    }
    public CenterTip setTip(String tip) {
        this.tip = tip;
        return this;
    }
}
