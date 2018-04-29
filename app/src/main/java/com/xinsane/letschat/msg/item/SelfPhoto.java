package com.xinsane.letschat.msg.item;

import android.net.Uri;
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
import com.xinsane.letschat.msg.Msg;

import org.litepal.crud.DataSupport;

import java.io.File;

public class SelfPhoto extends DataSupport implements Msg {
    public static final int resource = R.layout.item_self_photo;

    private String info, filepath;

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

    @Override
    public int resource() {
        return resource;
    }

    @Override
    public void draw(View view) {
        TextView infoView = view.findViewById(R.id.info);
        infoView.setText(info);
        SimpleDraweeView image = view.findViewById(R.id.image);
        Uri uri = Uri.fromFile(new File(filepath));
        image.setImageURI(uri);
        int width = 480, height = 600;
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(image.getController())
                .setImageRequest(request)
                .build();
        image.setController(controller);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("self_photo").setSelfPhoto(this).save();
        return is;
    }
}
