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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private EditText etAccount, etPwd;//输入账户与密码框
    private Button btnLogin,btnRegi;//登录注册按钮
    private LinearLayout llLoginStatus;//登录进度条
    private final static int SUCCESSED_LOGIN = 1;  //登录成功
    private final static int FAILED_LOGIN = 0;   //登录失败
private String pwd,account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        init();
    }

    private void init() {
        context = this;
        etAccount = (EditText) findViewById(R.id.login_activity_et_account);
        etPwd = (EditText) findViewById(R.id.login_activity_et_password);
        llLoginStatus = (LinearLayout) findViewById(R.id.login_activity_ll_login_show);
        btnLogin = (Button) findViewById(R.id.login_activity_btn_login);
        btnRegi = (Button) findViewById(R.id.login_activity_btn_register);
        btnLogin.setOnClickListener(this);
        btnRegi.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_activity_btn_login:
                 XmppManager.DisConnect();
                 account = etAccount.getText().toString();
                 pwd = etPwd.getText().toString(); //账号判空
                if (account == null || account.length() <= 0) {
                    Toast.makeText(context, "账号不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (pwd == null || pwd.length() <= 0) { //密码判空
                    Toast.makeText(context, "密码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                new AsyncTask<String, Void, Integer>() {   //前台显示

                    protected void onPreExecute() {
                        System.out.println("login onPreExecute()");
                        llLoginStatus.setVisibility(View.VISIBLE);
                    }

                    //用来处理后台耗时的线程
                    protected Integer doInBackground(String... strings) {
                        System.out.println("login doInBackground()");
                        //登录
                        boolean bool = XmppManager.getInstance().Login(strings[0], strings[1]);
                        Log.e("Login sufa",bool+"");
                        if (bool) {

                            return SUCCESSED_LOGIN;
                        }
                        return FAILED_LOGIN;
                    }

                    //后台处理的结果
                    protected void onPostExecute(Integer result) {
                        llLoginStatus.setVisibility(View.GONE);

                        switch (result) {
                            case SUCCESSED_LOGIN:
                                System.out.println("登录成功");
                                Intent intent = new Intent(LoginActivity.this, List_friend_Activity.class);
                                intent.putExtra("account", account);
                                startActivity(intent);
                                break;

                            case FAILED_LOGIN:
                                Toast.makeText(context, "登录失败", Toast.LENGTH_LONG).show();
                                break;
                            default:
                                break;
                        }
                    }
                }.execute(account, pwd);

                break;
                case  R.id.login_activity_btn_register:
                {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
                break;
        }

    }

}


