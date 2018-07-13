package com.jin.chat1;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IUIMsgListener{
    private final static int DISCONNECET = 1000;
    private final static int MSGWHAT_MSGRECV = 0;
    private List<Msg> msgList = new ArrayList<>();
    private EditText et_sendmsg;
  //  private EditText et_sendto;
   // private EditText et_revcmsg;
    private String account;
    private LinearLayoutManager layoutManager;
    private MsgAdapter adapter;
    private TextView tv_fname;
    private String username;

    //数据库
    private MydatabaseHelper dbHelper;

    private RecyclerView msgRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        init();

    }
    private void init() {
       // msgList.add(new Msg("你好", Msg.TYPE.RECEIVED));
       // msgList.add(new Msg("你好，请问你是？", Msg.TYPE.SENT));
       // msgList.add(new Msg("我是 deniro，很高兴认识你^_^", Msg.TYPE.RECEIVED));
       // TextView tv_welcome = (TextView)this.findViewById(R.id.main_tv_welcome);
        et_sendmsg = (EditText)this.findViewById(R.id.input);
        msgRecyclerView=(RecyclerView)findViewById(R.id.msg);
        layoutManager=new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter=new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        tv_fname = findViewById(R.id.tv_fname);
     //   et_sendto = (EditText)this.findViewById(R.id.main_et_sendto);
      //  et_revcmsg = (EditText)this.findViewById(R.id.main_et_recvmsg);
     //   et_revcmsg.setText("怎么回事");
        Intent intent = getIntent();
         account = intent.getStringExtra("account");
         username = intent.getStringExtra("username");
         tv_fname.setText(account);

     //   tv_welcome.setText(account + "，欢迎！");
        XmppManager.getInstance().SetUIMsgListener(this);

        //数据库初始化
        dbHelper = new MydatabaseHelper(this,"ChatRecord.db",null,1);
        dbHelper.getWritableDatabase();

        //初始化聊天记录
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("record",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                String fr = cursor.getString(cursor.getColumnIndex("fr"));
                String t = cursor.getString(cursor.getColumnIndex("t"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                if(fr.equals(username)&&t.equals(account)) {
                    msgList.add(new Msg(body, Msg.TYPE.SENT));
                }
                if(fr.equals(account)&&t.equals(username)) {
                    msgList.add(new Msg(body, Msg.TYPE.RECEIVED));
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        int newSize = msgList.size() -1;

        adapter.notifyItemInserted(newSize);
        msgRecyclerView.scrollToPosition(newSize);
    }

    public void onClick(View view) {
        //Toast.makeText(this, "点击", Toast.LENGTH_LONG).show();
        // TODO Auto-generated method stub
        switch(view.getId())
        {
            case R.id.send: {
                String content=et_sendmsg.getText().toString();
                if("".equals(content))
                    return;

                msgList.add(new Msg(content, Msg.TYPE.SENT));

                //如果有新消息，则设置适配器的长度（通知适配器，有新的数据被插入），并让 RecyclerView 定位到最后一行
                int newSize = msgList.size() -1;

                adapter.notifyItemInserted(newSize);
                msgRecyclerView.scrollToPosition(newSize);

                //清空输入框中的内容
                et_sendmsg.setText("");

                String sendto = account.trim();

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("fr",username);
                values.put("t",account);
                values.put("body",content);
                db.insert("record",null,values);
                values.clear();
                XmppManager.getInstance().SendChatMessage(sendto, content);

                break;
            }
            case R.id.bt_back:{
                Intent intent = new Intent(MainActivity.this, List_friend_Activity.class);
                intent.putExtra("account", username);
                startActivity(intent);

                break;
            }
        }
    }
    public void onMessageReceived(String sender, Object data) {
        // TODO Auto-generated method stub
        //Log.e("MainActivity" , sender + ": " + data.toString() );
        //需要数据传递，用下面方法；
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fr",account);
        values.put("t",username);
        values.put("body",data.toString());
        db.insert("record",null,values);
        values.clear();
        Message msg =new Message();
        msg.obj = data;//可以是基本类型，可以是对象，可以是List、map等；
        msg.what = MSGWHAT_MSGRECV;
        String send="";
        for(int i=0;i<sender.length();i++)
        {
            if(sender.charAt(i)=='@')
                break;
            else
                send = send + String.valueOf(sender.charAt(i));
        }
        if(send.equals(account)) {
            String content = data.toString();
            msgList.add(new Msg(content, Msg.TYPE.RECEIVED));


            //Log.i("Tag",data.toString());
            //int newSize = msgList.size() -1;
            //Log.i("Tag",String.valueOf(newSize));
            // adapter.notifyItemInserted(newSize);
            //adapter.notifyItemChanged(newSize2);
            // msgRecyclerView.scrollToPosition(newSize);
            _mHandler.sendMessage(msg);
        }

    }

   Handler _mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int newSize = msgList.size() -1;
            //Log.i("Tag",String.valueOf(newSize));
            adapter.notifyItemInserted(newSize);
            //adapter.notifyItemChanged(newSize2);
            msgRecyclerView.scrollToPosition(newSize);
        }
    };
}
