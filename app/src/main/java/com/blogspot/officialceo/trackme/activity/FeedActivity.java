package com.blogspot.officialceo.trackme.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.blogspot.officialceo.trackme.R;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

    }
}
