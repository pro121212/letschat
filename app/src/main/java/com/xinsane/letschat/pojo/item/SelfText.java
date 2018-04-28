package com.xinsane.letschat.pojo.item;

import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.pojo.Msg;

public class SelfText implements Msg {
    public static final int resource = R.layout.item_self_text;

    private String info, text;

    public SelfText(String info, String text) {
        this.info = info;
        this.text = text;
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
}
