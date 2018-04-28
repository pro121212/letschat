package com.xinsane.letschat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketService extends Service implements Runnable {

    private Socket socket;
    private Thread thread;
    private DataInputStream reader;
    private DataOutputStream writer;
    private EventListener listener;
    private final Object writeLocker = new Object();

    public void setReceiveListener(EventListener listener) {
        if (this.listener == null)
            this.listener = listener;
    }

    public void send(final String json) {
        if (writer == null)
            return;
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (writeLocker) {
                        writer.writeInt(json.length());
                        writer.writeUTF(json);
                        writer.flush();
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
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        return new ServiceBinder();
    }

    @Override
    public void run() {
        if (socket == null)
            connect();
        try {
            while (true) {
                if (socket != null && socket.isConnected()) {
                    int size = reader.readInt();
                    String json = reader.readUTF();
                    if (json.length() != size)
                        throw new IOException("wrong size");
                    if (listener != null)
                        listener.onReceive(json);
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null)
                listener.onError(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            socket = new Socket();
            SocketAddress socAddress = new InetSocketAddress(
                    "127.0.0.1",
                    7212);
            socket.connect(socAddress, 3000);
            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null)
                listener.onError(e.getMessage());
        }
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
        void onReceive(String json);
        void onError(String msg);
    }
}
