package com.xinsane.letschat.data.item;

import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xinsane.letschat.R;
import com.xinsane.letschat.data.FileItem;
import com.xinsane.letschat.data.Item;
import com.xinsane.letschat.database.Wrapper;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.IOException;

public class OtherVoice extends DataSupport implements FileItem {
    private String info, filepath, text;

    @Column(ignore = true)
    private MediaPlayer player;

    @Override
    public int resource() {
        return R.layout.item_other_voice;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.infoView.setText(info);
        if (player != null && player.isPlaying())
            holder.textView.setText("正在播放...");
        else
            holder.textView.setText(text);
    }

    @Override
    public synchronized boolean save() {
        boolean is = super.save();
        new Wrapper().setType("other_voice").setOtherVoice(this).save();
        return is;
    }

    public static RecyclerView.ViewHolder onCreateViewHolder(View view, Item.Adapter adapter) {
        return new ViewHolder(view, adapter);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView infoView, textView;

        private ViewHolder(View view, final Item.Adapter adapter) {
            super(view);
            infoView = view.findViewById(R.id.info);
            textView = view.findViewById(R.id.text);
            view.findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取当前绑定的数据位置
                    int position = getAdapterPosition();

                    // 获取当前绑定的数据
                    final OtherVoice data = (OtherVoice) adapter.getItemList().get(position);

                    try {
                        // 准备播放
                        if (data.player == null) {
                            data.player = new MediaPlayer();
                            data.player.setDataSource(data.filepath);
                            data.player.setLooping(false);
                            data.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    data.player.stop();
                                    data.player.release();
                                    data.player = null;
                                    textView.setText(data.text);
                                }
                            });
                        }

                        // 如果正在播放则停止播放
                        else if (data.player.isPlaying()) {
                            data.player.stop();
                            data.player.release();
                            data.player = null;
                            textView.setText(data.text);
                            return;
                        }

                        // 开始播放
                        data.player.prepare();
                        data.player.start();
                        textView.setText("正在播放...");
                    } catch (IOException e) {
                        e.printStackTrace();
                        data.player = null;
                        textView.setText(data.text);
                        Toast.makeText(adapter.getContext(), "无法播放音频", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    // Constructor、getter、setter
    public OtherVoice(String info) {
        this.info = info;
    }
    public String getInfo() {
        return info;
    }
    public OtherVoice setInfo(String info) {
        this.info = info;
        return this;
    }
    public String getText() {
        return text;
    }
    public OtherVoice setText(String text) {
        this.text = text;
        return this;
    }
    public String getFilepath() {
        return filepath;
    }
    public OtherVoice setFilepath(String filepath) {
        this.filepath = filepath;
        return this;
    }
}
