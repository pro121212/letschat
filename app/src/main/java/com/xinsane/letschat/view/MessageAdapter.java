package com.xinsane.letschat.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinsane.letschat.R;
import com.xinsane.letschat.msg.Msg;
import com.xinsane.letschat.msg.item.OtherPhoto;
import com.xinsane.letschat.msg.item.SelfPhoto;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Msg> list;
    private MainActivity activity;

    MessageAdapter(MainActivity activity, List<Msg> list) {
        this.activity = activity;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (viewType == R.layout.item_other_photo) {
                    Intent intent = new Intent(activity, ViewPhotoActivity.class);
                    String filepath = ((OtherPhoto) list.get(position)).getFilepath();
                    if (filepath != null && !filepath.isEmpty()) {
                        intent.putExtra("filepath", filepath);
                        activity.startActivity(intent);
                    } else
                        notifyItemChanged(position);
                } else if (viewType == R.layout.item_self_photo) {
                    Intent intent = new Intent(activity, ViewPhotoActivity.class);
                    intent.putExtra("filepath", ((SelfPhoto) list.get(position)).getFilepath());
                    activity.startActivity(intent);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        list.get(position).draw(holder.view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).resource();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }
}
