package com.example.simpleocr.utils;

// Piper TTS 工具类

import com.example.simpleocr.utils.wav.WavHeader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class PiperTtsUtil {

    // 加载so库
//    static {
//        System.loadLibrary("tts");
//    }
    private static final float noiseScale = 0.667f; // 噪声放缩比例
    private static final float lengthScale = 1.0f; // 长度放缩比例
    private static final float noiseW = 0.8f; // 噪声宽度
    private static final float MAX_WAV_VALUE = 32767.0f; // 放缩基准

    private static DataOutputStream dos;

    private static final Map<String, Integer> SAMPLE_RATE = new HashMap<>(){{
        put("ko", 44100);
        put("fa",32000);
        put("id", 32000);
        put("th",32000);
        put("ur",32000);
        put("ar",22050);
        put("de",22050);
        put("en",22050);
        put("es",22050);
        put("ru",22050);
        put("ja",22050);
        put("tr",22050);
        put("fr", 16000);
        put("pt", 16000);
        put("it", 16000);
    }};

    protected static final int sampleWidth = 2; // 16-bit
    protected static final short channels = 1;    // 单声道

    private static final OrtEnvironment env = OrtEnvironment.getEnvironment(); // OnnxRuntime 环境

    private static final OrtSession.SessionOptions options = new OrtSession.SessionOptions(); // OnnxRuntime 会话


    private static native long[] textToPhonemeId(String text, String lang,String dataPath); // 原生C++实现文字转音素ID

    // int-32 转字节数组
    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    // int-16 转字节数组
    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

    // 打开文件
    private static void openFile(String path) throws IOException {
        if(dos != null) {
            closeFile();
        }
        dos = new DataOutputStream(new FileOutputStream(path));
    }

    // 关闭文件
    private static void closeFile() throws IOException {
        if(dos != null) {
            dos.close();
            dos = null;
        }

    }

    // 写wav文件头
    private static void writeHeader(WavHeader header) throws IOException {

        if(dos == null) {
            return;
        }
        dos.writeBytes(header.riff);
        dos.write(intToByteArray(header.chunkSize));
        dos.writeBytes(header.wave);
        dos.writeBytes(header.fmt);
        dos.write(intToByteArray(header.fmtSize), 0, 4);
        dos.write(shortToByteArray(header.audioFmt), 0, 2);
        dos.write(shortToByteArray(header.numChannels), 0, 2);
        dos.write(intToByteArray(header.sampleRate), 0, 4);
        dos.write(intToByteArray(header.byteRate), 0, 4);
        dos.write(shortToByteArray(header.blockAlign), 0, 2);
        dos.write(shortToByteArray(header.bitsPerSample), 0, 2);
        dos.writeBytes(header.data);
        dos.write(intToByteArray(header.dataSize), 0, 4);
    }

    // 写wav文件数据
    private static void writeData(short[] audio) throws IOException {
        for(short a: audio) {
            dos.write(shortToByteArray(a),0,2);
        }

    }

    // 写wav文件
    private static void writeAudio(String path, WavHeader header, short[] audio) throws IOException {
        openFile(path);
        writeHeader(header);
        writeData(audio);
        closeFile();
    }


    // TTS函数
    public static void synthAndWrite(byte[] modelArray, String dataPath, String text, String langCode, String audioPath) throws OrtException, IOException {
        // variables
        long[] phonemeIds = textToPhonemeId(text,langCode,dataPath);
        long[][] inputPhonemeIds = new long[1][phonemeIds.length];
        long[] phonemeIdSize = {(long) phonemeIds.length};
        float[] scales = {noiseScale,lengthScale,noiseW};


        inputPhonemeIds[0] = phonemeIds;
        OrtSession session = env.createSession(modelArray,options); // 加载Onnx模型


        Map<String, OnnxTensor> inputs = Map.of(
                "input", OnnxTensor.createTensor(env, inputPhonemeIds),
                "input_lengths", OnnxTensor.createTensor(env, phonemeIdSize),
                "scales",OnnxTensor.createTensor(env,scales)

        ); // OnnxRuntime 输入
        Set<String> outputNames = Set.of("output"); // OnnxRuntime 输出名
        OrtSession.Result result = session.run(inputs,outputNames); // Onnx 推理
        OnnxTensor value = null;
        for(Map.Entry<String, OnnxValue> r: result) {
            value = (OnnxTensor) r.getValue();
        }
        final float[][][][] output = (float[][][][]) value.getValue();
        final float[] audio = output[0][0][0]; // 得到原始音频数据

        // 音频数据处理为short类型，以便写入wav文件
        float maxAudioValue = 0.01f;
        for (float v : audio) {
            float audioVal = Math.abs(v);
            if(audioVal > maxAudioValue) {
                maxAudioValue = audioVal;
            }
        }
        float audioScale = (MAX_WAV_VALUE / Math.max(0.01f, maxAudioValue));
        short[] shortAudio = new short[audio.length];
        for(int i = 0; i<audio.length;i++) {
            float tempAudio = audio[i] * audioScale;
            shortAudio[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (long) tempAudio));
        }

        // 写入wav文件
        WavHeader header;
        header = new WavHeader(SAMPLE_RATE.get(langCode),sampleWidth,channels,shortAudio.length);
        writeAudio(audioPath, header, shortAudio);
        session.close();


    }

}
