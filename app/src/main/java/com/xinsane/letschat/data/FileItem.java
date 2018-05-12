package com.xinsane.letschat.data;

public interface FileItem extends Item {
    FileItem setFilepath(String filepath);
    FileItem setInfo(String info);
    String getFilepath();
    boolean save();
}
