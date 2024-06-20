package com.example.simpleocr;
import static com.example.simpleocr.FileUtils.fileSaveToInside;
import static com.example.simpleocr.FileUtils.toTurn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;


import com.example.simpleocr.Views.FocusView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 相机活动，用于扫描条码并处理图像分析。
 */
@ExperimentalGetImage
public class CameraxActivity extends AppCompatActivity {
    private ImageView success; // 显示成功扫描的图像
    private ImageView imageView; // 显示相机预览的图像
    private ImageAnalysis imageAnalysis; // 图像分析用例
    private BarcodeScanner scanner; // 条码扫描器
    private PreviewView viewFinder; // 相机预览视图
    private FocusView focusView; // 焦点视图
    private Camera camera; // 相机实例
    private String savedUri; // 保存的图像 URI
    private float fingerSpacing = 0; // 双指缩放的初始间距

    // 设置相机的缩放比例
    private void setZoomRatio(float zoomRatio) {
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.setZoomRatio(zoomRatio);
    }

    // 获取相机的缩放状态
    private ZoomState getZoomState() {
        return camera.getCameraInfo().getZoomState().getValue();
    }

    // 根据增量值计算新的缩放比例
    private float getZoomRatio(float delta) {
        float zoomLevel = 1f;
        float newZoomLevel = zoomLevel + delta;
        if (newZoomLevel < 1f) {
            newZoomLevel = 1f;
        } else if (newZoomLevel > Objects.requireNonNull(getZoomState()).getMaxZoomRatio()) {
            newZoomLevel = getZoomState().getMaxZoomRatio();
        }
        return newZoomLevel;
    }

    // 获取双指之间的距离
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 在创建活动时调用
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camerax);
        // 初始化焦点视图
        focusView = new FocusView(this);
        addContentView(focusView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        focusView.setVisibility(View.GONE);
        success = findViewById(R.id.success);
        imageView = findViewById(R.id.imageView);
        ImageView back = findViewById(R.id.imageView1);
        back.getBackground().setAlpha(50);
        imageView.setClickable(true);
        imageView.bringToFront();

        // 初始化条码扫描器
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder().build();
        scanner = BarcodeScanning.getClient(options);

        // 启动相机
        startCamera();
    }

    // 启动相机并设置预览和图像分析
    @SuppressLint({"UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    private void startCamera() {
        // 将相机的生命周期与活动绑定
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // 获取相机提供者
                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();

                // 创建预览实例并设置 surface 提供者
                viewFinder = findViewById(R.id.preview);
                viewFinder.setScaleType(PreviewView.ScaleType.FILL_CENTER);
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // 选择后置摄像头
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 设置图像分析用例
                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new MyAnalyzer());

                // 重新绑定用例前先解绑
                processCameraProvider.unbindAll();

                // 绑定用例至相机
                camera = processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                // 设置点击事件以切换闪光灯状态
                imageView.setOnClickListener(v -> {
                    if (camera.getCameraInfo().hasFlashUnit()) {
                        if (null != camera.getCameraInfo().getTorchState().getValue()) {
                            if (camera.getCameraInfo().getTorchState().getValue() == TorchState.OFF) {
                                camera.getCameraControl().enableTorch(true);
                                imageView.setImageDrawable(getDrawable(R.drawable.baseline_flashlight_on_24));
                            } else {
                                camera.getCameraControl().enableTorch(false);
                                imageView.setImageDrawable(getDrawable(R.drawable.baseline_flashlight_off_24));
                            }
                        }
                    }
                });

                // 设置触摸事件以处理单指点击对焦和双指缩放
                viewFinder.setOnTouchListener((view, event) -> {
                    try {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN -> {
                                // 单指点击对焦
                                if (event.getPointerCount() == 1) {
                                    focusView.setCenter((int) event.getX(), (int) event.getY());
                                    focusView.setVisibility(View.VISIBLE);
                                    FocusMeteringAction action = new FocusMeteringAction.Builder(
                                            viewFinder.getMeteringPointFactory()
                                                    .createPoint(event.getX(), event.getY())).build();
                                    camera.getCameraControl().startFocusAndMetering(action);
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> focusView.setVisibility(View.GONE), 1000);
                                }
                            }
                            case MotionEvent.ACTION_POINTER_DOWN ->
                                // 双指缩放
                                    fingerSpacing = getFingerSpacing(event);
                            case MotionEvent.ACTION_MOVE -> {
                                // 处理双指缩放
                                if (event.getPointerCount() == 2) {
                                    float newFingerSpacing = getFingerSpacing(event);
                                    if (newFingerSpacing > fingerSpacing) {
                                        setZoomRatio(getZoomRatio(2f));
                                    } else if (newFingerSpacing < fingerSpacing) {
                                        setZoomRatio(getZoomRatio(-2f));
                                    }
                                    fingerSpacing = newFingerSpacing;
                                }
                            }
                            case MotionEvent.ACTION_POINTER_UP -> fingerSpacing = 0;
                            default -> {
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Error setting focus and exposure", "");
                    }
                    return true;
                });

            } catch (Exception ignored) {
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 图像分析器类，用于处理图像并进行条码扫描
    private class MyAnalyzer implements ImageAnalysis.Analyzer {
        @SuppressLint("RestrictedApi")
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            // 将图像转换为 Bitmap
            final Bitmap bitmap = imageProxy.toBitmap();
            // 将 Bitmap 转换为 InputImage
            InputImage image = InputImage.fromBitmap(bitmap, imageProxy.getImageInfo().getRotationDegrees());

            // 处理图像并进行条码扫描
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        // 处理成功扫描到的条码
                        StringBuilder codeInfo = new StringBuilder();
                        for (Barcode barcode : barcodes) {
                            int type = barcode.getValueType();
                            switch (type) {
                                case Barcode.TYPE_WIFI -> {
                                    String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    if (ssid != null && !ssid.isEmpty()) {
                                        codeInfo.append(getString(R.string.ssid)).append(" ").append(ssid

                                        ).append("\n");
                                    }
                                    if (password != null && !password.isEmpty()) {
                                        codeInfo.append(getString(R.string.password)).append(" ").append(password).append("\n");
                                    }
                                }
                                case Barcode.TYPE_URL -> {
                                    String title = Objects.requireNonNull(barcode.getUrl()).getTitle();
                                    String uri = barcode.getUrl().getUrl();
                                    if (title != null && !title.isEmpty()) {
                                        codeInfo.append(getString(R.string.title)).append(" ").append(title).append("\n");
                                    }
                                    if (uri != null && !uri.isEmpty()) {
                                        codeInfo.append(getString(R.string.uri)).append(" ").append(uri).append("\n");
                                    }
                                }
                                case Barcode.TYPE_EMAIL -> {
                                    String address = Objects.requireNonNull(barcode.getEmail()).getAddress();
                                    String body = barcode.getEmail().getBody();
                                    if (address != null && !address.isEmpty()) {
                                        codeInfo.append(getString(R.string.address)).append(" ").append(address).append("\n");
                                    }
                                    if (body != null && !body.isEmpty()) {
                                        codeInfo.append(getString(R.string.body)).append(" ").append(body).append("\n");
                                    }
                                }
                                case Barcode.TYPE_PHONE -> {
                                    String number = Objects.requireNonNull(barcode.getPhone()).getNumber();
                                    if (number != null && !number.isEmpty()) {
                                        codeInfo.append(getString(R.string.phone)).append(" ").append(number).append("\n");
                                    }
                                }
                                default -> {
                                    String raw = barcode.getRawValue();
                                    codeInfo.append(raw).append("\n");
                                }
                            }
                        }
                        // 如果扫描到条码，显示成功图像并保存
                        if (!codeInfo.toString().isEmpty()) {
                            success.bringToFront();
                            Bitmap res = toTurn(bitmap, imageProxy.getImageInfo().getRotationDegrees());
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                            savedUri = fileSaveToInside(CameraxActivity.this, formatter.format(LocalDateTime.now()), res);
                            Intent intent = new Intent();
                            intent.putExtra("code", codeInfo.toString().trim());
                            intent.putExtra("uri", savedUri);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }).addOnCompleteListener(c -> imageProxy.close());
        }
    }
}
