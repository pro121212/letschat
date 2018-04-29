package com.xinsane.letschat.msg.item;

import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.msg.Msg;

import org.litepal.crud.DataSupport;

public class OtherText extends DataSupport implements Msg {
    public static final int resource = R.layout.item_other_text;

    private String info, text;

    public OtherText() { }
    public OtherText(String info, String text) {
        this.info = info;
        this.text = text;
    }

    public String getInfo() {
        return info;
    }
    public OtherText setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getText() {
        return text;
    }
    public OtherText setText(String text) {
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
        new Wrapper().setType("other_text").setOtherText(this).save();
        return is;
    }
}
