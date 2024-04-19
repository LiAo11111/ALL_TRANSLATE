//这是使用腾讯翻译接口的代码
package com.example.simpleocr;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.*;
import java.util.logging.Logger;
import android.os.AsyncTask;
import java.util.logging.Logger;

public class translate {

    public interface TranslationListener {
        void onTranslationResult(String result);
    }

    public static void otherToZh(String content, String src, TranslationListener listener) {
        TranslateAsyncTask task = new TranslateAsyncTask(content, src, "zh", listener);
        task.execute();
    }

    public static void zhToother(String content, String target, TranslationListener listener) {
        TranslateAsyncTask task = new TranslateAsyncTask(content, "zh", target, listener);
        task.execute();
    }

    private static class TranslateAsyncTask extends AsyncTask<Void, Void, String> {
        private String content;
        private String src;
        private String target;
        private TranslationListener listener;

        public TranslateAsyncTask(String content, String src, String target, TranslationListener listener) {
            this.content = content;
            this.src = src;
            this.target = target;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String secretId = "";
                String secretKey = "";
                Credential cred = new Credential(secretId, secretKey);
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint("tmt.tencentcloudapi.com");
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile);
                TmtClient client = new TmtClient(cred, "ap-shanghai", clientProfile);
                TextTranslateRequest req = new TextTranslateRequest();
                req.setSourceText(content);
                req.setSource(src);
                req.setTarget(target);
                req.setProjectId(0L);
                TextTranslateResponse resp = client.TextTranslate(req);
                return resp.getTargetText();
            } catch (TencentCloudSDKException e) {
                Logger log = Logger.getLogger(translate.class.getName());
                log.info("腾讯云接口请求错误，错误信息：" + e.getMessage());
                return "腾讯云接口请求错误";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (listener != null) {
                listener.onTranslationResult(result);
            }
        }
    }
}
