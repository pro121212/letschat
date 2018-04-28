package com.xinsane.letschat.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xinsane.letschat.R;
import com.xinsane.letschat.pojo.Msg;
import com.xinsane.letschat.pojo.item.CenterTip;
import com.xinsane.letschat.pojo.item.OtherText;
import com.xinsane.letschat.pojo.item.SelfText;
import com.xinsane.letschat.service.SocketService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SocketService.EventListener {

    private SocketService service;
    private ServiceConnection connection;
    private List<Msg> list = new ArrayList<>();
    private EditText editText;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    @Override
    public void onReceive(String json) {
        Message message = new Message();
        message.what = MessageHandler.EVENT_RECEIVE;
        message.obj = json;
        handler.sendMessage(message);
    }

    @Override
    public void onError(String msg) {
        Message message = new Message();
        message.what = MessageHandler.EVENT_RECEIVE;
        message.obj = msg;
        handler.sendMessage(message);
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
        adapter = new MessageAdapter(list);
        recyclerView.setAdapter(adapter);

        // 开启服务并监听消息事件
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                service = ((SocketService.ServiceBinder) binder).getService();
                service.setReceiveListener(MainActivity.this);
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
                    send();
                    return true;
                }
                return false;
            }
        });
        Button sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void initList() {
        list.add(new CenterTip("你已加入聊天室"));
        list.add(new OtherText("hhh", "想啥呢~"));
    }

    private void send() {
        String text = editText.getText().toString();
        if (text.isEmpty())
            return;
        editText.setText("");
        list.add(new SelfText("我", text));
        adapter.notifyItemInserted(list.size() - 1);
        list.add(new OtherText("hhh", "是的没错"));
        adapter.notifyItemInserted(list.size() - 1);
        recyclerView.scrollToPosition(list.size() - 1);
    }

    private MessageHandler handler = new MessageHandler(this);

    static class MessageHandler extends Handler {
        static final int EVENT_RECEIVE = 0x01;
        static final int EVENT_ERROR = 0x02;
        private MainActivity activity;
        MessageHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RECEIVE:
                    break;
                case EVENT_ERROR:
                    break;
            }
        }
    }
}
