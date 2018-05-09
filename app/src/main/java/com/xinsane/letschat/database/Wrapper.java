package com.xinsane.letschat.database;

import com.xinsane.letschat.msg.item.CenterTip;
import com.xinsane.letschat.msg.item.OtherPhoto;
import com.xinsane.letschat.msg.item.OtherText;
import com.xinsane.letschat.msg.item.SelfPhoto;
import com.xinsane.letschat.msg.item.SelfText;
import com.xinsane.letschat.msg.item.SelfVoice;

import org.litepal.crud.DataSupport;

import java.util.List;

public class Wrapper extends DataSupport {
    private long id;
    private String type;
    private CenterTip centerTip;
    private OtherPhoto otherPhoto;
    private OtherText otherText;
    private SelfText selfText;
    private SelfPhoto selfPhoto;
    private SelfVoice selfVoice;

    public long getId() {
        return id;
    }
    public Wrapper setId(long id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }
    public Wrapper setType(String type) {
        this.type = type;
        return this;
    }

    public CenterTip getCenterTip() {
        if (centerTip == null) {
            String linkId = this.getClass().getSimpleName().toLowerCase();
            List<CenterTip> list = DataSupport.where(linkId + "_id=?", String.valueOf(id))
                    .find(CenterTip.class);
            centerTip = list.isEmpty() ? null : list.get(0);
        }
        return centerTip;
    }
    public Wrapper setCenterTip(CenterTip centerTip) {
        this.centerTip = centerTip;
        return this;
    }

    public OtherPhoto getOtherPhoto() {
        if (otherPhoto == null) {
            String linkId = this.getClass().getSimpleName().toLowerCase();
            List<OtherPhoto> list = DataSupport.where(linkId + "_id=?", String.valueOf(id))
                    .find(OtherPhoto.class);
            otherPhoto = list.isEmpty() ? null : list.get(0);
        }
        return otherPhoto;
    }
    public Wrapper setOtherPhoto(OtherPhoto otherPhoto) {
        this.otherPhoto = otherPhoto;
        return this;
    }

    public OtherText getOtherText() {
        if (otherText == null) {
            String linkId = this.getClass().getSimpleName().toLowerCase();
            List<OtherText> list = DataSupport.where(linkId + "_id=?", String.valueOf(id))
                    .find(OtherText.class);
            otherText = list.isEmpty() ? null : list.get(0);
        }
        return otherText;
    }
    public Wrapper setOtherText(OtherText otherText) {
        this.otherText = otherText;
        return this;
    }

    public SelfText getSelfText() {
        if (selfText == null) {
            String linkId = this.getClass().getSimpleName().toLowerCase();
            List<SelfText> list = DataSupport.where(linkId + "_id=?", String.valueOf(id))
                    .find(SelfText.class);
            selfText = list.isEmpty() ? null : list.get(0);
        }
        return selfText;
    }
    public Wrapper setSelfText(SelfText selfText) {
        this.selfText = selfText;
        return this;
    }

    public SelfPhoto getSelfPhoto() {
        if (selfPhoto == null) {
            String linkId = this.getClass().getSimpleName().toLowerCase();
            List<SelfPhoto> list = DataSupport.where(linkId + "_id=?", String.valueOf(id))
                    .find(SelfPhoto.class);
            selfPhoto = list.isEmpty() ? null : list.get(0);
        }
        return selfPhoto;
    }
    public Wrapper setSelfPhoto(SelfPhoto selfPhoto) {
        this.selfPhoto = selfPhoto;
        return this;
    }

    public SelfVoice getSelfVoice() {
        if (selfVoice == null) {
            String linkId = this.getClass().getSimpleName().toLowerCase();
            List<SelfVoice> list = DataSupport.where(linkId + "_id=?", String.valueOf(id))
                    .find(SelfVoice.class);
            selfVoice = list.isEmpty() ? null : list.get(0);
        }
        return selfVoice;
    }
    public Wrapper setSelfVoice(SelfVoice selfVoice) {
        this.selfVoice = selfVoice;
        return this;
    }
}
