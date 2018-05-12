package com.xinsane.letschat.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinsane.letschat.R;
import com.xinsane.letschat.data.Item;
import com.xinsane.letschat.data.item.CenterTip;
import com.xinsane.letschat.data.item.OtherPhoto;
import com.xinsane.letschat.data.item.OtherText;
import com.xinsane.letschat.data.item.OtherVoice;
import com.xinsane.letschat.data.item.SelfPhoto;
import com.xinsane.letschat.data.item.SelfText;
import com.xinsane.letschat.data.item.SelfVoice;
import com.xinsane.util.LogUtil;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    private List<Item> list;
    private MainActivity activity;

    MessageAdapter(MainActivity activity, List<Item> list) {
        this.activity = activity;
        this.list = list;
    }

    // Item适配器
    private Item.Adapter itemAdapter = new Item.Adapter() {
        @Override
        public Context getContext() {
            return activity;
        }
        @Override
        public RecyclerView.Adapter getAdapter() {
            return MessageAdapter.this;
        }
        @Override
        public List<Item> getItemList() {
            return list;
        }
    };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        LogUtil.d("onCreateViewHolder:" + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        switch (viewType) {
            case R.layout.item_center_tip:
                return CenterTip.onCreateViewHolder(view);
            case R.layout.item_other_photo:
                return OtherPhoto.onCreateViewHolder(view, itemAdapter);
            case R.layout.item_other_text:
                return OtherText.onCreateViewHolder(view);
            case R.layout.item_other_voice:
                return OtherVoice.onCreateViewHolder(view, itemAdapter);
            case R.layout.item_self_photo:
                return SelfPhoto.onCreateViewHolder(view, itemAdapter);
            case R.layout.item_self_text:
                return SelfText.onCreateViewHolder(view);
            case R.layout.item_self_voice:
                return SelfVoice.onCreateViewHolder(view, itemAdapter);
        }
        throw new RuntimeException("catch wrong viewType");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LogUtil.d("onBindViewHolder:" + position);
        list.get(position).onBindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).resource();
    }
}
