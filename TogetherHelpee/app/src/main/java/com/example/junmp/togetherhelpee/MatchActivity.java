package com.example.junmp.togetherhelpee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MatchActivity extends AppCompatActivity {
    String phone_num;

    TextView txt_type;
    TextView txt_date;
    TextView txt_time;
    TextView txt_duration;
    TextView txt_content;
    TextView txt_match_state;
    TextView txt_match_info;

    Button btn_accept;
    Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        Intent fromCall = getIntent();
        String type = fromCall.getStringExtra("type");
        if(type.equals("outside")){
            type = "외출";
        }
        else if(type.equals("talk")){
            type = "말동무";
        }
        else if(type.equals("housework")){
            type = "가사";
        }
        else if(type.equals("education")){
            type = "교육";
        }
        String helperId = fromCall.getStringExtra("helperId");
        int matchingStatus = fromCall.getIntExtra("matchingStatus", 0);
        String content = fromCall.getStringExtra("content");
        String time = fromCall.getStringExtra("time");
        int duration = fromCall.getIntExtra("duration", 0);
        String date = fromCall.getStringExtra("date");

        phone_num = fromCall.getStringExtra("phonenum");

        txt_type = findViewById(R.id.txt_type);
        txt_date = findViewById(R.id.txt_date);
        txt_time = findViewById(R.id.txt_time);
        txt_duration = findViewById(R.id.txt_duration);
        txt_content = findViewById(R.id.txt_content);
        txt_match_state = findViewById(R.id.txt_match_state);
        txt_match_info = findViewById(R.id.txt_match_info);
        btn_accept = findViewById(R.id.btn_accept);
        btn_cancel = findViewById(R.id.btn_cancel);

        txt_type.setText("요청 종류: "+type);
        txt_date.setText("요청 날짜: "+date);
        txt_time.setText("요청 시각: "+time);
        txt_duration.setText("요청 봉사시간: "+duration);
        txt_content.setText("요청 기타사항: "+content);

        if(matchingStatus == 1){
            txt_match_state.setText("매칭 수락 대기중입니다!");
            txt_match_info.setText("지원한 봉사자의 아이디: "+helperId);
            btn_accept.setVisibility(View.VISIBLE);
        }

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
