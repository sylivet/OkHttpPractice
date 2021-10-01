package com.test.okhttp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView mTextViewResult;
    private TextView mTextViewResult2;
    Button button;
    String ans1;
    String ans2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewResult = findViewById(R.id.text_view_result);
        mTextViewResult2 = findViewById(R.id.text_view_result2);
        button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextViewResult.setText(ans1);
                mTextViewResult2.setText(ans2);
            }
        });

        OkHttpClient client = new OkHttpClient();
        String url = "https://alphamallappconfigdev.blob.core.windows.net/config/config-a.json";
        Request request = new Request.Builder().url(url).build();

        //同步請求call.execute()，會阻塞當前線程

        //異步請求call.enqueue，在子線程做執行
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        Iterator<String> keys = jsonObject.keys();

                        /**
                         * 糜爛的解答
                         ans1=jsonObject.getJSONObject("wallet").getString("get_transaction_data");
                         mTextViewResult2.setText(jsonObject.getJSONArray("customerFeatures").getJSONObject(2).getString("promo_page");

                         * 第一版本解答
                        for (int i =0; i<jsonObject.getJSONArray("customerFeatures").length();i++){
                            if (jsonObject.getJSONArray("customerFeatures").getJSONObject(i).getString("label_name_zh").equals("直播購物")){
                                ans2=jsonObject.getJSONArray("customerFeatures").getJSONObject(i).getString("promo_page");
                                break;
                            }
                        }
                        */

                        while (keys.hasNext()) {
                            String key = keys.next();

                            //1. key = get_transaction_data 的 value
                            if (jsonObject.get(key) instanceof JSONObject) {
                                if (jsonObject.getJSONObject(key).has("get_transaction_data")) {
                                    ans1 = jsonObject.getJSONObject(key).getString("get_transaction_data");
                                }
                            }

                            //2. 在 key = label_name_zh, value = “直播購物” 中的key = promo_page 的 value
                            if (jsonObject.get(key) instanceof JSONArray) {
                                if (jsonObject.getJSONArray(key) != null) {
                                    for (int i = 0; i < jsonObject.getJSONArray(key).length(); i++) {
                                        if (jsonObject.getJSONArray(key).getJSONObject(i).has("label_name_zh")) {
                                            if (jsonObject.getJSONArray(key).getJSONObject(i).getString("label_name_zh").equals("直播購物")) {
                                                ans2 = jsonObject.getJSONArray(key).getJSONObject(i).getString("promo_page");
                                                System.out.println(ans2);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        /**
                         * 跑那個迴圈會抓不到，畫面無法更新（？）
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewResult.setText(ans1);
                                mTextViewResult2.setText(ans2);
                            }
                        });
                        */
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}