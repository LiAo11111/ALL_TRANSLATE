package com.example.simpleocr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class register extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化视图
        editTextUsername = findViewById(R.id.Login_Phone);
        editTextPassword = findViewById(R.id.Login_key);
        buttonRegister = findViewById(R.id.enter);
        buttonLogin = findViewById(R.id.log_in);

        // 设置登录按钮的点击事件监听器
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的用户名和密码
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // 验证用户名和密码
                if (validateCredentials(username, password)) {
                    // 登录成功
                    Toast.makeText(register.this, "登录成功", Toast.LENGTH_SHORT).show();
                    // 在这里可以跳转到其他界面
                    Intent intent = new Intent(register.this, MainActivity.class);
                    // 启动MainActivity
                    startActivity(intent);
                    // 关闭当前的登录界面
                    finish();
                } else {
                    // 登录失败
                    Toast.makeText(register.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置注册按钮的点击事件监听器
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个Intent对象，用于启动注册活动
                Intent intent = new Intent(register.this, login.class);
                // 启动注册活动
                startActivity(intent);
            }
        });
    }

    // 验证用户名和密码是否正确
    private boolean validateCredentials(String username, String password) {
        try {
            FileInputStream fis = openFileInput("credentials.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
