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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.simpleocr.Model.OcrItem;
import com.example.simpleocr.utils.OkHttpUtil;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.hutool.json.JSONUtil;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;

/**
 * @author 30415
 */
@ExperimentalGetImage
public class OcrActivity extends AppCompatActivity {
    SpinKitView process;
    ImageButton copy, edit, share;
    ShapeableImageView imageButton;
    MaterialTextView textView;
    private Bitmap bitmap;
    ActivityResultLauncher<Intent> galleryActivityResultLauncher, cameraActivityResultLauncher,
            editActivityResultLauncher, cameraxActivityResultLauncher;
    Uri sourceUri;
    TessBaseAPI mTess;
    OcrItem ocrItem;
    private boolean oldItem;
    String imageUri = "", dateStr = "";
    private boolean changed = false;
    TextRecognizer textRecognizer;
    private int engineNum;

    private void translateOcrtozh(String ocrText) {
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
                    src = "notsupport";
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
                    src = "notsupport";
                    break;
                case 19:
                    src = "notsupport";
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
            baidutranslation.baiduTranslation(ocrText, src, "zh", new baidutranslation.TranslationListener() {
                @Override
                public void onTranslationResult(String result) {
                    // 处理翻译结果
                    textView.setText(ocrText+"\n\n  翻译结果： \n\n " + result);
                }
            });
        }

        /*

        translate.otherToZh(ocrText, src, new translate.TranslationListener() {
            @Override
            public void onTranslationResult(String result) {
                // 处理翻译结果，例如更新 UI
                //String combinedText = ocrText+"\n\n  翻译结果： \n\n " + result;
                textView.setText(ocrText+"\n\n  翻译结果： \n\n " + result);
            }
        });*/

        //String combinedText = ocrText+"\n\n  翻译结果： \n\n " + result;
        //textView.setText(combinedText);

    }


    @Override
    public void finish() {
        String text = Objects.requireNonNull(textView.getText()).toString();
        if (!text.isEmpty() || !imageUri.isEmpty()) {
            if (changed) {
                ocrItem.setText(text);
                ocrItem.setImage(imageUri);
                Intent intent = new Intent();
                intent.putExtra("ocr_item", ocrItem);
                setResult(Activity.RESULT_OK, intent);
            }
        }
        if (!oldItem) {

            if (engineNum >=27) {
                //使用第二个OCR模型
                //mTess.recycle();
                textRecognizer.close();
            } else if (engineNum >= 0&&engineNum<=26) {
                //使用第一个模型
                textRecognizer.close();
            }
        }
        super.finish();
    }

    private void initUi() {
        process = findViewById(R.id.spin_kit);
        share = findViewById(R.id.buttonShare);
        edit = findViewById(R.id.buttonEdit);
        imageButton = findViewById(R.id.imageButton);
        copy = findViewById(R.id.buttonCopy);
        textView = findViewById(R.id.textView);

        copy.setOnClickListener(view -> {
            if (!Objects.requireNonNull(textView.getText()).toString().isEmpty()) {
                ClipData clipData = ClipData.newPlainText("", textView.getText());
                ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clipData);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
            }
        });
        edit.setOnClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("text", textView.getText().toString());
                editActivityResultLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, textView, "text"));
            }
        });
        share.setOnClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                try {
                    CharSequence res = textView.getText();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, res.toString());
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
                } catch (Exception e) {
                    Toast.makeText(this, "???", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button buttontranslate=(Button)findViewById(R.id.translate_button);
        buttontranslate.setOnClickListener(view -> {
            String text = Objects.requireNonNull(textView.getText()).toString();
            //如果在文本中检测到了“translation result:”，按钮无效，不调用下面的翻译函数
//            if (!text.contains("翻译结果：")) {
//                // 在这里调用新的翻译OCR函数
//                translateOcr(text);
//            }
            //在这里调用新的翻译OCR函数
            translateOcrtozh(text);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (changed) {
                finish();
            } else {
                finishAfterTransition();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);

        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_ocr);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        initUi();
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

        initGalleryActivityResultLauncher();
        initCameraActivityResultLauncher();
        initEditActivityResultLauncher();
        initCameraxActivityResultLauncher();

        ocrItem = new OcrItem();
        oldItem = true;
        if (null != getIntent().getExtras() && getIntent().getExtras().containsKey("old_ocr")) {
            ocrItem = (OcrItem) getIntent().getExtras().getSerializable("old_ocr");
            if (ocrItem != null) {
                textView.setText(ocrItem.getText());
                imageUri = ocrItem.getImage();
                dateStr = ocrItem.getDate();
            }
            bitmap = BitmapFactory.decodeFile(imageUri);
            imageButton.setVisibility(View.VISIBLE);
            imageButton.setImageBitmap(bitmap);
            imageButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhotoActivity.class);
                intent.putExtra("uri", imageUri);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
            });
        } else {
            engineNum = getIntent().getIntExtra("engine", -1);
            if (engineNum == 0) {
                textRecognizer =  TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
            } else if (engineNum == 1) {
                textRecognizer =  TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
            } else if(engineNum==2){
                textRecognizer =  TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
            }  else if(engineNum>=3&&engineNum<=23){
                textRecognizer =  TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            }else if(engineNum>=24&&engineNum<=26){
                textRecognizer =  TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
            }else if(engineNum>=27){
                //实际上是没有使用这个模型的，因为性能很差，备用
                String lang = getIntent().getStringExtra("langs");

                mTess = new TessBaseAPI();
                try {
                    mTess.init(getFilesDir().getAbsolutePath(), lang, TessBaseAPI.OEM_LSTM_ONLY);
                } catch (IllegalArgumentException ignored) {
                }
            }
            oldItem = false;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            dateStr = formatter.format(LocalDateTime.now());
            ocrItem.setDate(dateStr);
            changed = true;
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle(dateStr);
        if (getIntent().getStringExtra("launch") != null && "camera".equals(getIntent().getStringExtra("launch"))) {
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
        } else if (getIntent().getStringExtra("launch") != null && "scancode".equals(getIntent().getStringExtra("launch"))) {
            Intent intent = new Intent(this, CameraxActivity.class);
            cameraxActivityResultLauncher.launch(intent);
        }
        //翻译功能


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

    public void startUcrop(Uri sourceUri) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarWidgetColor(getColor(android.R.color.tab_indicator_text));
        options.setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        options.setToolbarColor(SurfaceColors.SURFACE_2.getColor(this));
        options.setFreeStyleCropEnabled(true);
        File newFile = new File(getApplicationContext().getCacheDir(), "temp.jpeg");
        UCrop.of(sourceUri, Uri.fromFile(newFile))
                .withOptions(options)
                .start(this, UCrop.REQUEST_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                imageButton.setVisibility(View.VISIBLE);
                bitmap = toTurn(BitmapFactory.decodeFile(resultUri.getPath()), readPictureDegree(resultUri.getPath()));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                imageUri = fileSaveToInside(this, formatter.format(LocalDateTime.now()), bitmap);
                Glide.with(this).load(imageUri).into(imageButton);
                imageButton.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PhotoActivity.class);
                    intent.putExtra("uri", imageUri);
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
                });
                process.setVisibility(View.VISIBLE);
                getText(bitmap);
            }
        }
    }

    private void getText(Bitmap bitmap) {
        textView.setText("");
        textView.setHint(R.string.processing___);
        if (engineNum >=27) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                mTess.setImage(bitmap);
                //mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD);
                String text = mTess.getUTF8Text();
                runOnUiThread(() -> {
                    process.setVisibility(View.GONE);
                    if (text.isEmpty()) {
                        textView.setText(getString(R.string.nothing));
                    } else {
                        textView.setText(text);
                    }
                });
            });
            executor.shutdown();
        } else if (engineNum >= 0&&engineNum <=26) {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        process.setVisibility(View.GONE);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Text.TextBlock textBlock : visionText.getTextBlocks()) {
                            for (Text.Line textLines : textBlock.getLines()) {
                                stringBuilder.append(textLines.getText()).append(" ");
                            }
                            stringBuilder.append("\n");
                        }
                        if (stringBuilder.toString().isEmpty()) {
                            textView.setText(getString(R.string.nothing));
                        } else {
                            textView.setText(stringBuilder.toString());
                        }
                    }).addOnFailureListener(f -> textView.setText(getString(R.string.nothing)));
        }
    }
}
