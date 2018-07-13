package com.jin.chat1;

import android.graphics.drawable.Drawable;
import android.widget.Adapter;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class Friend implements MultiItemEntity {
    public String friendName;
    public Drawable friendSculpture;
    public String status;
    public Friend(){}
    public Friend(String friendName,Drawable friendSculpture,String status)
    {
        this.friendName = friendName;
        this.friendSculpture = friendSculpture;
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public Drawable getFriendSculpture() {
        return friendSculpture;
    }

    public void setFriendSculpture(Drawable friendSculpture) {
        this.friendSculpture = friendSculpture;
    }

    @Override
    public int getItemType() {
        return FriendAdapter.TYPE_LEVEL_1;
    }
}
