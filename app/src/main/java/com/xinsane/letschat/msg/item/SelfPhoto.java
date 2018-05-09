package com.xinsane.letschat.msg.item;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.xinsane.letschat.R;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.msg.Item;
import com.xinsane.letschat.view.ViewPhotoActivity;

import org.litepal.crud.DataSupport;

import java.io.File;

public class SelfPhoto extends DataSupport implements Item {
    private String info, filepath;

    @Override
    public int resource() {
        return R.layout.item_self_photo;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.infoView.setText(info);
        Uri uri = Uri.fromFile(new File(filepath));
        holder.image.setImageURI(uri);
        int width = 480, height = 600;
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(holder.image.getController())
                .setImageRequest(request)
                .build();
        holder.image.setController(controller);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("self_photo").setSelfPhoto(this).save();
        return is;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView infoView;
        SimpleDraweeView image;
        public ViewHolder(View view, final Item.Adapter adapter) {
            super(view);
            infoView = view.findViewById(R.id.info);
            image = view.findViewById(R.id.image);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取当前绑定的数据位置
                    int position = getAdapterPosition();

                    // 获取当前绑定的数据
                    SelfPhoto data = (SelfPhoto) adapter.getItemList().get(position);
                    String filepath = data.getFilepath();

                    // 获取上下文
                    Context context = adapter.getContext();

                    // 启动Activity查看图片
                    Intent intent = new Intent(adapter.getContext(), ViewPhotoActivity.class);
                    intent.putExtra("filepath", filepath);
                    context.startActivity(intent);
                }
            });
        }
    }


    // Constructor、getter、setter
    public SelfPhoto() { }
    public SelfPhoto(String info, String filepath) {
        this.info = info;
        this.filepath = filepath;
    }
    public String getInfo() {
        return info;
    }
    public SelfPhoto setInfo(String info) {
        this.info = info;
        return this;
    }
    public String getFilepath() {
        return filepath;
    }
    public SelfPhoto setFilepath(String filepath) {
        this.filepath = filepath;
        return this;
    }
}
