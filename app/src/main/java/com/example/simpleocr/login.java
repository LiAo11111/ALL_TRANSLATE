package com.example.simpleocr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.IOException;

public class login extends AppCompatActivity {

    private EditText editTextphone;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private Button BACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_login);

        // 初始化视图
        editTextphone = findViewById(R.id.phone_input);
        editTextPassword = findViewById(R.id.signup_password);
        editTextConfirmPassword = findViewById(R.id.rp_signup_password);
        buttonRegister = findViewById(R.id.try_signup_button);
        BACK=findViewById(R.id.go_back_button);

        // 设置注册按钮的点击事件监听器
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的用户名和密码
                String username = editTextphone.getText().toString();
                String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();

                // 检查用户名和密码是否为空
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(login.this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    // 检查两次输入的密码是否一致
                    Toast.makeText(login.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    // 注册成功
                    registerSuccess(username, password);

                }
            }
        });
        BACK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个Intent对象，用于启动登录活动
                Intent intent = new Intent(login.this, register.class);
                // 启动登录活动
                startActivity(intent);
                // 关闭当前的注册活动
                finish();
            }
        });
    }

    // 注册成功后调用保存用户名和密码到文件的方法
    private void registerSuccess(String username, String password) {
        // 保存用户名和密码到文件
        saveCredentialsToFile(username, password);
        // 这里可以添加其他操作，比如跳转到登录界面
        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(login.this, register.class);
        // 启动登录活动
        startActivity(intent);
        // 关闭当前的注册活动
        finish();
    }

    // 保存用户名和密码到文件
    private void saveCredentialsToFile(String username, String password) {
        String filename = "credentials.txt";
        String content = username + "," + password;

        try {
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

