package com.pkg.bookbliss.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pkg.bookbliss.R;
import com.pkg.bookbliss.databinding.ActivityRegisterBinding;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;

    //firebase dialog
    private FirebaseAuth firebaseAuth;

//    private static final String TAG = "LOGIN_OPTIONS_TAG";

    //progress dialog
    private ProgressDialog progressDialog;

//    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        ///handle loginGoogleBtn click, open LoginEmailActivity to login with Email &Password
//        binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                beginGoogleLogin();
//            }
//        });
    }

    /*----  Get Registration Data (Firebase Database)  ----*/
    private String name = "", email = "", password = ""; //here name, email & password is Foreign Key

    private void validateData() {
        /**/


        //get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.cPasswordEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)) {
            //name edit text is empty, must enter name
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //email is either not entered or email pattern is invalid, don't allow to continue in that case
            Toast.makeText(this, "Enter your email pattern...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            //password edit text is empty, must enter password
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cPassword)) {
            //confirm password edit text is empty, must enter confirm password
            Toast.makeText(this, "Confirm password...", Toast.LENGTH_SHORT).show();
        }
        //password confirm password doesn't match, don't allow to continue in that case, both password must match
        else if (!password.equals(cPassword)) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show();
        } else {
            //all date is validate, begin create account
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progress
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account creation success, now add in firebase realtime database
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //account creation failed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /*----  Saving Registration data to Firebase Database  ----*/
    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //setup current user uid, since user is registered so we can get now
        String uid = firebaseAuth.getUid();

        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();

        /*hashMap - JAVA class to implement the Map interface which allows to store key and value pair.
        Where key should be unique*/
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); //add empty,
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        //setup data to do
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data added to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account created...", Toast.LENGTH_SHORT).show();
                        //since user account is created so start dashboard of user
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data failed adding to Db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


//    private void beginGoogleLogin() {
//        Log.d(TAG, "beginGoogleLogin: ");
//
//        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
//        googleSignInnARL.launch(googleSignInIntent);
//    }

//    private ActivityResultLauncher<Intent> googleSignInnARL = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    Log.d(TAG, "onActivityResult: ");
//
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//
//                        Intent data = result.getData();
//                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//
//                        try {
//                            //Google signIn was successful, authenticate with Firebase
//                            GoogleSignInAccount account = task.getResult(ApiException.class);
//                            Log.d(TAG, "onActivityResult: Account ID: "+account.getId());
//                            firebaseAuthWithGoogleAccount(account.getIdToken());
//                        }
//                        catch (ApiException e) {
//                            //google signIn failed, update UI appropriately
//                            Log.e(TAG, "onActivityResult: ", e);
//                        }
//                    }
//                    else {
//                        //cancelled from google signIn options/confirmation dialog
//                        Log.d(TAG, "onActivityResult: Cancelled");
//                        Toast.makeText(RegisterActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//    );

//    private void firebaseAuthWithGoogleAccount(String idToken) {
//        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: "+idToken);
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//
//        firebaseAuth.signInWithCredential(credential)
//                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                    @Override
//                    public void onSuccess(AuthResult authResult) {
//
//                        if (Objects.requireNonNull(authResult.getAdditionalUserInfo()).isNewUser()) {
//                            Log.d(TAG, "onSuccess: New User, Account created...");
//                            updateUserInfo();
//                        }
//                        else {
//                            Log.d(TAG, "onSuccess: Existing user, Logged In...");
//                            startActivity(new Intent( RegisterActivity.this, DashboardUserActivity.class));
//                            finishAffinity();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "onFailure: ", e);
//                    }
//                });
//    }

//    private void updateUserInfoDb() {
//        Log.d(TAG, "updateUserInfoDb: ");
//
//        progressDialog.setMessage("Saving User Info");
//        progressDialog.show();
//
//        //timestamp
//        long timestamp = System.currentTimeMillis();
//
//        //setup current user uid, since user is registered so we can get now
//        String uid = firebaseAuth.getUid();
//
//        //setup data to add in db
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("uid", uid);
//        hashMap.put("email", email);
//        hashMap.put("name", name);
//        hashMap.put("profileImage", "");//add empty,
//        hashMap.put("userType", "user");
//        hashMap.put("timestamp", timestamp);
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(uid)
//                .setValue(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Log.d(TAG, "onSuccess: User info saved...");
//                        progressDialog.dismiss();
//
//                        startActivity(new Intent( RegisterActivity.this, DashboardUserActivity.class));
//                        finishAffinity();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "onFailure: ", e);
//                        progressDialog.dismiss();
//                        Toast.makeText(RegisterActivity.this, "Failed to save user info due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

}



