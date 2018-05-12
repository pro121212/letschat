package com.xinsane.letschat.data.item;

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
import com.xinsane.letschat.data.FileItem;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.data.Item;
import com.xinsane.letschat.view.ViewPhotoActivity;

import org.litepal.crud.DataSupport;

import java.io.File;

public class OtherPhoto extends DataSupport implements FileItem {
    private String info, filepath;

    @Override
    public int resource() {
        return R.layout.item_other_photo;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.infoView.setText(info);
        if (filepath == null) {
            holder.image.setImageURI("res:///" + R.drawable.ic_photo_black_120dp);
            return;
        }
        File file = new File(filepath);
        if (!file.exists()) {
            holder.image.setImageURI("res:///" + R.drawable.ic_photo_black_120dp);
            return;
        }
        Uri uri = Uri.fromFile(file);
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
        new Wrapper().setType("other_photo").setOtherPhoto(this).save();
        return is;
    }

    public static RecyclerView.ViewHolder onCreateViewHolder(View view, Item.Adapter adapter) {
        return new ViewHolder(view, adapter);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView infoView;
        private SimpleDraweeView image;

        private ViewHolder(View view, final Item.Adapter adapter) {
            super(view);
            infoView = view.findViewById(R.id.info);
            image = view.findViewById(R.id.image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取当前绑定的数据位置
                    int position = getAdapterPosition();

                    // 获取当前绑定的数据
                    OtherPhoto data = (OtherPhoto) adapter.getItemList().get(position);
                    String filepath = data.filepath;

                    // 获取上下文
                    Context context = adapter.getContext();

                    // 启动Activity查看图片
                    Intent intent = new Intent(adapter.getContext(), ViewPhotoActivity.class);
                    if (filepath != null && !filepath.isEmpty()) {
                        intent.putExtra("filepath", filepath);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }


    // Constructor、getter、setter
    public OtherPhoto(String info) {
        this.info = info;
    }
    public String getInfo() {
        return info;
    }
    public OtherPhoto setInfo(String info) {
        this.info = info;
        return this;
    }
    public String getFilepath() {
        return filepath;
    }
    public OtherPhoto setFilepath(String filepath) {
        this.filepath = filepath;
        return this;
    }
}
