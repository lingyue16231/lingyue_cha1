package com.jin.chat1;


import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;
import java.util.logging.LogRecord;

import static com.jin.chat1.XmppManager.HOST;

public class FriendAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    private Context context;
    private String username;
    private List<MultiItemEntity> list;
    private MydatabaseHelper dbHelper;
    public FriendAdapter(Context context,List<MultiItemEntity> data,String username) {
        super(data);
        this.context = context;
        this.list = data;
        addItemType(TYPE_LEVEL_0, R.layout.friend_group_item);
        addItemType(TYPE_LEVEL_1, R.layout.friend_item);
        this.username = username;
        dbHelper = new MydatabaseHelper(this.context,"ChatRecord.db",null,1);
        dbHelper.getWritableDatabase();
    }

    private String search(String friendName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String str=null;
        Cursor cursor = db.query("record",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                String fr = cursor.getString(cursor.getColumnIndex("fr"));
                String t = cursor.getString(cursor.getColumnIndex("t"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                if(fr.equals(friendName)||t.equals(friendName)) {
                    str=body;

                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        return str;
    }


    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {
        final Resources resources = mContext.getResources();
        switch (helper.getItemViewType()) {
            case 0:
                final Friend_by_group lv0 = (Friend_by_group) item;
                helper.setText(R.id.tv_friend_gourp, lv0.friendGroup);
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = helper.getAdapterPosition();
                        if (lv0.isExpanded()) {
                            helper.setImageDrawable(R.id.iv_group,resources.getDrawable(R.mipmap.list_down));
                            collapse(pos);
                        }
                        else {
                            helper.setImageDrawable(R.id.iv_group,resources.getDrawable(R.mipmap.list_right));
                            expand(pos);
                        }
                    }
                });
                break;
            case 1:
                final Friend lv1 = (Friend) item;
                helper.setImageDrawable(R.id.iv_head, lv1.friendSculpture);
                String str=search(lv1.friendName);
                helper.setText(R.id.tv_friend_title, lv1.friendName+lv1.status);
                if (str!=null)
                    helper.setText(R.id.tv_friend_title, lv1.friendName+lv1.status+"  "+str);
                helper.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,MainActivity.class);
                        intent.putExtra("account", lv1.getFriendName());
                        intent.putExtra("username",username);
                        Log.d("Tag",username);
                        context.startActivity(intent);
                    }
                });
                helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(context)
                                .setTitle("删除好友")
                                .setMessage("确定删除好友"+lv1.getFriendName())
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        XmppManager.getInstance().removerUser(lv1.getFriendName()+"@"+HOST);
                                        Log.d("TAG","删除成功");
                                        list.remove(lv1);
                                        notifyDataSetChanged();

                                    }
                                })
                                .setNegativeButton("取消", null).show();
                        return true;
                    }
                });
                break;
        }
    }
}
