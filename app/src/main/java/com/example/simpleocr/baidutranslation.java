package com.example.simpleocr;
import android.os.AsyncTask;

import com.baidu.translate.demo.TransApi;
/*package com.example.simpleocr;



public class baidutranslation {

    // 在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
    private static final String APP_ID = "20240419002029557";
    private static final String SECURITY_KEY = "BLag7rp1CtdBZMC7DFRj";

    public static String baidu_translation(String query, String src, String dst) {
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);


        return api.getTransResult(query, src, dst);
    }

}
import android.os.AsyncTask;

public class baidutranslation {

    private static final String APP_ID = "20240419002029557";
    private static final String SECURITY_KEY = "69mbJODhNSZ1XbVxkJYb";

    public static void baiduTranslation(String ocrText, String src, String tgt, TranslationListener listener) {
        // 创建一个 AsyncTask 来执行网络请求
        TranslationTask task = new TranslationTask(ocrText, src, tgt, listener);
        task.execute();
    }

    private static class TranslationTask extends AsyncTask<Void, Void, String> {
        private String ocrText;
        private String src;
        private String tgt;
        private TranslationListener listener;

        public TranslationTask(String ocrText, String src, String tgt, TranslationListener listener) {
            this.ocrText = ocrText;
            this.src = src;
            this.tgt = tgt;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Void... voids) {
            // 在后台线程中执行网络请求
            TransApi api = new TransApi(APP_ID, SECURITY_KEY);
            return api.getTransResult(ocrText, src, tgt);
        }

        @Override
        protected void onPostExecute(String result) {
            // 在 UI 线程中处理网络请求的结果，并将结果传递给监听器
            if (listener != null) {
                listener.onTranslationResult(result);
            }
        }
    }

    public interface TranslationListener {
        void onTranslationResult(String result);
    }
}

*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class baidutranslation {

    private static final String APP_ID = "20240419002029557";
    private static final String SECURITY_KEY = "69mbJODhNSZ1XbVxkJYb";

    public static void baiduTranslation(String ocrText, String src, String tgt, TranslationListener listener) {
        // 创建一个 AsyncTask 来执行网络请求
        TranslationTask task = new TranslationTask(ocrText, src, tgt, listener);
        task.execute();
    }

    private static class TranslationTask extends AsyncTask<Void, Void, String> {
        private String ocrText;
        private String src;
        private String tgt;
        private TranslationListener listener;

        public TranslationTask(String ocrText, String src, String tgt, TranslationListener listener) {
            this.ocrText = ocrText;
            this.src = src;
            this.tgt = tgt;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Void... voids) {
            // 在后台线程中执行网络请求
            TransApi api = new TransApi(APP_ID, SECURITY_KEY);
            return api.getTransResult(ocrText, src, tgt);
        }

        @Override
        protected void onPostExecute(String result) {
            // 在 UI 线程中处理网络请求的结果，并将提取出来的翻译文本传递给监听器
            if (listener != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    JSONArray transResult = jsonResult.getJSONArray("trans_result");
                    StringBuilder translatedText = new StringBuilder();
                    for (int i = 0; i < transResult.length(); i++) {
                        JSONObject item = transResult.getJSONObject(i);
                        translatedText.append(item.getString("dst"));
                    }
                    listener.onTranslationResult(translatedText.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface TranslationListener {
        void onTranslationResult(String result);
    }
}
