package com.example.junmp.togetherhelpee.activity.volunteer.recommend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.example.junmp.togetherhelpee.R;
import com.example.junmp.togetherhelpee.domain.user.UserService;
import com.example.junmp.togetherhelpee.domain.volunteer.VolunteerService;

public class RecommendActivity extends AppCompatActivity {

    private UserService userService = new UserService();
    private VolunteerService volunteerService = new VolunteerService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_recommend);

        Button btnYes = findViewById(R.id.btn_yes);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*volunteerService.getRecommendHelper(new VolunteerForm());*/

            }
        });

        Button btnNo = findViewById(R.id.btn_no);

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*volunteerService.getRecommendHelper(new VolunteerForm());*/

            }
        });
    }
}