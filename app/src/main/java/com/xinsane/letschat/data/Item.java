package com.xinsane.letschat.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView通用Item接口
 */
public interface Item {
    /**
     * 返回item的布局id作为视图类型
     * @return item的布局id
     */
    int resource();

    /**
     * 通知适配器重新渲染视图，只能在RecyclerView适配器的onBindViewHolder方法中调用
     * @param viewHolder 与视图相关的ViewHolder
     */
    void onBindViewHolder(RecyclerView.ViewHolder viewHolder);


    /**
     * Item适配器，用于Item中获取RecyclerView适配器等相关信息
     */
    interface Adapter {
        /**
         * 获取Context
         * @return RecyclerView适配器的上下文，通常为父级activity或fragment
         */
        Context getContext();

        /**
         * 获取RecyclerView适配器
         * @return RecyclerView适配器
         */
        RecyclerView.Adapter getAdapter();

        /**
         * 获取数据列表
         * @return Item数据列表
         */
        List<Item> getItemList();
    }
}
