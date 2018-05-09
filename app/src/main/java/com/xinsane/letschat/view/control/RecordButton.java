package com.xinsane.letschat.view.control;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinsane.letschat.R;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RecordButton extends AppCompatButton {
    enum RecordStatus {
        RECORD_OFF("按住 说话", R.drawable.ic_mic_off_white_64dp, ""),
        RECORD_ON("松开 发送", R.drawable.ic_mic_white_64dp, "手指上滑，取消发送"),
        RECORD_PRECANCEL("松开 取消", R.drawable.ic_mic_off_white_64dp, "松开手指，取消发送");

        private final String btn_text;
        private final int dialog_image;
        private final String dialog_text;
        RecordStatus(String btn_text, int dialog_image, String dialog_text) {
            this.btn_text = btn_text;
            this.dialog_image = dialog_image;
            this.dialog_text = dialog_text;
        }
    }
    private RecordStatus status = RecordStatus.RECORD_OFF;
    private Context context;
    private Dialog dialog;
    private ImageView dialog_image;
    private TextView dialog_text;
    private float touchDownY;

    private MediaRecorder recorder;
    private RecordListener listener;
    private File directory;
    private String filename;

    public void setListener(RecordListener listener) {
        this.listener = listener;
    }

    private void init() {
        directory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (directory == null)
            Toast.makeText(context, "无法获取录音文件目录", Toast.LENGTH_SHORT).show();
    }

    public RecordButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void updateStatus() {
        setText(status.btn_text);
        if (dialog == null) {
            dialog = new Dialog(context, R.style.DialogStyle);
            dialog.setContentView(R.layout.dialog_record);
            dialog_image = dialog.findViewById(R.id.image);
            dialog_text = dialog.findViewById(R.id.text);
        }
        if (status == RecordStatus.RECORD_OFF)
            dialog.hide();
        else {
            dialog_image.setImageResource(status.dialog_image);
            dialog_text.setText(status.dialog_text);
            dialog.show();
        }
    }

    private void startRecord() {
        try {
            filename = directory.getPath() + "/" + UUID.randomUUID().toString().replace("-", "") + ".amr";
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            recorder.setOutputFile(filename);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        RecordStatus old_status = status;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                status = RecordStatus.RECORD_ON;
                touchDownY = event.getY();
                if (old_status == RecordStatus.RECORD_OFF)
                    startRecord();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY();
                if (touchDownY - moveY > 60)
                    status = RecordStatus.RECORD_PRECANCEL;
                else if (touchDownY - moveY < 40)
                    status = RecordStatus.RECORD_ON;
                break;
            case MotionEvent.ACTION_UP:
                status = RecordStatus.RECORD_OFF;
                recorder.stop();
                recorder.release();
                recorder = null;
                if (old_status == RecordStatus.RECORD_ON && listener != null)
                    listener.onRecordComplete(filename);
                performClick();
                break;
        }
        if (old_status != status)
            updateStatus();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public interface RecordListener {
        void onRecordComplete(String filename);
    }
}
