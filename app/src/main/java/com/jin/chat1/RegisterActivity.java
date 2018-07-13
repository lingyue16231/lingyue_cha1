package com.jin.chat1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private EditText etAccount, etPwd,etConPwd;//输入账户与密码框
    private Button btnRegi;//注册按钮
    private LinearLayout llREGStatus;//登录进度条
    private final static int SUCCESSED_REG = 1;  //登录成功
    private final static int FAILED_REG = 0;   //登录失败
    private int regCode;
   private String account;
   private String pwd;
    private String Conpwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        init();
    }

    private void init() {
        context = this;
        etAccount = (EditText) findViewById(R.id.username);
        etPwd = (EditText) findViewById(R.id.password);
        etConPwd= (EditText) findViewById(R.id.confirm_password);
        llREGStatus = (LinearLayout) findViewById(R.id.reg_activity_ll_reg_show);
        btnRegi = (Button) findViewById(R.id.reg_activity_btn_register);
        btnRegi.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reg_activity_btn_register: {
                 account = etAccount.getText().toString();
                 pwd = etPwd.getText().toString(); //账号判空
                Conpwd = etConPwd.getText().toString();

                if (account == null || account.length() <= 0) {
                    Toast.makeText(context, "账号不能为空！", Toast.LENGTH_LONG).show();
                    return ;
                }
                if (pwd == null || pwd.length() <= 0) { //密码判空
                    Toast.makeText(context, "密码不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                if ((Conpwd == null || pwd.length() <= 0)||Conpwd.equals(pwd)==false) { //密码判空
                    Toast.makeText(context, "请重新确认密码！", Toast.LENGTH_LONG).show();
                    etConPwd.setText("");
                    return;
                }
                new AsyncTask<String, Void, Integer>() {   //前台显示

                    protected void onPreExecute() {

                        llREGStatus.setVisibility(View.VISIBLE);
                    }

                    //用来处理后台耗时的线程
                    protected Integer doInBackground(String... strings) {
                         regCode = FAILED_REG;
                        //注册

                        boolean bool = XmppManager.getInstance().Register(strings[0], strings[1]);
                        if (bool) {
                            regCode = SUCCESSED_REG;
                        }
                        return regCode;
                    }

                    //后台处理的结果
                    protected void onPostExecute(Integer result) {
                        llREGStatus.setVisibility(View.GONE);

                        switch (result) {
                            case SUCCESSED_REG:
                                Toast.makeText(context, "注册成功!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("account", account);
                                startActivity(intent);
                                break;

                            case FAILED_REG:
                                Toast.makeText(context, "注册失败!", Toast.LENGTH_LONG).show();
                                etAccount.setText("");
                                etConPwd.setText("");
                                 etPwd.setText("");
                                break;
                            default:
                                break;
                        }
                    }
                }.execute(account, pwd);
                break;
            }


        }

    }
}
