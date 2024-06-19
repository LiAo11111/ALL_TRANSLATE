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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class changepassword extends AppCompatActivity {

    private EditText editTextPhone;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonChangePassword;
    private Button BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        // 初始化视图
        editTextPhone = findViewById(R.id.phone_input);
        editTextPassword = findViewById(R.id.signup_password);
        editTextConfirmPassword = findViewById(R.id.rp_signup_password);
        buttonChangePassword = findViewById(R.id.try_signup_button);
        BACK = findViewById(R.id.go_back_button);

        // 设置修改密码按钮的点击事件监听器
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = editTextPhone.getText().toString();
                String newPassword = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();

                if (phoneNumber.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(changepassword.this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidPhoneNumber(phoneNumber)) {
                    Toast.makeText(changepassword.this, "无效的手机号码", Toast.LENGTH_SHORT).show();
                } else if (!isStrongPassword(newPassword)) {
                    Toast.makeText(changepassword.this, "密码强度不够，必须包含至少6个字符，包括数字、字母和特殊字符", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(changepassword.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else if (!isPhoneNumberMatch(phoneNumber)) {
                    Toast.makeText(changepassword.this, "手机号不匹配", Toast.LENGTH_SHORT).show();
                } else {
                    changePasswordSuccess(phoneNumber, newPassword);
                }
            }
        });

        BACK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(changepassword.this, register.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // 检查手机号码格式
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phonePattern = "^\\d{11}$"; // 假设手机号码是10位数字
        return Pattern.matches(phonePattern, phoneNumber);
    }

    // 检查密码强度
    private boolean isStrongPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#\\$%^&*]).{6,}$";
        return Pattern.matches(passwordPattern, password);
    }

    // 检查手机号是否匹配
    private boolean isPhoneNumberMatch(String phoneNumber) {
        String[] credentials = readCredentialsFromFile();
        if (credentials != null && credentials.length > 0) {
            return phoneNumber.equals(credentials[0]);
        }
        return false;
    }

    // 修改密码成功后调用保存新密码到文件的方法
    private void changePasswordSuccess(String phoneNumber, String newPassword) {
        saveCredentialsToFile(phoneNumber, newPassword);
        Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(changepassword.this, register.class);
        startActivity(intent);
        finish();
    }

    // 从文件中读取用户名和密码
    private String[] readCredentialsFromFile() {
        String filename = "credentials.txt";
        try {
            FileInputStream fis = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            fis.close();
            return line.split(",");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
