package com.example.junmp.togetherhelpee;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Integer.valueOf;

public class Call1Activity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    String idByTelephonyManager;

    checkUser checkUser;
    postKey postKey;
    putKey putKey;
    checkDevice checkDevice;

    private static final int REQUEST_READ_PHONE_STATE_PERMISSION = 225;

    private int flag_speech = 0;

    private String date;
    private String time;
    private String etc;

    String dest_date;
    String dest_time;

    Intent intent;
    SpeechRecognizer mRecognizer;

    TextView textView;
    TextView textView2;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private final int CAMERA_PERMISSIONS_GRANTED = 1;
    String phone_num;

    String mJsonString;
    getVolunteer getVolunteer;
    getWait getWait;
    int haveRegisted = 0;

    ProgressDialog pd = null;

    int wheretogo = 0;

    Button btn_next;
    Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_call1);

        Intent from = getIntent();

        wheretogo = from.getIntExtra("from", 0);

        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            idByTelephonyManager = mgr.getDeviceId();
            Log.d("abc", "Abc");
            phone_num = mgr.getLine1Number();//mgr.getLine1Number();
            phone_num = phone_num.replace("+82", "0");
            Log.d("abc", phone_num);
        } catch (Exception e) {
        }

        textView = (TextView) findViewById(R.id.textView);

        textView.setText("예시) 8월 17일");

        getWait = new getWait();
        getWait.execute("http://210.89.191.125/helpee/volunteer/");


        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                idByTelephonyManager = mgr.getDeviceId();
                phone_num = mgr.getLine1Number();//mgr.getLine1Number();
                phone_num = phone_num.replace("+82", "0");
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(Call1Activity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);




        ImageView btn_call = (ImageView) findViewById(R.id.btn_call);
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_next.setVisibility(View.VISIBLE);
                btn_back.setVisibility(View.VISIBLE);
                mRecognizer.startListening(intent);
            }
        });

        btn_next = findViewById(R.id.btn_yes);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDevice = new checkDevice();
                checkDevice.execute("http://210.89.191.125/helpee/device");


            }
        });
        btn_back = findViewById(R.id.btn_no);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecognizer.startListening(intent);
            }
        });

        btn_next.setVisibility(View.INVISIBLE);
        btn_back.setVisibility(View.INVISIBLE);
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            pd = new ProgressDialog(Call1Activity.this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage("요청사항을 마이크에 대고 예시와 같이 말씀해주세요.\n예시) 8월 17일");
            pd.show();
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
            if(pd != null){
                pd.dismiss();
            }
            textView.setText("예시) 8월 17일");
            Toast.makeText(Call1Activity.this,"너무 늦게 말하면 오류뜹니다",Toast.LENGTH_SHORT).show();
            flag_speech = 0;

        }

        @Override
        public void onResults(Bundle bundle) {
            if(pd != null){
                pd.dismiss();
            }
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = bundle.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            String message = rs[0];

            messageSeparate(message);

            if(flag_speech==1) {
                messageCheck();

                textView.setText("\'"+dest_date+"\'\n이 날짜가 맞나요?");
            }/*
            Toast.makeText(getApplicationContext(), textView.getText().toString(),Toast.LENGTH_LONG).show();*/
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
            if (temp_n ==0 && words[i].contains("일") && !(words[i].contains("내일")) && !(words[i].contains("네일"))){
                result_date.append(" " + words[i]);
                temp_n = 1;

                continue;
            }
            if(words[i].contains("오늘") || words[i].contains("오널")){
                Calendar today = Calendar.getInstance ( );
                today.add ( Calendar.DATE, 0 );

                Date tomorrow = today.getTime ( );
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String a = sdf.format(tomorrow);
                result_day = a.substring(4,6)+"월 "+a.substring(6,8)+"일";
                date_flag = 1;
                break;
            }
            if(words[i].contains("모레") || words[i].contains("모래")){
                Calendar today = Calendar.getInstance ( );
                today.add ( Calendar.DATE, 2 );

                Date tomorrow = today.getTime ( );
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String a = sdf.format(tomorrow);
                result_day = a.substring(4,6)+"월 "+a.substring(6,8)+"일";
                date_flag = 1;
                break;
            }
            if(words[i].contains("내일") || words[i].contains("네일")){
                Calendar today = Calendar.getInstance ( );
                today.add ( Calendar.DATE, 1 );

                Date tomorrow = today.getTime ( );
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String a = sdf.format(tomorrow);
                result_day = a.substring(4,6)+"월 "+a.substring(6,8)+"일";
                date_flag = 1;
                break;
            }
            if(words[i].contains("글피")){
                Calendar today = Calendar.getInstance ( );
                today.add ( Calendar.DATE, 3);

                Date tomorrow = today.getTime ( );
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String a = sdf.format(tomorrow);
                result_day = a.substring(4,6)+"월 "+a.substring(6,8)+"일";
                date_flag = 1;
                break;
            }
        }
        if(temp_m == 1 && temp_n == 1){
            date_flag = 1;
            result_day = result_date.toString();
        }
        if (date_flag == 0){
            textView.setText("예시) 8월 17일");
            Toast.makeText(Call1Activity.this,"다시 한번 말씀해주세요. 날짜정보가 정확하지 않습니다.",Toast.LENGTH_SHORT).show();
        }
        else{
            date = result_day;
        }
        flag_speech = date_flag;
    }
    private void messageCheck(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());

        String current_year = strToday.substring(0, 4);
        String current_month = strToday.substring(4, 6);
        String current_day = strToday.substring(6, 8);

        String dest_year = current_year;
        String dest_month = date.split("월")[0];
        if(dest_month.equals("")){
            dest_month = current_month;
        }
        String[] dest_days = date.split(" ");
        String dest_day="";
        for(int i = 0; i<dest_days.length; i++){
            if(dest_days[i].contains("일")){
                dest_day = dest_days[i].split("일")[0];
            }
        }
        if(dest_day.equals("")){
            dest_day = current_day;
        }

        if(Integer.valueOf(dest_month) < Integer.valueOf(current_month)){
            int plusYear = Integer.valueOf(dest_year) + 1;
            dest_year = String.valueOf(plusYear);
        }
        else if(Integer.valueOf(dest_month) == Integer.valueOf(current_month) && Integer.valueOf(dest_day) < Integer.valueOf(current_day)){
            textView.setText("예시) 8월 17일");
            Toast.makeText(Call1Activity.this, "오늘 이후의 날짜로 신청해주세요!", Toast.LENGTH_SHORT).show();
            flag_speech = 0;
        }

        dest_date = dest_year+"-"+dest_month+"-"+dest_day;
    }





    private class getVolunteer extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d("Asd",result);
            if (result.equals("[]")){/*
                Toast.makeText(getApplicationContext(), "예시) 8월 17일", Toast.LENGTH_SHORT).show();*/
                haveRegisted = 0;
            }
            else {
                mJsonString = result;
                try {
                    showResult();
                } catch (JSONException e) {
                    Log.d("fda","asd");
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0]+phone_num;

            Log.d("Asd",serverURL);
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("fadsfsads", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d("fadsfsads", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private void showResult() throws JSONException {
        try {
            JSONArray jsonArray = new JSONArray(mJsonString);

            int i=jsonArray.length()-1;
            JSONObject item = jsonArray.getJSONObject(i);

            int startStatus = item.getInt("startStatus");
            Log.d("Asd",startStatus+"");
            if(startStatus == 0){
                String type = item.getString("type");
                String helperId = item.getString("helperId");
                int matchingStatus = item.getInt("matchingStatus");
                String content = item.getString("content");
                String time = item.getString("time");
                int duration = item.getInt("duration");
                String date = item.getString("date");
                int volunteerId = item.getInt("volunteerId");

                if(matchingStatus == 2){
                    long current_time = System.currentTimeMillis();
                    SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String str = dayTime.format(new Date(current_time));

                    String dest_time = date+" "+time;

                    Date current = null;
                    Date dest = null;
                    try {
                        current = dayTime.parse(str);
                        dest = dayTime.parse(dest_time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = dest.getTime() - current.getTime();
                    long diffSeconds = diff / 1000;

                    Log.d("fdasfadfd",diffSeconds+"");

                    Log.d("fdasfadfd",str);
                    Log.d("fdasfadfd",dest_time+"");
                    if(diffSeconds <= 3600 && wheretogo == 0){
                        Intent toMap = new Intent(Call1Activity.this, MapActivity.class);
                        toMap.putExtra("phonenum", phone_num);
                        toMap.putExtra("volunteerId", volunteerId);
                        toMap.putExtra("helperId", helperId);
                        startActivity(toMap);
                        finish();
                    }
                    else {
                        Intent toStart = new Intent(Call1Activity.this, StartActivity.class);
                        toStart.putExtra("type", type);
                        toStart.putExtra("helperId", helperId);
                        toStart.putExtra("content", content);
                        toStart.putExtra("time", time);
                        toStart.putExtra("duration", duration);
                        toStart.putExtra("date", date);
                        toStart.putExtra("volunteerId", volunteerId);
                        toStart.putExtra("phonenum", phone_num);
                        startActivity(toStart);
                        finish();
                    }
                }
                else{
                    Intent toMatch = new Intent(Call1Activity.this, MatchActivity.class);
                    toMatch.putExtra("type", type);
                    toMatch.putExtra("helperId", helperId);
                    toMatch.putExtra("matchingStatus", matchingStatus);
                    toMatch.putExtra("content", content);
                    toMatch.putExtra("time", time);
                    toMatch.putExtra("duration", duration);
                    toMatch.putExtra("date", date);
                    toMatch.putExtra("volunteerId", volunteerId);
                    toMatch.putExtra("phonenum", phone_num);

                    startActivity(toMatch);

                    finish();
                }

            }
            else if(startStatus == 1){
                int volunteerId = item.getInt("volunteerId");

                Intent toIng = new Intent(Call1Activity.this, IngActivity.class);
                toIng.putExtra("volunteerId", volunteerId);
                toIng.putExtra("phonenum", phone_num);
                startActivity(toIng);
                finish();


            }

        } catch (JSONException e) {
            Log.d("fadsfsads", "showResult : ", e);
        }


    }

    private class getWait extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals("")){
                getVolunteer = new getVolunteer();
                getVolunteer.execute("http://210.89.191.125/helpee/volunteers/wait/");
            }
            else {
                Toast.makeText(getApplicationContext(),"최근 받으신 봉사활동에 대한 평가를 먼저 진행해주세요!", Toast.LENGTH_SHORT).show();
                Intent toFeedback = new Intent(Call1Activity.this, FeedbackActivity.class);
                toFeedback.putExtra("volunteerId", Integer.valueOf(result));
                startActivity(toFeedback);
                finish();
            }
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0]+phone_num;

            Log.d("dfasfsafsa",serverURL);
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("fadsfsads", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d("fadsfsads", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("phony", "Permission Granted");
                    //Proceed to next steps

                } else {
                    Log.e("phony", "Permission Denied");
                }
                return;
            }
            case MY_PERMISSIONS_RECORD_AUDIO: {
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private class checkDevice extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result == null){
                Intent toError = new Intent(Call1Activity.this, ErrorActivity.class);
                startActivity(toError);
                finish();
            }

            else if (result.equals("true")) {
                putKey = new putKey();
                putKey.execute("http://210.89.191.125/helpee/token/update");
            } else {
                postKey = new postKey();
                postKey.execute("http://210.89.191.125/helpee/device/save");
            }

        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0] + "/" + idByTelephonyManager;
            Log.d("ASD",serverURL);
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {
                errorString = e.toString();

                return null;
            }

        }
    }

    class postKey extends AsyncTask<String, Void, String> {
        RequestHandler rh = new RequestHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            checkUser = new checkUser();
            checkUser.execute("http://210.89.191.125/helpee/user");
        }

        @Override
        protected String doInBackground(String... params) {
            String UPLOAD_URL = params[0];
            HashMap<String, String> data = new HashMap<>();
            String token = FirebaseInstanceId.getInstance().getToken();
            data.put("token", token);
            data.put("deviceKey", idByTelephonyManager);

            String result = rh.sendPostRequest(UPLOAD_URL, data);

            return result;
        }
    }

    class putKey extends AsyncTask<String, Void, String> {
        RequestHandler rh = new RequestHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            checkUser = new checkUser();
            checkUser.execute("http://210.89.191.125/helpee/user");
        }

        @Override
        protected String doInBackground(String... params) {
            String UPLOAD_URL = params[0];
            HashMap<String, String> data = new HashMap<>();
            String token = FirebaseInstanceId.getInstance().getToken();
            data.put("token", token);
            data.put("deviceKey", idByTelephonyManager);

            String result = rh.sendPutRequest(UPLOAD_URL, data);

            return result;
        }
    }

    private class checkUser extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result == null){
                Intent toError = new Intent(Call1Activity.this, ErrorActivity.class);
                startActivity(toError);
                finish();
            }

            else if (result.equals("true")) {
                if(flag_speech == 0){
                    Toast.makeText(Call1Activity.this,"먼저 마이크 버튼을 누르시고 도움받으실 내용을 말씀해주세요.",Toast.LENGTH_LONG).show();
                }
                else{
                    Intent toCall2 = new Intent(Call1Activity.this, Call2Activity.class);

                    toCall2.putExtra("phonenum", phone_num);
                    toCall2.putExtra("date", dest_date);

                    startActivity(toCall2);
                    finish();
                }
            } else {
                Log.d("ASD",result);
                Intent toSplash = new Intent(Call1Activity.this, SplashActivity.class);
                toSplash.putExtra("from", "first");
                toSplash.putExtra("phonenum", phone_num);
                toSplash.putExtra("deviceKey",idByTelephonyManager);

                startActivity(toSplash);
                finish();
            }

        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0] + "/" + phone_num;
            Log.d("ASD",serverURL);
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {
                errorString = e.toString();

                return null;
            }

        }
    }

}
