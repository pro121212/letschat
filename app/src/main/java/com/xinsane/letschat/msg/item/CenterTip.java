package com.xinsane.letschat.msg.item;

import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.msg.Msg;

import org.litepal.crud.DataSupport;

public class CenterTip extends DataSupport implements Msg {
    public static final int resource = R.layout.item_center_tip;

    private String tip;

    public CenterTip() { }
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

    @Override
    public int resource() {
        return resource;
    }

    @Override
    public void draw(View view) {
        TextView tipView = view.findViewById(R.id.tip);
        tipView.setText(tip);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("center_tip").setCenterTip(this).save();
        return is;
    }
}
