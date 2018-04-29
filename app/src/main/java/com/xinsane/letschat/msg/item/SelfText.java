package com.xinsane.letschat.msg.item;

import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.msg.Msg;

import org.litepal.crud.DataSupport;

public class SelfText extends DataSupport implements Msg {
    public static final int resource = R.layout.item_self_text;

    private String info, text;

    public SelfText() { }
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

    @Override
    public int resource() {
        return resource;
    }

    @Override
    public void draw(View view) {
        TextView infoView = view.findViewById(R.id.info);
        infoView.setText(info);
        TextView textView = view.findViewById(R.id.text);
        textView.setText(text);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("self_text").setSelfText(this).save();
        return is;
    }
}
