package com.blogspot.officialceo.trackme.activity;

import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blogspot.officialceo.trackme.BottomNavigationBehavior;
import com.blogspot.officialceo.trackme.R;
import com.blogspot.officialceo.trackme.animation.GuillotineAnimation;
import com.blogspot.officialceo.trackme.fragment.AccountFragment;
import com.blogspot.officialceo.trackme.fragment.HomeFragment;
import com.blogspot.officialceo.trackme.fragment.NotificationFragment;
import com.facebook.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.ButterKnife;
import butterknife.BindView;


public class Main2Activity extends AppCompatActivity {
    private static final long RIPPLE_DURATION = 250;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.root)
    FrameLayout root;

    @BindView(R.id.content_hamburger)
    View contentHamburger;

    @BindView(R.id.add_post_button)
    FloatingActionButton addPostButton;

    @BindView(R.id.main_button_nav_bar)
    BottomNavigationView mainButtomNavView;


    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private FirebaseAuth firebaseAuth;
    Intent intent;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null){

            gotoLogin();

        }else{

            current_user_id = firebaseAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){

                        if (!task.getResult().exists()){

                            Toast.makeText(Main2Activity.this, "Please setup your profile", Toast.LENGTH_LONG).show();
                            intent = new Intent(Main2Activity.this, EditProfile.class);
                            startActivity(intent);
                            finish();

                        }

                    }else{

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(Main2Activity.this, errorMessage, Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }

    }

    public BottomNavigationView.OnNavigationItemSelectedListener bottomNavigation =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment fragment;

                    switch (menuItem.getItemId()) {

                        case R.id.bottom_action_home:
                            fragment = new HomeFragment();
                            replaceFragment(fragment);
                            return true;

                        case R.id.bottom_action_notification:
                            fragment = new NotificationFragment();
                            replaceFragment(fragment);
                            return true;

                        case R.id.bottom_action__account:
                            fragment = new AccountFragment();
                            replaceFragment(fragment);
                            return true;

                        default:
                            return false;

                    }

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        ButterKnife.bind(this);

       firebaseAuth = FirebaseAuth.getInstance();
       firebaseFirestore = FirebaseFirestore.getInstance();

       replaceFragment(new HomeFragment());

        mainButtomNavView.setOnNavigationItemSelectedListener(bottomNavigation);

       addPostButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               intent = new Intent(Main2Activity.this, NewPostActivity.class);
               startActivity(intent);
           }
       });

        if (firebaseAuth.getCurrentUser() == null){
            finish();
            intent = new Intent(Main2Activity.this, LoginActivity.class);
            startActivity(intent);

        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
        }

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        root.addView(guillotineMenu);

        LinearLayout logout_butt = findViewById(R.id.logout_group);

        logout_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(Main2Activity.this, LoginActivity.class));
            }
        });

        LinearLayout profile_layout = findViewById(R.id.profile_group);
        profile_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Main2Activity.this, EditProfile.class);
                startActivity(intent);
            }
        });

        new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();

    }

    private void logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(Main2Activity.this, LoginActivity.class));
    }

    private void gotoLogin(){
        intent = new Intent(Main2Activity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
}
