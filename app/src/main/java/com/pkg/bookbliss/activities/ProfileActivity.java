package com.pkg.bookbliss.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.pkg.bookbliss.MyApplication;
import com.pkg.bookbliss.R;
import com.pkg.bookbliss.adapter.AdapterPdfFavorite;
import com.pkg.bookbliss.databinding.ActivityProfileBinding;
import com.pkg.bookbliss.models.ModelPdf;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    //view binding
    private ActivityProfileBinding binding;

    //firebase auth, for loading user data using user uid
    private FirebaseAuth firebaseAuth;

    //firebase current user
//    private FirebaseUser firebaseUser;

    //arrayList to hold the books
    private ArrayList<ModelPdf> pdfArrayList;

    //adapter to set in recyclerview
    private AdapterPdfFavorite adapterPdfFavorite;

    //progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //reset data of user info
        binding.accountTypeTv.setText("N/A");
        binding.memberDateTv.setText("N/A");
        binding.favoriteBooksCountTv.setText("N/A");
//        binding.accountStatusTv.setText("N/A");

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadFavoriteBooks();

        //get current user
//        firebaseUser = firebaseAuth.getCurrentUser();

        //init/setup progress dialog
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Please Wait");
//        progressDialog.setCanceledOnTouchOutside(false);

        //handle click , start profile edit page
        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, verify user if not
//        binding.accountStatusTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (firebaseUser.isEmailVerified()) {
//                    //already verified
//                    Toast.makeText(ProfileActivity.this, "Already verified...", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    //not verified, show confirmation dialog first
//                    emailVerificationDialog();
//                }
//            }
//        });

    }

//    private void emailVerificationDialog() {
//        //Alert dialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Verify Email")
//                .setMessage("Are you sure you want to sent email verification instruction to your email "+firebaseUser.getEmail())
//                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                       sendEmailVerification();
//                    }
//                })
//                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .show();
//    }
//
//    private void sendEmailVerification() {
//        //show progress
//        progressDialog.setMessage("Sending email verification instruction to your email "+firebaseUser.getEmail());
//        progressDialog.show();
//
//        firebaseUser.sendEmailVerification()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        //successfully sent
//                        progressDialog.dismiss();
//                        Toast.makeText(ProfileActivity.this, "Instructions sent, check your email "+firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //failed to sent
//                        progressDialog.dismiss();
//                        Toast.makeText(ProfileActivity.this, "Failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

        private void loadUserInfo() {
            Log.d(TAG, "loadUserInfo: Loading user info of user "+firebaseAuth.getUid());

            //get email verification
//        if (firebaseUser.isEmailVerified()) {
//            binding.accountStatusTv.setText("Verified");
//        } else {
//            binding.accountStatusTv.setText("Not Verified");
//        }

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get all info of user here from snapshot
                            String email = ""+snapshot.child("email").getValue();
                            String name = ""+snapshot.child("name").getValue();
                            String profileImage = ""+snapshot.child("profileImage").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();
                            String uid = ""+snapshot.child("uid").getValue();
                            String userType = ""+snapshot.child("userType").getValue();

                            //format data to dd/ww/yyyy
                            String formattedData = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                            //set data to ui
                            binding.emailTv.setText(email);
                            binding.nameTv.setText(name);
                            binding.memberDateTv.setText(formattedData);
                            binding.accountTypeTv.setText(userType);

                            //set image, using glide
                            Glide.with(ProfileActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.profileIv);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }

    private void loadFavoriteBooks() {
        //init list
        pdfArrayList = new ArrayList<>();

        //load favorite books from database
        //Users > bookId > Favorites
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            //only get the bookId here, and got other details in adapter using that bookId
                            String bookId = ""+ds.child("bookId").getValue();

                            //set id to model
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            //add model to list
                            pdfArrayList.add(modelPdf);
                        }

                        //set number of favorite books
                        binding.favoriteBooksCountTv.setText(""+pdfArrayList.size());
                        //setup adapter
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);
                        //set adapter to recyclerview
                        binding.bookRv.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}

