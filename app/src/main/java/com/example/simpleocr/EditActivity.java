package com.example.simpleocr;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/**
 * 编辑文本的活动。
 * 提供一个文本输入框并处理返回按钮和菜单操作。
 * 使用 Material Design 组件进行动态颜色主题设置。
 */
public class EditActivity extends AppCompatActivity {

    // 文本输入框，用于编辑文本
    TextInputEditText textInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 如果可用，应用动态颜色
        DynamicColors.applyToActivityIfAvailable(this);

        // 设置状态栏颜色
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));

        // 禁用返回按钮的默认行为
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        // 设置活动的布局
        setContentView(R.layout.activity_edit);

        // 创建并添加返回按钮回调
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 此处可以添加返回按钮被按下时的处理逻辑
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // 获取布局中的文本输入框
        textInputEditText = findViewById(R.id.editText);

        // 如果Intent中包含"text"数据，则设置文本输入框的内容
        if (null != getIntent().getStringExtra("text")) {
            textInputEditText.setText(getIntent().getStringExtra("text"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单布局
        getMenuInflater().inflate(R.menu.done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理菜单项被选中的情况
        if (item.getItemId() == R.id.done) {
            // 获取文本输入框的内容
            CharSequence text = textInputEditText.getText();

            // 创建Intent并将文本内容放入其中
            Intent intent = new Intent();
            intent.putExtra("text", text);

            // 设置结果并结束活动
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
