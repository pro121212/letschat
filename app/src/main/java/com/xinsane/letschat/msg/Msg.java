package com.xinsane.letschat.msg;

import android.view.View;

public interface Msg {
    /**
     * 返回消息类型，即item的布局id
     * @return item的布局id
     */
    int resource();

    /**
     * 用于填充数据渲染视图
     * @param view 待渲染的view
     */
    void draw(View view);
}
