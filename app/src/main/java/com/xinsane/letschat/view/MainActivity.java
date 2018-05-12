package com.xinsane.letschat.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinsane.letschat.R;
import com.xinsane.letschat.data.FileItem;
import com.xinsane.letschat.database.Wrapper;
import com.xinsane.letschat.data.Item;
import com.xinsane.letschat.data.item.CenterTip;
import com.xinsane.letschat.data.item.OtherText;
import com.xinsane.letschat.data.item.SelfPhoto;
import com.xinsane.letschat.data.item.SelfText;
import com.xinsane.letschat.data.item.SelfVoice;
import com.xinsane.letschat.service.SocketService;
import com.xinsane.letschat.view.control.RecordButton;
import com.xinsane.util.LogUtil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SocketService.EventListener,
                    RecordButton.RecordListener {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_CHOOSE_PHOTO = 2;
    private static final int REQUEST_CONFIRM_PHOTO = 3;
    private static final int REQUEST_RECORD_AUDIO = 4;

    private SocketService service;
    private ServiceConnection connection;
    private List<Item> list = new ArrayList<>();
    private EditText editText;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private Uri takePhotoImage;

    private LinearLayout input_text;
    private LinearLayout input_audio;

    @Override
    public void onConnected() {
        handler.sendEmptyMessage(MessageHandler.EVENT_CONNECTED);
    }

    @Override
    public void onTextMessage(OtherText otherText) {
        Message message = new Message();
        message.what = MessageHandler.EVENT_TEXT_MESSAGE;
        message.obj = otherText;
        handler.sendMessage(message);
    }

    @Override
    public int onFileMessage(final FileItem fileItem) {
        int index = list.size();
        Message message = new Message();
        message.what = MessageHandler.EVENT_FILE_MESSAGE;
        message.obj = fileItem;
        handler.sendMessage(message);
        return index;
    }

    @Override
    public void onFileDownloaded(String filepath, int index) {
        Message message = new Message();
        message.what = MainActivity.MessageHandler.EVENT_FILE_MESSAGE;
        message.arg1 = index;
        handler.sendMessage(message);
    }

    @Override
    public void onTip(CenterTip centerTip) {
        Message message = new Message();
        message.what = MessageHandler.EVENT_TEXT_TIP;
        message.obj = centerTip;
        handler.sendMessage(message);
    }

    @Override
    public void onError(String msg) {
        Message message = new Message();
        message.what = MessageHandler.EVENT_ERROR;
        message.obj = msg;
        handler.sendMessage(message);
    }

    @Override
    public void onRecordComplete(String filename, long timeMillis) {
        if (timeMillis < 500) {
            Toast.makeText(this, "录音时间过短", Toast.LENGTH_SHORT).show();
            return;
        }
        // String text = "[语音] " + (int) Math.ceil(timeMillis / 1000.0) + "\"";
        String text = "[语音消息]";
        SelfVoice selfVoice = new SelfVoice().setInfo(service.getName()).setText(text).setFilepath(filename);
        service.sendFile(selfVoice, "amr");
        list.add(selfVoice);
        notifyPushOne();
        recyclerView.scrollToPosition(list.size() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化列表数据
        initList();

        // 加载并渲染列表
        recyclerView = findViewById(R.id.main_content);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, list);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(list.size() - 1);

        // 绑定服务并监听消息事件
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                service = ((SocketService.ServiceBinder) binder).getService();
                service.setReceiveListener(MainActivity.this);
                if (!service.isConnected())
                    service.requestReconnect();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                service = null;
            }
        };
        bindService(new Intent(this, SocketService.class),
                connection, Context.BIND_AUTO_CREATE);

        // 绑定发送消息事件
        editText = findViewById(R.id.text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                        && KeyEvent.ACTION_DOWN == event.getAction())) {
                    sendText();
                    return true;
                }
                return false;
            }
        });
        Button sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText();
            }
        });

        // 绑定拍照事件
        ImageView btn_take_photo = findViewById(R.id.btn_take_photo);
        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPhotoPermission(REQUEST_CHOOSE_PHOTO))
                    openCamera();
            }
        });

        // 绑定选择图片事件
        ImageView btn_choose_photo = findViewById(R.id.btn_choose_photo);
        btn_choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPhotoPermission(REQUEST_CHOOSE_PHOTO))
                    openAlbum();
            }
        });

        // 监听键盘弹起事件
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        final int keyHeight = screenHeight / 3;
        LinearLayout rootView = findViewById(R.id.root_view);
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight))
                    recyclerView.scrollToPosition(list.size() - 1);
            }
        });

        // 绑定文本和语音输入切换事件
        input_text = findViewById(R.id.input_text);
        input_audio = findViewById(R.id.input_audio);
        findViewById(R.id.btn_input_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRecordAudioPermission(REQUEST_RECORD_AUDIO)) {
                    input_text.setVisibility(View.GONE);
                    input_audio.setVisibility(View.VISIBLE);
                }
            }
        });
        findViewById(R.id.btn_input_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_audio.setVisibility(View.GONE);
                input_text.setVisibility(View.VISIBLE);
            }
        });

        // 设置录音完成监听器
        RecordButton recordButton = findViewById(R.id.btn_start_record);
        recordButton.setListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    File cacheDir = getExternalCacheDir();
                    if (cacheDir == null) {
                        Toast.makeText(this, "Can not open external cache dir.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String filepath = cacheDir + takePhotoImage.getPath();
                    Intent intent = new Intent(this, ConfirmPhotoActivity.class);
                    intent.putExtra("filepath", filepath);
                    startActivityForResult(intent, REQUEST_CONFIRM_PHOTO);
                }
                break;
            case REQUEST_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data == null)
                        return;
                    Uri uri = data.getData();
                    String filepath = null;
                    if (Build.VERSION.SDK_INT >= 19) {
                        if (uri == null) {
                            Toast.makeText(this, "Failed to get image!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (DocumentsContract.isDocumentUri(this, uri)) {
                            String docId = DocumentsContract.getDocumentId(uri);
                            String authority = uri.getAuthority();
                            if ("com.android.providers.media.documents".equals(authority)) {
                                String id = docId.split(":")[1];
                                String selection = MediaStore.Images.Media._ID + "=" + id;
                                filepath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                                filepath = getImagePath(contentUri, null);
                            }
                        } else if ("content".equalsIgnoreCase(uri.getScheme()))
                            filepath = getImagePath(uri, null);
                        else if ("file".equalsIgnoreCase(uri.getScheme()))
                            filepath = uri.getPath();
                    }
                    else
                        filepath = getImagePath(uri, null);
                    Intent intent = new Intent(this, ConfirmPhotoActivity.class);
                    intent.putExtra("filepath", filepath);
                    startActivityForResult(intent, REQUEST_CONFIRM_PHOTO);
                }
                break;
            case REQUEST_CONFIRM_PHOTO:
                if (resultCode == RESULT_OK) {
                    String filepath = data.getStringExtra("filepath");
                    SelfPhoto selfPhoto = new SelfPhoto(service.getName(), filepath);
                    service.sendFile(selfPhoto, "jpg");
                    list.add(selfPhoto);
                    notifyPushOne();
                    recyclerView.scrollToPosition(list.size() - 1);
                }
                break;
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }

    private boolean checkPhotoPermission(int request) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, request);
            return false;
        }
        return true;
    }

    private boolean checkRecordAudioPermission(int request) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO }, request);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_TAKE_PHOTO)
                openCamera();
            else if (requestCode == REQUEST_CHOOSE_PHOTO)
                openAlbum();
            else if (requestCode == REQUEST_RECORD_AUDIO) {
                input_text.setVisibility(View.GONE);
                input_audio.setVisibility(View.VISIBLE);
            }
        }
        else
            Toast.makeText(this, "You denied the permission!", Toast.LENGTH_SHORT).show();
    }

    private void openAlbum() {
        // 调用系统图库选照片
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }

    private void openCamera() {
        File dir = new File(getExternalCacheDir(), "provider");
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Toast.makeText(this, "create provider dir fail", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (dir.isFile()) {
            if (!dir.delete()) {
                Toast.makeText(this, "file provider exists and delete fail", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (!dir.mkdir()) {
                    Toast.makeText(this, "create provider dir fail", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        File outputImage = new File(getExternalCacheDir(), "provider/output_image.jpg");
        try {
            if (outputImage.exists()) {
                if (!outputImage.delete()) {
                    Toast.makeText(this, "file output_image.jpg exists and delete fail", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!outputImage.createNewFile()) {
                Toast.makeText(this, "file output_image.jpg create fail", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        takePhotoImage = FileProvider.getUriForFile(this, "com.xinsane.letschat.fileprovider", outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoImage);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void initList() {
        list.clear();
        List<Wrapper> wrappers = DataSupport.order("id desc").limit(20).find(Wrapper.class);
        if (wrappers.isEmpty())
            list.add(new CenterTip("正在连接到聊天室..."));
        else {
            for (int i = wrappers.size()-1; i>=0; --i) {
                Wrapper wrapper = wrappers.get(i);
                Item item = null;
                switch (wrapper.getType()) {
                    case "center_tip":
                        item = wrapper.getCenterTip();
                        break;
                    case "other_photo":
                        item = wrapper.getOtherPhoto();
                        break;
                    case "other_text":
                        item = wrapper.getOtherText();
                        break;
                    case "other_voice":
                        item = wrapper.getOtherVoice();
                        break;
                    case "self_photo":
                        item = wrapper.getSelfPhoto();
                        break;
                    case "self_text":
                        item = wrapper.getSelfText();
                        break;
                    case "self_voice":
                        item = wrapper.getSelfVoice();
                        break;
                }
                if (item != null)
                    list.add(item);
            }
            list.add(new CenterTip("以上为近期历史消息"));
        }
    }

    private void notifyPushOne() {
        adapter.notifyItemInserted(adapter.getItemCount());
    }

    private void sendText() {
        String text = editText.getText().toString();
        if (text.isEmpty())
            return;
        editText.setText("");
        SelfText selfText = new SelfText(service.getName(), text);
        list.add(selfText);
        notifyPushOne();
        recyclerView.scrollToPosition(list.size() - 1);
        service.sendText(selfText);
    }

    private MessageHandler handler = new MessageHandler(this);

    static class MessageHandler extends Handler {
        static final int EVENT_CONNECTED = 0x01;
        static final int EVENT_TEXT_MESSAGE = 0x02;
        static final int EVENT_FILE_MESSAGE = 0x03;
        static final int EVENT_TEXT_TIP = 0x04;
        static final int EVENT_ERROR = 0x05;

        private MainActivity activity;
        MessageHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            boolean scroll = !activity.recyclerView.canScrollVertically(1);
            switch (msg.what) {
                case EVENT_CONNECTED:
                    activity.list.add(new CenterTip("成功进入聊天室"));
                    activity.notifyPushOne();
                    break;
                case EVENT_TEXT_MESSAGE:
                    activity.list.add((OtherText) msg.obj);
                    activity.notifyPushOne();
                    break;
                case EVENT_FILE_MESSAGE:
                    if (msg.obj != null) {
                        activity.list.add((FileItem) msg.obj);
                        activity.notifyPushOne();
                    } else
                        activity.adapter.notifyItemChanged(msg.arg1);
                    break;
                case EVENT_TEXT_TIP:
                    activity.list.add((CenterTip) msg.obj);
                    activity.notifyPushOne();
                    break;
                case EVENT_ERROR:
                    activity.list.add(new CenterTip(msg.obj.toString()));
                    activity.notifyPushOne();
                    break;
            }
            if (scroll)
                activity.recyclerView.scrollToPosition(activity.list.size() - 1);
        }
    }
}
