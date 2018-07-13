package com.jin.chat1;

import android.graphics.drawable.Drawable;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

public class Friend_by_group extends AbstractExpandableItem<Friend> implements MultiItemEntity {
    public String friendGroup;

    public Friend_by_group(String friendGroup){
        this.friendGroup = friendGroup;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return FriendAdapter.TYPE_LEVEL_0;
    }
}
