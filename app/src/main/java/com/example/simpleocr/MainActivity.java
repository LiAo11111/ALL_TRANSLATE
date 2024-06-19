package com.example.simpleocr;
import static com.example.simpleocr.FileUtils.copyFile;
import static com.example.simpleocr.FileUtils.deleteLangFile;
import static com.example.simpleocr.FileUtils.deleteRecursive;
import static com.example.simpleocr.FileUtils.deleteSingleFile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.simpleocr.Adapters.OcrListAdapter;
import com.example.simpleocr.DataBase.Room;
import com.example.simpleocr.Model.ItemClick;
import com.example.simpleocr.Model.OcrItem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * @author 30415
 */
@ExperimentalGetImage
public class MainActivity extends AppCompatActivity {
    // 声明变量
    AppBarLayout appBarLayout;
    MaterialToolbar materialToolbar;
    RecyclerView recyclerView;
    FloatingActionButton button, openCamera, openAlbum, write;
    SwipeRefreshLayout refresh;
    Room room;
    OcrListAdapter ocrListAdapter;
    private List<OcrItem> itemList = new ArrayList<>();
    private int mPosition;
    ActivityResultLauncher<Intent> intentActivityResultLauncher1, intentActivityResultLauncher2;
    File parentFile;
    // 第二个OCR引擎支持的5种语言，阿拉伯语+波斯语+俄语+泰语+乌尔都语
    private static final String LANG = "ara+fas+rus+tha+urd";
    private int engineNum = 0;

    // 初始化界面
    private void initUi() {
        // 绑定控件
        appBarLayout = findViewById(R.id.appBar);
        appBarLayout.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        materialToolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(materialToolbar);
        materialToolbar.setTitleCentered(true);
        materialToolbar.setNavigationIcon(R.drawable.baseline_menu_24);

        recyclerView = findViewById(R.id.recycler_view);
        button = findViewById(R.id.fab_add_btn);
        openCamera = findViewById(R.id.camera_btn);
        openAlbum = findViewById(R.id.album_btn);
        refresh = findViewById(R.id.refresh);
        write = findViewById(R.id.write_btn);

        // 设置按钮点击事件
        openCamera.setOnClickListener(v -> {
            // 检查相机权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 请求相机权限
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                // 启动OCR活动，并传递相机启动参数
                Intent intent = new Intent(this, OcrActivity.class);
                intent.putExtra("launch", "camera");
                intent.putExtra("langs", LANG);
                intent.putExtra("engine", engineNum);
                // 添加一个整数参数,值为1时是中文翻译成其他语言，0时是其他语言翻译成中文
                intent.putExtra("translate_option", 0);
                intentActivityResultLauncher1.launch(intent);
            }
        });

        write.setOnClickListener(v -> {
            // 启动OCR活动，并传递手写识别启动参数
            Intent intent = new Intent(this, OcrActivity.class);
            intent.putExtra("launch", "write");
            intent.putExtra("langs", LANG);
            intent.putExtra("engine", engineNum);
            intent.putExtra("translate_option", 1); // 中文翻译成其他语言
            intentActivityResultLauncher1.launch(intent);
        });

        openAlbum.setOnClickListener(v -> {
            // 检查读取相册权限
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 请求读取相册权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                // 启动OCR活动，并传递相册启动参数
                Intent intent = new Intent(this, OcrActivity.class);
                intent.putExtra("launch", "album");
                intent.putExtra("langs", LANG);
                intent.putExtra("engine", engineNum);
                intent.putExtra("translate_option", 0); // 其他语言翻译成中文
                intentActivityResultLauncher1.launch(intent);
            }
        });

        // 悬浮按钮点击事件
        button.setOnClickListener(view -> {
            if (openCamera.getVisibility() == View.GONE) {
                // 如果相机按钮不可见，则显示操作选项
                showOptions();
            } else {
                // 否则隐藏操作选项
                hideOptions();
            }
        });

        // 下拉刷新事件监听
        refresh.setOnRefreshListener(this::onRefresh);
        refresh.setProgressViewEndTarget(true, (int) (getResources().getDisplayMetrics().density * 100));
        refresh.setDistanceToTriggerSync((int) (getResources().getDisplayMetrics().density * 200));

        // RecyclerView滚动事件监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (openCamera.getVisibility() != View.GONE) {
                    // 如果相机按钮可见，则隐藏操作选项
                    hideOptions();
                }
            }
        });
    }
    private void showOptions() {
        // 显示悬浮按钮
        openCamera.setVisibility(View.VISIBLE);
        openAlbum.setVisibility(View.VISIBLE);
        write.setVisibility(View.VISIBLE);
        // 动画效果，向上移动按钮
        animateViewTranslationY(openCamera, -getResources().getDisplayMetrics().density * 150);
        animateViewTranslationY(openAlbum, -getResources().getDisplayMetrics().density * 75);
        animateViewTranslationX(write, -getResources().getDisplayMetrics().density * 75);
        // 更改主按钮图标
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_clear_24));
    }

    private void hideOptions() {
        // 动画效果，还原按钮位置
        animateViewTranslationY(openCamera, 0f);
        animateViewTranslationY(openAlbum, 0f);
        animateViewTranslationX(write, 0f);
        // 延迟隐藏按钮
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            openCamera.setVisibility(View.GONE);
            openAlbum.setVisibility(View.GONE);
            write.setVisibility(View.GONE);
        }, 200);
        // 更改主按钮图标
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_add_24));
    }

    private void animateViewTranslationY(View view, float translationY) {
        // 垂直动画效果
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", translationY);
        animator.setDuration(200);
        animator.start();
    }

    private void animateViewTranslationX(View view, float translationX) {
        // 水平动画效果
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", translationX);
        animator.setDuration(200);
        animator.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);

        FileUtils.checkAndRequestPermissions(this);

        initUi(); // 初始化界面

        itemTouchHelper.attachToRecyclerView(recyclerView); // 设置RecyclerView的拖拽操作
        room = Room.getInstance(this);
        itemList = room.dao().getAll();
        updateRecycler(itemList); // 更新RecyclerView数据

        // 注册Activity结果监听器
        intentActivityResultLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                // 处理OCR活动返回的结果
                OcrItem newItem = null;
                if (res.getData() != null) {
                    newItem = (OcrItem) res.getData().getSerializableExtra("ocr_item");
                }
                long id = room.dao().insert(newItem);
                if (newItem != null) {
                    newItem.setId(id);
                }
                itemList.add(0, newItem);
                ocrListAdapter.notifyItemInserted(0);
            }
        });

        // 注册Activity结果监听器
        intentActivityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                // 处理OCR活动返回的结果
                OcrItem newItem = null;
                if (res.getData() != null) {
                    newItem = (OcrItem) res.getData().getSerializableExtra("ocr_item");
                }
                if (newItem != null) {
                    room.dao().update(newItem);
                }
                itemList.clear();
                itemList.addAll(room.dao().getAll());
                ocrListAdapter.notifyItemChanged(mPosition);
            }
        });

        // 设置Fragment结果监听器
        getSupportFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            if ("requestKey".equals(requestKey)) {
                if (bundle.getBoolean("clear", false)) {
                    // 处理清除数据操作
                    SpinKitView process = findViewById(R.id.spin_kit);
                    process.setVisibility(View.VISIBLE);
                    room.clearAllTables();
                    File dir = getFilesDir();
                    deleteRecursive(dir);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        itemList = room.dao().getAll();
                        updateRecycler(itemList);
                        process.setVisibility(View.GONE);
                    }, 2000);
                }
            }
        });

        String mDataPath = getFilesDir().getAbsolutePath();

        parentFile = new File(mDataPath, "tessdata");
        if (!parentFile.exists()) { // 确保路径存在
            parentFile.mkdir();
        }
        copyFiles(); // 复制字库到手机

        //String[] deleteFilePaths = new String[]{"ara.traineddata","tha.traineddata"};
        String[] deleteFilePaths = new String[]{"ara.traineddata","fas.traineddata","rus.traineddata","tha.traineddata","urd.traineddata"};
        try {
            for (String path : deleteFilePaths) {
                deleteLangFile(path, parentFile);
            }
        } catch (Exception ignored) {
        }
    }

    private void copyFiles() {
        // 从Assets文件夹复制字库到手机
        AssetManager am = getAssets();
        String[] dataFilePaths = new String[]{"ara.traineddata","fas.traineddata","rus.traineddata","tha.traineddata","urd.traineddata"};
        for (String dataFilePath : dataFilePaths) {
            File engFile = new File(parentFile, dataFilePath);
            if (!engFile.exists()) {
                copyFile(am, dataFilePath, engFile);
            }
        }
    }


    public void onRefresh() {//刷新
        // 模拟刷新操作
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            itemList.clear();
            itemList.addAll(room.dao().getAll());
            updateRecycler(itemList);
            refresh.setRefreshing(false);//刷新旋转动画停止
        }, 1000);
    }

    private void updateRecycler(List<OcrItem> ocrItemList) {
        // 更新RecyclerView数据
        recyclerView.setHasFixedSize(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        ocrListAdapter = new OcrListAdapter(ocrItemList, itemClick);
        recyclerView.setAdapter(ocrListAdapter);
    }

    final ItemClick itemClick = new ItemClick() {
        @Override
        public void onClick(OcrItem ocrItem, int position, View imageview) {
            // 处理RecyclerView项点击事件
            mPosition = position;
            Intent intent = new Intent(MainActivity.this, OcrActivity.class);
            intent.putExtra("old_ocr", ocrItem);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imageview, "testImg");
            intentActivityResultLauncher2.launch(intent, optionsCompat);
        }
    };

    final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // 设置支持的拖拽和滑动方向
            int swiped = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            //第一个参数拖动，第二个删除侧滑
            return makeMovementFlags(0, swiped);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // 处理滑动删除事件
            int position = viewHolder.getAdapterPosition();
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(getString(R.string.delete))//标题
                    .setMessage(getString(R.string.sure))//内容
                    .setIcon(R.mipmap.ic_launcher)//图标
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> ocrListAdapter.notifyItemChanged(position))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        room.dao().delete(itemList.get(position));
                        String path = itemList.get(position).getImage();
                        deleteSingleFile(path);
                        itemList.remove(position);
                        ocrListAdapter.notifyItemRemoved(position);
                    }).show();
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 创建菜单项
        getMenuInflater().inflate(R.menu.info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 菜单项点击事件处理
        if (item.getItemId() == android.R.id.home) {
            // 创建Material3 Bottom Sheet
            MyBottomSheetDialog bottomSheetDialog = new MyBottomSheetDialog();
            // 显示Bottom Sheet
            bottomSheetDialog.show(getSupportFragmentManager(), "bottom_sheet_tag");
            return true;
        } else if (item.getItemId() == R.id.choose) {
            // 创建一个有两个选项的框
            final String[] items = {
                    getString(R.string.engineOptions1),
                    getString(R.string.engineOptions2),
                    getString(R.string.engineOptions3),
                    getString(R.string.engineOptions4),
                    getString(R.string.engineOptions5),
                    getString(R.string.engineOptions6),
                    getString(R.string.engineOptions7),
                    getString(R.string.engineOptions8),
                    getString(R.string.engineOptions9),
                    getString(R.string.engineOptions10),
                    getString(R.string.engineOptions11),
                    getString(R.string.engineOptions12),
                    getString(R.string.engineOptions13),
                    getString(R.string.engineOptions14),
                    getString(R.string.engineOptions15),
                    getString(R.string.engineOptions16),
                    getString(R.string.engineOptions17),
                    getString(R.string.engineOptions18),
                    getString(R.string.engineOptions19),
                    getString(R.string.engineOptions20),
                    getString(R.string.engineOptions21),
                    getString(R.string.engineOptions22),
                    getString(R.string.engineOptions23),
                    getString(R.string.engineOptions24),
                    getString(R.string.engineOptions25),
                    getString(R.string.engineOptions26),
                    getString(R.string.engineOptions27),

            };
            // 保存原始引擎选项
            final int originalEngineNum = engineNum;
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.engine))
                    .setIcon(R.drawable.baseline_settings_24)
                    .setCancelable(false)
                    //0 google 1 tess
                    .setSingleChoiceItems(items, engineNum, (dialog1, which) -> {
                        if ( which == 10 || which == 14 || which == 17  || which == 21 || which == 24 || which == 25 || which == 26) { // Replace SPECIAL_VALUE with the specific value that triggers the warning
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle("警告")
                                    .setMessage("该语种仅支持识别，暂不支持翻译")
                                    .setPositiveButton("好的", null)
                                    .show();
                        }
                        engineNum = which;
                    })
                    .setPositiveButton(getString(R.string.confirm), null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024) { // 检查请求码是否为1024
            // 检查每个权限的授予结果
            for (int i = 0; i < permissions.length; i++) {
                // 如果有权限未被授予，显示请求权限的提示消息
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.ask), Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }
}