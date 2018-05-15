package com.example.junmp.togetherhelpee;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CallActivity extends AppCompatActivity {
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private GpsInfo gps;

    private RadioGroup radioGroup;
    private RadioButton one, two, three, four, five;
    private int checked = 1;

    private RadioGroup radioGroup2;
    private RadioButton outside, talk, housework, education;
    private String type = "outside";

    private int flag_speech = 0;

    private String date;
    private String time;
    private String etc;

    Intent intent;
    SpeechRecognizer mRecognizer;
    TextView textView;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private final int CAMERA_PERMISSIONS_GRANTED = 1;
    String phone_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call);

        Intent fromMain = getIntent();

        if(fromMain.getStringExtra("phonenum") != null){
            phone_num = (String)fromMain.getStringExtra("phonenum");
        }
        Log.d("fads",phone_num);

        textView = (TextView) findViewById(R.id.textView);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO
                );
            }
        }

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);




        Button btn_call = (Button) findViewById(R.id.btn_call);
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecognizer.startListening(intent);

            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.one) {
                    checked = 1;
                } else if(checkedId == R.id.two) {
                    checked = 2;
                } else if(checkedId == R.id.three) {
                    checked = 3;
                } else if(checkedId == R.id.four) {
                    checked = 4;
                } else {
                    checked = 5;
                }
            }

        });

        one = (RadioButton) findViewById(R.id.one);
        two = (RadioButton) findViewById(R.id.two);
        three = (RadioButton) findViewById(R.id.three);
        four = (RadioButton) findViewById(R.id.four);
        five = (RadioButton) findViewById(R.id.five);

        radioGroup2 = (RadioGroup) findViewById(R.id.myRadioGroup2);

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.outside) {
                    type = "outside";
                } else if(checkedId == R.id.talk) {
                    type = "talk";
                } else if(checkedId == R.id.housework) {
                    type = "housework";
                } else {
                    type = "education";
                }
            }

        });

        one = (RadioButton) findViewById(R.id.one);
        two = (RadioButton) findViewById(R.id.two);
        three = (RadioButton) findViewById(R.id.three);
        four = (RadioButton) findViewById(R.id.four);
        five = (RadioButton) findViewById(R.id.five);

        Button btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag_speech == 0){
                    Toast.makeText(CallActivity.this,"먼저 도움요청 버튼을 누르시고 도움받으실 내용을 말씀해주세요.",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(CallActivity.this, type, Toast.LENGTH_SHORT).show();
                    Toast.makeText(CallActivity.this, String.valueOf(checked), Toast.LENGTH_SHORT).show();
                    if (!isPermission) {
                        callPermission();
                        return;
                    }

                    gps = new GpsInfo(CallActivity.this);
                    // GPS 사용유무 가져오기
                    if (gps.isGetLocation()) {

                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        Log.d("fdasds",String.valueOf(latitude));
                        Log.d("fdasds",String.valueOf(longitude));

                        Toast.makeText(
                                getApplicationContext(),
                                "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                                Toast.LENGTH_LONG).show();
                    } else {
                        // GPS 를 사용할수 없으므로
                        gps.showSettingsAlert();
                    }
                }


            }
        });
        callPermission();
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            textView.setText("예시) 8월 17일 오전 2시에 삼성역까지 데려다 주세요");
            Toast.makeText(CallActivity.this,"너무 늦게 말하면 오류뜹니다",Toast.LENGTH_SHORT).show();
            flag_speech = 0;

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onResults(Bundle bundle) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = bundle.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            String message = rs[0];

            messageSeparate(message);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };

    private void messageSeparate(String message){
        int flag = 0;
        String[] words = message.split("\\s");

        StringBuffer result = new StringBuffer();
        StringBuffer result_date = new StringBuffer();
        int temp_a = 0;
        int temp_b = 0;
        int temp_c = 0;

        int temp_m = 0;
        int temp_n = 0;

        String result_time = "";
        String result_vol = "";
        String result_day = "";

        int date_flag = 0;
        int end_flag = -1;

        for (int i = 0; i<words.length; i++){
            if (temp_m == 0 && words[i].contains("월")){
                result_date.append(words[i]);
                temp_m = 1;

                continue;
            }
            if (temp_n ==0 && words[i].contains("일")){
                result_date.append(" " + words[i]);
                temp_n = 1;

                continue;
            }
        }
        if(temp_m == 1 && temp_n == 1){
            date_flag = 1;
            result_day = result_date.toString();
        }
        if (date_flag == 0){
            textView.setText("예시) 8월 17일 오전 2시에 삼성역까지 데려다 주세요");
            Toast.makeText(CallActivity.this,"다시 한번 말씀해주세요. 날짜정보가 정확하지 않습니다.",Toast.LENGTH_SHORT).show();
        }

        for (int i=0; i<words.length; i++) {
            if (temp_a == 0 && (words[i].equals("오전") || words[i].equals("오후"))) {
                result.append(words[i]);

                temp_a = 1;
                continue;
            }
            if (temp_b == 0 && words[i].contains("시")) {
                words[i] = words[i].split("에")[0];
                result.append(" "+words[i]);
                end_flag = i;
                temp_b = 1;
                continue;
            }
            if (temp_c == 0 && (words[i].contains("에") || words[i].contains("반"))) {
                words[i] = words[i].split("에")[0];
                result.append(" "+words[i]);
                end_flag = i;
                temp_c = 1;
                continue;
            }
        }
        int time_flag = 0;

        if(date_flag == 1 && temp_a==1 && temp_b==1) {
            result_time = result.toString();
            time_flag = 1;
        }
        else {
            textView.setText("예시) 8월 17일 오전 2시에 삼성역까지 데려다 주세요");
            Toast.makeText(CallActivity.this,"다시 한번 말씀해주세요. 시간정보가 정확하지 않습니다.",Toast.LENGTH_SHORT).show();
        }
        if (time_flag==1) {
            int keyword_flag = -1;

            if (end_flag == words.length - 1) {
                textView.setText("예시) 8월 17일 오전 2시에 삼성역까지 데려다 주세요");
                Toast.makeText(CallActivity.this,"다시 한번 말씀해주세요. 봉사정보가 정확하지 않습니다.",Toast.LENGTH_SHORT).show();
            }
            else {
                for (int j=0; j<words.length; j++){
                    if (words[j].contains("봉사")){
                        keyword_flag = j;
                    }
                }
                if (keyword_flag == -1) {
                    keyword_flag = words.length;

                    String result_vol_temp = words[end_flag+1];
                    for(int k = end_flag+2; k<keyword_flag; k++){
                        result_vol_temp += " " + words[k];
                    }
                    result_vol = result_vol_temp;

                    String result_msg = "봉사 날짜 : " + result_day + "\n" + "봉사 시간 : " + result_time + "\n" + "봉사 종류 : " + result_vol;
                    date = result_day;
                    time = result_time;
                    etc = result_vol;
                    flag = 1;

                    textView.setText(result_msg);
                }
                else {
                    String result_vol_temp = words[end_flag+1];
                    for(int k = end_flag+2; k<keyword_flag; k++){
                        result_vol_temp += " " + words[k];
                    }

                    result_vol = result_vol_temp + " 봉사";

                    String result_msg = "봉사 날짜 : " + result_day + "\n" + "봉사 시간 : " + result_time + "\n" + "봉사 종류 : " + result_vol;
                    date = result_day;
                    time = result_time;
                    etc = result_vol;
                    flag = 1;

                    textView.setText(result_msg);
                }
            }
        }
        flag_speech = flag;
    }

    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }
/*
    class postRequest extends AsyncTask<Bitmap, Void, String> {
        RequestHandler rh = new RequestHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            String uploadImage = getStringImage(bitmap);
            HashMap<String, String, String, String, String, String, > data = new HashMap<>();
            data.put(UPLOAD_KEY, uploadImage);

            String result = rh.sendPostRequest(UPLOAD_URL, data, userId);

            return result;
        }
    }
    */

}
