package com.jin.chat1;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
public class ChatActivity extends AppCompatActivity implements OnClickListener{
        private EditText et_sendmsg;
        private EditText et_sendto;
        private EditText et_revcmsg;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);

            TextView tv_sendto = (TextView)this.findViewById(R.id.chat_tv_sendto);
            et_sendmsg = (EditText)this.findViewById(R.id.chat_et_sendmsg);
            et_revcmsg = (EditText)this.findViewById(R.id.chat_et_recvmsg);
            et_revcmsg.setText("怎么回事");

            Intent intent = getIntent();
            String account = intent.getStringExtra("account");
            tv_sendto.setText(account + "，正在和您通信");
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(this, "点击", Toast.LENGTH_LONG).show();
            // TODO Auto-generated method stub
            switch(view.getId())
            {
                case R.id.chat_btn_send:
                    // 在这里要判断空咯
                    if(et_sendmsg.getText().toString() == "" ||
                            et_sendto.getText().toString() =="" )
                    {
                        Toast.makeText(this, "对端账号或发送内容都不能为空哦", Toast.LENGTH_LONG);
                        return;
                    }
                    String msg = et_sendmsg.getText().toString();
                    String sendto = et_sendto.getText().toString().trim();

                    XmppManager.getInstance().SendChatMessage(sendto, msg);
                    break;
            }
        }
    }

