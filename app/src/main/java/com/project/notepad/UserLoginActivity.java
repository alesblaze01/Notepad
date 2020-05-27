package com.project.notepad;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.project.notepad.Utility.UserAccount;

public class UserLoginActivity extends AppCompatActivity {

    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 1;
    private static final String TAG = "UserLoginActivity";

    private GoogleSignInClient mSignInClient;
    public UserAccount mUserAccount;
    private View mSignInView;
    private View mSignedInView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserAccount = UserAccount.getInstance(this);
        mSignInView = getLayoutInflater().inflate(R.layout.activity_user_login,null);
        mSignedInView = getLayoutInflater().inflate(R.layout.user_logged_in,null);
        Button signOutButton = mSignedInView.findViewById(R.id.sign_out_button);
        SignInButton signInButton = mSignInView.findViewById(R.id.sign_in_button);

        setupGoogleSignIn();


        if(isLoggedIn()) {
            setContentView(mSignedInView);
            updateUI();
        }else {
            setContentView(mSignInView);
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignInFlow();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOutUser();
            }
        });
    }

    private void SignOutUser() {
        mSignInClient.signOut();
        mUserAccount.setAccount(null);
        setContentView(mSignInView);
        Toast.makeText(UserLoginActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder()
                .requestEmail()
                .requestProfile()
                .build();
        mSignInClient = GoogleSignIn.getClient(this,signInOptions);
    }

    private void updateUI(){
        setContentView(mSignedInView);
        /*TODO: display user image*/
        ImageView imageView = findViewById(R.id.user_image_display);
        TextView userName = findViewById(R.id.user_name);
        TextView userEmail = findViewById(R.id.user_email);

        final Uri photoUrl = mUserAccount.getAccount().getPhotoUrl();
        if (photoUrl != null) {

        }

        userName.setText(mUserAccount.getAccount().getDisplayName());
        userEmail.setText(mUserAccount.getAccount().getEmail());
    }

    public boolean isLoggedIn() {
        mUserAccount.setAccount(GoogleSignIn.getLastSignedInAccount(this));
        return mUserAccount.isSignedIn();
    }

    private void startSignInFlow() {
        Intent signInIntent = mSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(signInTask);
        }
    }
    
    private void handleSignInResult(Task<GoogleSignInAccount> signInTask) {
        try {
            mUserAccount.setAccount(signInTask.getResult(ApiException.class));
            if(mUserAccount.isSignedIn()) {
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        }catch (ApiException e) {
            e.printStackTrace();
            Log.d(TAG, "handleSignInResult: SignIn Failed");
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent getIntent(Context context){
        Intent signInIntent = new Intent(context,UserLoginActivity.class);
        return  signInIntent;
    }
}
