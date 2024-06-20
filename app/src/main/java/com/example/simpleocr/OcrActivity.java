package com.example.simpleocr;

import static com.example.simpleocr.FileUtils.fileSaveToInside;
import static com.example.simpleocr.FileUtils.readPictureDegree;
import static com.example.simpleocr.FileUtils.toTurn;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.app.ActivityOptionsCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.simpleocr.Model.OcrItem;
import com.example.simpleocr.Model.languagetranslation;
import com.example.simpleocr.Model.smalllanguagetranslate;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
//import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
//import com.google.mlkit.vision.text.japanese.japaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 30415
 */
@ExperimentalGetImage
public class OcrActivity extends AppCompatActivity {
    // 进度条
    SpinKitView process;
    // 复制、编辑、分享按钮
    ImageButton copy, edit, share;
    // 显示图片的ImageView和OCR文本的TextView
    ShapeableImageView imageButton;
    MaterialTextView textView;
    // OCR识别使用的位图
    private Bitmap bitmap;
    // 从相册或相机选择图片的Activity结果启动器
    ActivityResultLauncher<Intent> galleryActivityResultLauncher, cameraActivityResultLauncher,
            editActivityResultLauncher, cameraxActivityResultLauncher;
    // 图片来源的URI
    Uri sourceUri;
    // TessBaseAPI对象
    TessBaseAPI mTess;
    // OCR识别的数据项
    OcrItem ocrItem;
    // 是否是旧的OCR数据项
    private boolean oldItem;
    // 图片URI和日期字符串
    String imageUri = "", dateStr = "";
    // 文字识别器对象
    TextRecognizer textRecognizer;
    // 选用的OCR引擎编号
    private int engineNum;
    private boolean changed = false;

    private void translatezhtoothers(String ocrText) {
        //将中文翻译成其他语言
        // 获取当前目标语言
        engineNum = getIntent().getIntExtra("engine", -1);
        String src = "en";
        if (engineNum >= 0 && engineNum <= 26) {
            switch (engineNum) {
                case 0:
                    src = "zh";
                    break;
                case 1:
                    src = "jp";
                    break;
                case 2:
                    src = "kor";
                    break;
                case 3:
                    src = "en";
                    break;
                case 4:
                    src = "pt";
                    break;
                case 5:
                    src = "spa";
                    break;
                case 6:
                    src = "fra";
                    break;
                case 7:
                    src = "de";
                    break;
                case 8:
                    //土耳其语
                    src = "tr";
                    break;
                case 9:
                    src = "it";
                    break;
                case 10:
                    src = "notsupport";
                    break;
                case 11:
                    src = "cs";
                    break;
                case 12:
                    src = "dan";
                    break;
                case 13:
                    src = "nl";
                    break;
                case 14:
                    src = "notsupport";
                    break;
                case 15:
                    src = "fin";
                    break;
                case 16:
                    src = "hu";
                    break;
                case 17:
                    src = "notsupport";
                    break;
                case 18:
                    //印度尼西亚语
                    src = "id";
                    break;
                case 19:
                    //马来西亚语
                    src = "ms";
                    break;
                case 20:
                    src = "vie";
                    break;
                case 21:
                    src = "notsupport";
                    break;
                case 22:
                    src = "rom";
                    break;
                case 23:
                    src = "swe";
                    break;
                case 24:
                    src = "notsupport";
                    break;
                case 25:
                    src = "notsupport";
                    break;
                case 26:
                    src = "notsupport";
                    break;
                default:
                    src = "notsupport";
                    break;
            }
        }
        // 执行翻译操作
        if(src == "notsupport")
        {
            textView.setText(ocrText+"\n\n  翻译结果： \n\n " + "抱歉，当前不支持该语言的翻译功能");
        }
        else {
            //马来西亚语，印度尼西亚语，土耳其语
            if(src=="ms"||src=="id"||src=="tr"){
                //三种语言,土耳其语，马来西亚语、印度尼西亚语
                smalllanguagetranslate.zhToother(ocrText,src, new smalllanguagetranslate.TranslationListener() {
                    @Override
                    public void onTranslationResult(String result) {
                        // 处理翻译结果
                        textView.setText(ocrText+"\n\n  翻译结果： \n\n " + result);
                    }
                });
            }
            else{
                languagetranslation.languageTranslation(ocrText,  "zh",src, new languagetranslation.TranslationListener() {
                    @Override
                    public void onTranslationResult(String result) {
                        // 处理翻译结果
                        textView.setText(ocrText+"\n\n  翻译结果： \n\n " + result);
                    }
                });
            }

        }
    }
    private void translateOtherstozh(String ocrText) {
        //将其他语言翻译成中文
        // 获取当前目标语言
        engineNum = getIntent().getIntExtra("engine", -1);
        String src = "en";
        if (engineNum >= 0 && engineNum <= 26) {
            switch (engineNum) {
                case 0:
                    src = "zh";
                    break;
                case 1:
                    src = "jp";
                    break;
                case 2:
                    src = "kor";
                    break;
                case 3:
                    src = "en";
                    break;
                case 4:
                    src = "pt";
                    break;
                case 5:
                    src = "spa";
                    break;
                case 6:
                    src = "fra";
                    break;
                case 7:
                    src = "de";
                    break;
                case 8:
                    src = "tr";
                    break;
                case 9:
                    src = "it";
                    break;
                case 10:
                    src = "notsupport";
                    break;
                case 11:
                    src = "cs";
                    break;
                case 12:
                    src = "dan";
                    break;
                case 13:
                    src = "nl";
                    break;
                case 14:
                    src = "notsupport";
                    break;
                case 15:
                    src = "fin";
                    break;
                case 16:
                    src = "hu";
                    break;
                case 17:
                    src = "notsupport";
                    break;
                case 18:
                    //印度尼西亚语
                    src = "id";
                    break;
                case 19:
                    //马来西亚语
                    src = "ms";
                    break;
                case 20:
                    src = "vie";
                    break;
                case 21:
                    src = "notsupport";
                    break;
                case 22:
                    src = "rom";
                    break;
                case 23:
                    src = "swe";
                    break;
                case 24:
                    src = "notsupport";
                    break;
                case 25:
                    src = "notsupport";
                    break;
                case 26:
                    src = "notsupport";
                    break;
                default:
                    src = "notsupport";
                    break;
            }
        }
        // 执行翻译操作
        if(src == "notsupport")
        {
            textView.setText(ocrText+"\n\n  翻译结果： \n\n " + "抱歉，当前不支持该语言的翻译功能");
        }
        else {
            if(src=="ms"||src=="id"||src=="tr"){

                //三种语言,土耳其语，马来西亚语、印度尼西亚语
                smalllanguagetranslate.otherToZh(ocrText,src, new smalllanguagetranslate.TranslationListener() {
                    @Override
                    public void onTranslationResult(String result) {
                        // 处理翻译结果
                        textView.setText(ocrText+"\n\n  翻译结果： \n\n " + result);
                    }
                });
            }
            else {
                languagetranslation.languageTranslation(ocrText, src, "zh", new languagetranslation.TranslationListener() {
                    @Override
                    public void onTranslationResult(String result) {
                        // 处理翻译结果
                        textView.setText(ocrText+"\n\n  翻译结果： \n\n " + result);
                    }
                });
            }

        }

    }
    @Override
    public void finish() {
        // 获取TextView中的文本内容
        String text = Objects.requireNonNull(textView.getText()).toString();
        // 如果文本内容不为空或者图片URI不为空
        if (!text.isEmpty() || !imageUri.isEmpty()) {
            // 如果有修改过
            if (changed) {
                // 更新OCR条目的文本和图片URI
                ocrItem.setText(text);
                ocrItem.setImage(imageUri);
                // 设置返回结果
                Intent intent = new Intent();
                intent.putExtra("ocr_item", ocrItem);
                setResult(Activity.RESULT_OK, intent);
            }
        }
        // 如果不是旧条目
        if (!oldItem) {
            // 根据引擎编号关闭对应的文本识别器
            if (engineNum >= 27) {
                // 使用第二个OCR模型
                textRecognizer.close();
            } else if (engineNum >= 0 && engineNum <= 26) {
                // 使用第一个OCR模型
                textRecognizer.close();
            }
        }
        // 调用父类的finish方法
        super.finish();
    }
    private void initUi() {
        // 初始化界面元素
        process = findViewById(R.id.spin_kit);  // 加载进度条
        share = findViewById(R.id.buttonShare);  // 分享按钮
        edit = findViewById(R.id.buttonEdit);  // 编辑按钮
        imageButton = findViewById(R.id.imageButton);  // 图像按钮
        copy = findViewById(R.id.buttonCopy);  // 复制按钮
        textView = findViewById(R.id.textView);  // 文本视图
        // 设置复制按钮的点击事件
        copy.setOnClickListener(view -> {
            if (!Objects.requireNonNull(textView.getText()).toString().isEmpty()) {
                // 复制文本到剪贴板
                ClipData clipData = ClipData.newPlainText("", textView.getText());
                ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clipData);
                // 显示复制成功的提示
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
            }
        });
        // 设置编辑按钮的点击事件
        edit.setOnClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                // 启动编辑活动，并传递文本内容
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("text", textView.getText().toString());
                // 使用转场动画启动活动
                editActivityResultLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, textView, "text"));
            }
        });
        // 设置分享按钮的点击事件
        share.setOnClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                try {
                    // 获取文本内容
                    CharSequence res = textView.getText();
                    // 创建发送意图
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, res.toString());
                    // 启动分享活动
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
                } catch (Exception e) {
                    // 处理异常
                    Toast.makeText(this, "???", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 设置翻译按钮的点击事件
        Button buttontranslate=(Button)findViewById(R.id.translate_button);
        buttontranslate.setOnClickListener(view -> {
            String text = Objects.requireNonNull(textView.getText()).toString();
            int additionalParamValue = getIntent().getIntExtra("translate_option", -1);
            // 如果文本中不包含“翻译结果：”
            if (!text.contains("翻译结果：")) {
                // 根据额外的参数值选择翻译方法
                if (additionalParamValue == 0) {
                    translateOtherstozh(text);  // 其他语言到中文翻译
                } else if (additionalParamValue == 1) {
                    translatezhtoothers(text);  // 中文到其他语言翻译
                } else if (additionalParamValue == -1) {
                    // 显示翻译错误提示
                    textView.setText(text + "\n\n  翻译结果： \n\n " + "翻译的源语言和目标语言错误");
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理菜单项选择事件
        if (item.getItemId() == android.R.id.home) {
            // 如果是返回按钮
            if (changed) {
                // 如果有修改过内容，直接调用finish()方法结束当前活动
                finish();
            } else {
                // 如果没有修改过内容，调用finishAfterTransition()方法结束当前活动（带有转场动画）
                finishAfterTransition();
            }
            return true;  // 处理完毕，返回true
        }
        return super.onOptionsItemSelected(item);  // 其他情况交由父类处理
    }
    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 应用动态颜色（如果可用）
        DynamicColors.applyToActivityIfAvailable(this);
        // 设置状态栏颜色
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        // 设置布局
        setContentView(R.layout.activity_ocr);
        // 显示返回按钮
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // 初始化界面元素
        initUi();
        // 处理返回按键事件
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (changed) {
                    finish();
                } else {
                    finishAfterTransition();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        // 初始化相册启动器
        initGalleryActivityResultLauncher();
        // 初始化相机启动器
        initCameraActivityResultLauncher();
        // 初始化编辑活动启动器
        initEditActivityResultLauncher();
        // 初始化Camerax启动器
        initCameraxActivityResultLauncher();
        // 初始化OCR条目
        ocrItem = new OcrItem();
        // 判断是否是旧的OCR条目
        oldItem = true;
        if (null != getIntent().getExtras() && getIntent().getExtras().containsKey("old_ocr")) {
            // 如果是旧的OCR条目，从Intent中获取OCR条目对象，并设置文本和图片
            ocrItem = (OcrItem) getIntent().getExtras().getSerializable("old_ocr");
            if (ocrItem != null) {
                textView.setText(ocrItem.getText());
                imageUri = ocrItem.getImage();
                dateStr = ocrItem.getDate();
            }
            // 加载图片并显示
            bitmap = BitmapFactory.decodeFile(imageUri);
            imageButton.setVisibility(View.VISIBLE);
            imageButton.setImageBitmap(bitmap);
            // 设置点击图片按钮打开PhotoActivity的功能
            imageButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhotoActivity.class);
                intent.putExtra("uri", imageUri);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
            });
        } else {
            // 如果是新的OCR条目，根据引擎编号初始化文本识别器
            engineNum = getIntent().getIntExtra("engine", -1);
            if (engineNum == 0) {
                textRecognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
            } else if (engineNum == 1) {
                textRecognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
            } else if (engineNum == 2) {
                textRecognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
            } else if (engineNum >= 3 && engineNum <= 23) {
                textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            } else if (engineNum >= 24 && engineNum <= 26) {
                textRecognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
            } else if (engineNum >= 27) {
                // 实际上是没有使用这个模型的，因为性能很差，备用
                String lang = getIntent().getStringExtra("langs");
                mTess = new TessBaseAPI();
                try {
                    mTess.init(getFilesDir().getAbsolutePath(), lang, TessBaseAPI.OEM_LSTM_ONLY);
                } catch (IllegalArgumentException ignored) {
                }
            }
            oldItem = false;
            // 获取当前时间作为OCR条目的时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            dateStr = formatter.format(LocalDateTime.now());
            ocrItem.setDate(dateStr);
            changed = true;
        }
        // 设置ActionBar的标题为OCR条目的时间
        Objects.requireNonNull(getSupportActionBar()).setTitle(dateStr);
        // 根据启动来源进行相应的操作
        if (getIntent().getStringExtra("launch") != null && "camera".equals(getIntent().getStringExtra("launch"))) {
            // 如果是相机启动，打开相机拍照
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (intent.resolveActivity(getPackageManager()) != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, getString(R.string.photo));
                values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.camera));
                sourceUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, sourceUri);
                cameraActivityResultLauncher.launch(intent);
            }
        } else if (getIntent().getStringExtra("launch") != null && "album".equals(getIntent().getStringExtra("launch"))) {
            // 如果是相册启动，打开相册选择图片
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                intent.setType("image/*");
                galleryActivityResultLauncher.launch(intent);
            } else {
                intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                galleryActivityResultLauncher.launch(intent);
            }
        } else if (getIntent().getStringExtra("launch") != null && "write".equals(getIntent().getStringExtra("launch"))) {
            // 如果是编辑启动，打开编辑活动
            Intent intent = new Intent(this, EditActivity.class);
            editActivityResultLauncher.launch(intent);
        }
    }


    private void initGalleryActivityResultLauncher() {
        galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                sourceUri = result.getData().getData();
                startUcrop(sourceUri);
            }
        });
    }

    private void initCameraActivityResultLauncher() {
        cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                startUcrop(sourceUri);
            }
        });
    }

    private void initCameraxActivityResultLauncher() {
        cameraxActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    textView.setText(result.getData().getStringExtra("code"));
                    imageButton.setVisibility(View.VISIBLE);
                    imageUri = result.getData().getStringExtra("uri");
                    bitmap = BitmapFactory.decodeFile(imageUri);
                    imageButton.setImageBitmap(bitmap);
                    imageButton.setOnClickListener(v -> {
                        Intent intent = new Intent(this, PhotoActivity.class);
                        intent.putExtra("uri", imageUri);
                        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
                    });
                }
            }
        });
    }

    private void initEditActivityResultLauncher() {
        editActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                textView.setText(result.getData().getCharSequenceExtra("text"));
                changed = true;
            }
        });
    }
    /**
     * 启动UCrop进行裁剪
     *
     * @param sourceUri 要裁剪的图片的URI
     */
    public void startUcrop(Uri sourceUri) {
        // 设置UCrop的选项
        UCrop.Options options = new UCrop.Options();
        options.setToolbarWidgetColor(getColor(android.R.color.tab_indicator_text));  // 设置工具栏部件颜色
        options.setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));  // 设置状态栏颜色
        options.setToolbarColor(SurfaceColors.SURFACE_2.getColor(this));  // 设置工具栏颜色
        options.setFreeStyleCropEnabled(true);  // 启用自由裁剪模式

        // 创建一个临时文件用于保存裁剪后的图片
        File newFile = new File(getApplicationContext().getCacheDir(), "temp.jpeg");

        // 使用UCrop启动裁剪操作
        UCrop.of(sourceUri, Uri.fromFile(newFile))
                .withOptions(options)
                .start(this, UCrop.REQUEST_CROP);  // 启动UCrop并请求裁剪结果
    }
    //对UCrop库的裁剪操作返回结果进行处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 处理UCrop裁剪结果
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                // 显示图片按钮并加载裁剪后的图片
                imageButton.setVisibility(View.VISIBLE);
                bitmap = toTurn(BitmapFactory.decodeFile(resultUri.getPath()), readPictureDegree(resultUri.getPath()));
                // 保存裁剪后的图片到内部存储
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                imageUri = fileSaveToInside(this, formatter.format(LocalDateTime.now()), bitmap);
                // 使用Glide加载图片到按钮
                Glide.with(this).load(imageUri).into(imageButton);
                // 设置点击图片按钮打开PhotoActivity的功能
                imageButton.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PhotoActivity.class);
                    intent.putExtra("uri", imageUri);
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
                });
                // 显示进度条
                process.setVisibility(View.VISIBLE);
                // 对图片进行OCR识别
                getText(bitmap);
            }
        }
    }
    //生成文本
    private void getText(Bitmap bitmap) {
        textView.setText("");  // 清空文本视图
        textView.setHint(R.string.processing___);  // 设置提示信息为处理中

        // 根据引擎编号选择文本识别方式
        if (engineNum >= 27) {
            // 使用Tesseract进行文本识别
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                mTess.setImage(bitmap);
                //mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD);
                String text = mTess.getUTF8Text();
                runOnUiThread(() -> {
                    process.setVisibility(View.GONE);  // 隐藏进度条
                    if (text.isEmpty()) {
                        textView.setText(getString(R.string.nothing));  // 如果识别结果为空，显示"无内容"
                    } else {
                        textView.setText(text);  // 显示识别结果
                    }
                });
            });
            executor.shutdown();  // 关闭线程池
        } else if (engineNum >= 0 && engineNum <= 26) {
            // 使用ML Kit Vision进行文本识别
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        process.setVisibility(View.GONE);  // 隐藏进度条
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Text.TextBlock textBlock : visionText.getTextBlocks()) {
                            for (Text.Line textLines : textBlock.getLines()) {
                                stringBuilder.append(textLines.getText()).append(" ");
                            }
                            stringBuilder.append("\n");
                        }
                        if (stringBuilder.toString().isEmpty()) {
                            textView.setText(getString(R.string.nothing));  // 如果识别结果为空，显示"无内容"
                        } else {
                            textView.setText(stringBuilder.toString());  // 显示识别结果
                        }
                    }).addOnFailureListener(e -> {
                        process.setVisibility(View.GONE);  // 隐藏进度条
                        textView.setText(getString(R.string.nothing));  // 显示"无内容"
                    });
        }
    }
}
