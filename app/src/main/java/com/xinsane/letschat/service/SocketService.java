package com.xinsane.letschat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xinsane.letschat.msg.item.CenterTip;
import com.xinsane.letschat.msg.item.OtherPhoto;
import com.xinsane.letschat.msg.item.OtherText;
import com.xinsane.letschat.msg.item.SelfPhoto;
import com.xinsane.letschat.msg.item.SelfText;
import com.xinsane.letschat.protocol.MessageType;
import com.xinsane.letschat.util.DataIOUtil;
import com.xinsane.letschat.util.Location;
import com.xinsane.letschat.util.Name;
import com.xinsane.letschat.util.PhotoCompress;
import com.xinsane.util.LogUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;

public class SocketService extends Service implements Runnable {
    private static final String host = "aliyun.xinsane.com";
    private static final int port = 7214;

    private Thread thread;
    private Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private EventListener listener;
    private final Object writeLocker = new Object();
    private String city = null;
    private String name = "我";
    private int reconnectTime = 1;
    private int reconnects = 0;

    public String getName() {
        return name;
    }

    public void setReceiveListener(EventListener listener) {
        if (this.listener == null)
            this.listener = listener;
    }

    public boolean isConnected() {
        return thread != null && thread.isAlive() &&
                socket != null && socket.isConnected();
    }

    @Override
    public void onCreate() {
        thread = new Thread(this);
        thread.start();
    }

    public void sendText(final SelfText selfText) {
        if (writer == null)
            return;
        new Thread() {
            @Override
            public void run() {
                selfText.save();
                try {
                    synchronized (writeLocker) {
                        writer.writeByte(MessageType.TEXT);
                        DataIOUtil.sendString(writer, selfText.getText());
                        writer.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null)
                        listener.onError("消息发送失败");
                }
            }
        }.start();
    }

    public void sendImage(final String filePath, final String destFilePath) {
        if (writer == null)
            return;
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        if (listener != null)
                            listener.onError("图片发送失败：File Not Found");
                        return;
                    }
                    file = new File(destFilePath);
                    OutputStream outputStream = new FileOutputStream(file);
                    PhotoCompress.compress(filePath, outputStream);
                    outputStream.close();
                    new SelfPhoto().setFilepath(destFilePath).setInfo(name).save(); // 保存数据
                    synchronized (writeLocker) {
                        writer.writeByte(MessageType.FILE);
                        DataIOUtil.sendString(writer, "jpg");
                        FileInputStream fileInputStream = new FileInputStream(file);
                        DataIOUtil.sendFile(writer, fileInputStream);
                        fileInputStream.close();
                        writer.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null)
                        listener.onError("图片发送失败：IOException");
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (writer == null)
            return;
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (writeLocker) {
                        writer.writeByte(MessageType.EXIT);
                        writer.flush();
                        writer.close();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null)
                        listener.onError(e.getMessage());
                }
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        listener = null;
        return super.onUnbind(intent);
    }

    private void preReconnect() {
        if (reconnects >= 10) {
            if (listener != null)
                listener.onError("重连次数过多，取消连接");
            return;
        }
        reconnectTime *= 2;
        if (reconnectTime > 30)
            reconnectTime = 30;
        if (listener != null)
            listener.onError("断开连接，" + reconnectTime + " 秒后尝试重连");
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(reconnectTime * 1000);
                    reconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void requestReconnect() {
        if (reconnects >= 10)
            reconnect();
        else if (listener != null)
            listener.onTip(new CenterTip("等待重新连接..."));
    }

    private void reconnect() {
        if (thread == null || !thread.isAlive()) {
            reconnects ++;
            CenterTip centerTip = new CenterTip("正在重新连接...");
            if (listener != null)
                listener.onTip(centerTip);
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        try {
            if (socket != null && socket.isConnected())
                socket.close();
            if (!connect())
                return;
            reconnectTime = 1;
            reconnects = 0;
            city = Location.getCity();
            if (city != null) {
                name = Name.getRandomName() + " · " + city;
                CenterTip centerTip = new CenterTip("你的匿名信息为：" + name);
                centerTip.save();
                if (listener != null)
                    listener.onTip(centerTip);
                synchronized (writeLocker) {
                    writer.writeByte(MessageType.LOGIN);
                    DataIOUtil.sendString(writer, name);
                    writer.flush();
                }
            } else {
                Location.setListener(new Location.Listener() {
                    @Override
                    public void onLocation(String city) {
                        SocketService.this.city = city;
                        name = Name.getRandomName() + " · " + city;
                        CenterTip centerTip = new CenterTip("你的匿名信息为：" + name);
                        centerTip.save();
                        if (listener != null)
                            listener.onTip(centerTip);
                        synchronized (writeLocker) {
                            try {
                                writer.writeByte(MessageType.LOGIN);
                                DataIOUtil.sendString(writer, name);
                                writer.flush();
                            } catch (IOException e) {
                                LogUtil.e(e.getMessage(), "UpdateName");
                            }
                        }
                    }
                });
            }
            while (isConnected()) {
                int type = reader.readByte();
                switch (type) {
                    case MessageType.LOGIN: {
                        String tip = DataIOUtil.receiveString(reader) + "进入了聊天室";
                        CenterTip centerTip = new CenterTip(tip);
                        if (listener != null)
                            listener.onTip(centerTip);
                        centerTip.save();
                        break;
                    }
                    case MessageType.TEXT: {
                        String user = DataIOUtil.receiveString(reader);
                        String text = DataIOUtil.receiveString(reader);
                        OtherText otherText = new OtherText(user, text);
                        if (listener != null)
                            listener.onTextMessage(otherText);
                        otherText.save();
                        break;
                    }
                    case MessageType.FILE_ID: {
                        String user = DataIOUtil.receiveString(reader);
                        String token = DataIOUtil.receiveString(reader);
                        int index = -1;
                        OtherPhoto otherPhoto = new OtherPhoto(user);
                        if (listener != null)
                            index = listener.onFileMessage(otherPhoto);
                        downloadImage(otherPhoto, token, index);
                        break;
                    }
                    case MessageType.EXIT: {
                        String tip = DataIOUtil.receiveString(reader) + "退出了聊天室";
                        CenterTip centerTip = new CenterTip(tip);
                        if (listener != null)
                            listener.onTip(centerTip);
                        centerTip.save();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            preReconnect();
        }
    }

    private void downloadImage(final OtherPhoto otherPhoto, final String token, final int index) {
        new Thread() {
            @Override
            public void run() {
                Socket socket;
                try {
                    File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                    if (dir == null) {
                        LogUtil.e("Can not access external files dir.");
                        return;
                    }
                    final File file = new File(dir + "/" +
                            UUID.randomUUID().toString().replace("-", "") + ".jpg");

                    socket = new Socket(host, port);
                    socket.setSoTimeout(10000);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    out.writeByte(MessageType.FILE_ID);
                    DataIOUtil.sendString(out, token);
                    out.flush();
                    byte type = in.readByte();
                    if (type != MessageType.FILE)
                        LogUtil.e("wrong type(expect FILE): " + type);
                    DataIOUtil.receiveString(in);

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    DataIOUtil.receiveFile(in, fileOutputStream);
                    fileOutputStream.close();

                    otherPhoto.setFilepath(file.getAbsolutePath()).save(); // 保存数据

                    if (listener != null && index >= 0)
                        listener.onFileDownloaded(file.getAbsolutePath(), index);

                    out.writeByte(MessageType.EXIT);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private boolean connect() {
        try {
            socket = new Socket();
            SocketAddress socAddress = new InetSocketAddress(
                    host,
                    port);
            socket.connect(socAddress, 3000);
            if (!socket.isConnected())
                throw new Exception("无法连接服务器!");
            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
            if (listener != null)
                listener.onConnected();
        } catch (Exception e) {
            e.printStackTrace();
            preReconnect();
            return false;
        }
        return true;
    }

    public class ServiceBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    /**
     * 此接口中的方法回调于子线程，不能在此方法中操作UI
     */
    public interface EventListener {
        void onConnected();
        void onTextMessage(OtherText otherText);
        int onFileMessage(OtherPhoto otherPhoto);
        void onFileDownloaded(String filepath, int index);
        void onTip(CenterTip centerTip);
        void onError(String msg);
    }
}
