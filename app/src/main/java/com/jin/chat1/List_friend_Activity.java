package com.jin.chat1;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

import static com.jin.chat1.XmppManager.HOST;

public class List_friend_Activity extends AppCompatActivity implements View.OnClickListener,IUIMsgListener{

    //数据库
    private MydatabaseHelper dbHelper;
    ArrayList<MultiItemEntity> res;
    List<Friend_by_group> lv0 = new ArrayList<>();
    RecyclerView mrecycleView;
    private String account;
    String alertSubName,response,alertName,acceptAdd;
    private MyReceiver receiver;
    private Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend_);


        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        mrecycleView = findViewById(R.id.rv);
        addData(XmppManager.getInstance().getGroups());
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        Intent intent = getIntent();
        account = intent.getStringExtra("account");
        mrecycleView.setAdapter(new FriendAdapter(List_friend_Activity.this,res,account));
        mrecycleView.setLayoutManager(manager);
        ImageView tx = findViewById(R.id.iv_tx);
        Drawable is = XmppManager.getInstance().getUserImage(account);
        tx.setImageDrawable(is);
        XmppManager.getInstance().SetUIMsgListener(this);

        //数据库初始化
        dbHelper = new MydatabaseHelper(this,"ChatRecord.db",null,1);
        dbHelper.getWritableDatabase();

        //注册广播
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.demo.List_friend_Activity");
        registerReceiver(receiver, intentFilter);
        if (XmppManager.getConnect()!=null){
            //条件过滤器
            PacketFilter filter = new AndFilter(new PacketTypeFilter(Presence.class));
            //packet监听器
            PacketListener listener = new PacketListener() {

                public void processPacket(Packet packet) {
                    System.out.println("PresenceService-"+packet.toXML());
                    if(packet instanceof Presence){
                        Presence presence = (Presence)packet;
                        String from = presence.getFrom();//发送方
                        String to = presence.getTo();//接收方
                        if (presence.getType().equals(Presence.Type.subscribe)) {
                            System.out.println("收到添加请求！");
                            //发送广播传递发送方的JIDfrom及字符串
                            acceptAdd = "收到添加请求！";
                            Intent intent = new Intent();
                            intent.putExtra("fromName", from);
                            System.out.println("第一次"+from);
                            intent.putExtra("acceptAdd", acceptAdd);
                            intent.setAction("com.example.demo.List_friend_Activity");
                            sendBroadcast(intent);
                        } else if (presence.getType().equals(
                                Presence.Type.subscribed)) {
                            //发送广播传递response字符串
                            response = "恭喜，对方同意添加好友！";
                            Intent intent = new Intent();
                            intent.putExtra("response", response);
                            intent.setAction("com.example.demo.List_friend_Activity");
                            sendBroadcast(intent);


                        } else if (presence.getType().equals(
                                Presence.Type.unsubscribe)) {
                            //发送广播传递response字符串
                            response = "抱歉，对方拒绝添加好友，将你从好友列表移除！";
                            Intent intent = new Intent();
                            intent.putExtra("response", response);
                            intent.setAction("com.example.demo.List_friend_Activity");
                            sendBroadcast(intent);
                        }else if (presence.getType().equals(
                                Presence.Type.unavailable)) {
                            _mHandler.sendMessage(new Message());
                        } else {
                            _mHandler.sendMessage(new Message());
                        }
                    }
                }
            };
            //添加监听
            XmppManager.getConnect().addPacketListener(listener, filter);
        }

    }

    private void addData(List<RosterGroup> groups){
        res = new ArrayList<>();
        lv0.clear();
        ArrayList<Friend> friend=new ArrayList<>();
        for(RosterGroup group:groups) {
            lv0.add(new Friend_by_group(group.getName()));
            for (RosterEntry entry : group.getEntries()) {
                Friend f1 = new Friend();
                Drawable is = XmppManager.getInstance().getUserImage(entry.getName());
                if (is != null) {
                    f1.setFriendSculpture(XmppManager.getInstance().getUserImage(entry.getName()));
                } else {
                    f1.setFriendSculpture(getResources().getDrawable(R.drawable.ic_launcher_background));
                }
                int a= XmppManager.getInstance().isonline(entry.getName()+"@"+XmppManager.HOST);
                String str;
                if(a==1)
                    str=" (在线)";
                else
                    str=" (离线)";
                f1.setFriendName(entry.getName());
                f1.setStatus(str);
                lv0.get(0).addSubItem(f1);
            }
        }
        for (int j = 0; j < lv0.size(); j++) {
            res.add(lv0.get(j));
        }
        return ;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.bt_add: {
                Log.d("Tag","1111");
                final EditText edt = new EditText(this);
                new AlertDialog.Builder(this)
                        .setTitle("请输入用户名")
                        .setView(edt)
                        .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                XmppManager.getInstance().addUser(edt.getText().toString());
                                Log.d("TAG",edt.getText().toString()+"@"+HOST);
                            }
                        })
                        .setNegativeButton("取消", null).show();
                break;
            }
        case R.id.bt_cc:{
                XmppManager.DisConnect();
                Intent intent = new Intent(List_friend_Activity.this, LoginActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onMessageReceived(String sender, Object data) {
        String send="";
        for(int i=0;i<sender.length();i++)
        {
            if(sender.charAt(i)=='@')
                break;
            else
                send = send + String.valueOf(sender.charAt(i));
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fr",send);
        values.put("t",account);
        values.put("body",data.toString());
        db.insert("record",null,values);
        values.clear();
        _mHandler.sendMessage(new Message());
    }
    //广播接收器
    public class MyReceiver extends BroadcastReceiver {

        public void onReceive(final Context context, Intent intent) {
            //接收传递的字符串response
            Bundle bundle = intent.getExtras();
            response = bundle.getString("response");
            System.out.println("广播收到"+response);
            if(response==null){
                //获取传递的字符串及发送方JID
                acceptAdd = bundle.getString("acceptAdd");
                alertName = bundle.getString("fromName");
                System.out.println("第二次"+alertName);
                if(alertName!=null){
                    //裁剪JID得到对方用户名
                    alertSubName = alertName.substring(0,alertName.indexOf("@"));
                }
                if(acceptAdd.equals("收到添加请求！")){
                    //弹出一个对话框，包含同意和拒绝按钮
                    AlertDialog.Builder builder  = new AlertDialog.Builder(List_friend_Activity.this);
                    builder.setTitle("添加好友请求" ) ;
                    builder.setMessage("用户"+alertSubName+"请求添加你为好友" ) ;
                    System.out.println("第三次"+alertSubName);
                    builder.setPositiveButton("同意",new DialogInterface.OnClickListener() {
                        //同意按钮监听事件，发送同意Presence包及添加对方为好友的申请
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            Presence presenceRes = new Presence(Presence.Type.subscribed);
                            presenceRes.setTo(alertName);
                            XmppManager.getConnect().sendPacket(presenceRes);
                            XmppManager.getInstance().addUser(alertSubName);
                            addData(XmppManager.getInstance().getGroups());
                            RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
                            mrecycleView.setAdapter(new FriendAdapter(List_friend_Activity.this,res,account));
                            mrecycleView.setLayoutManager(manager);
                        }
                    });
                    builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        //拒绝按钮监听事件，发送拒绝Presence包
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            Presence presenceRes = new Presence(Presence.Type.unsubscribe);
                            presenceRes.setTo(alertName);
                            XmppManager.getConnect().sendPacket(presenceRes);
                        }
                    });
                    builder.show();
                }
            }
        }

    }

    Handler _mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            addData(XmppManager.getInstance().getGroups());
            RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
            mrecycleView.setAdapter(new FriendAdapter(List_friend_Activity.this,res,account));
            mrecycleView.setLayoutManager(manager);
        }
    };

}
