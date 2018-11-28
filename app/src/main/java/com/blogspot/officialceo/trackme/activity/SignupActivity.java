package com.blogspot.officialceo.trackme.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.officialceo.trackme.MainActivity;
import com.blogspot.officialceo.trackme.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends Activity {

    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_name)
    EditText _nameText;

    @BindView(R.id.input_address)
    EditText _addressText;

    @BindView(R.id.input_email)
    EditText _emailText;

    @BindView(R.id.input_mobile)
    EditText _mobileText;

    @BindView(R.id.input_password)
    EditText _passwordText;

    @BindView(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;

    @BindView(R.id.btn_signup)
    Button _signupButton;

    @BindView(R.id.link_login)
    TextView _loginLink;

    @BindView(R.id.fb_login_button)
    LoginButton fbButton;

    CallbackManager mCallbackManager;

    private FirebaseAuth mAuth;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });


        // Initializing Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        fbButton.setReadPermissions("email", "public_profile");
        fbButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                // handleFacebookAccessToken(loginResult.getAccessToken());

                String userId = loginResult.getAccessToken().getUserId();
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        displayUserInfo(object);

                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name , last_name , email , id");
                graphRequest.setParameters(parameters);

                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


    }

    @SuppressLint("SetTextI18n")
    public void displayUserInfo(JSONObject object){

        String first_name = "", last_name = "", email = "", id = "";


        try {
            first_name = object.getString("first_name");
            last_name = object.getString("last_name");
            email = object.getString("email");
            id = object.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        _nameText.setText(first_name + " " + last_name);
        _emailText.setText(email);
        _addressText.setText(id);
        _passwordText.setVisibility(View.GONE);
        _reEnterPasswordText.setVisibility(View.GONE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        final String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        // signup logic here.

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    onSignupSuccess();
                    progressDialog.dismiss();

                    Intent intent = new Intent(SignupActivity.this, Main2Activity.class);
                    intent.putExtra("userName", name);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }else
                onSignupFailed();
                progressDialog.dismiss();
            }
        });


    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        showToast("Signup Successful...");

        finish();
    }

    public void onSignupFailed() {

        showToast("Signup Failed...");

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (address.isEmpty()) {
            _addressText.setError("Enter Valid Address");
            valid = false;
        } else {
            _addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=11) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    public void showToast(String message) {

        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}