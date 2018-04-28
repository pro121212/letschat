package com.xinsane.letschat.pojo.item;

import android.view.View;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.pojo.Msg;

public class CenterTip implements Msg {
    public static final int resource = R.layout.item_center_tip;

    private String tip;

    public CenterTip(String tip) {
        this.tip = tip;
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
}
