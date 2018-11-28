package com.blogspot.officialceo.trackme.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blogspot.officialceo.trackme.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    @BindView(R.id.edit_profile_image)
    CircleImageView editProfileImage;

    @BindView(R.id.profile_name_edttxt)
    EditText userProfileName;

    @BindView(R.id.bio_edttxt)
    EditText userBio;

    @BindView(R.id.save_profile_button)
    Button saveProfileButton;

    @BindView(R.id.userImageProgress)
    ProgressBar imageUploadProgress;

    private Uri mImageUri = null;

    public static final int PICK_IMAGE = 1;
    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    private static final int GALLERY = 1;

    private Intent intent;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private boolean isChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = FirebaseAuth.getInstance().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        imageUploadProgress.setVisibility(View.VISIBLE);
        saveProfileButton.setEnabled(false);

        //getting user details from firebasefirestore
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String bio = task.getResult().getString("bio");
                        String image = task.getResult().getString("image");

                        mImageUri = Uri.parse(image);

                        userProfileName.setText(name);
                        userBio.setText(bio);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.defaultp);
                        Glide.with(EditProfile.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(editProfileImage);
                        //Glide.with(EditProfile.this).load(image).into(editProfileImage);

                    }


                }else{

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(EditProfile.this, "FireStore Retrieval Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                }

                imageUploadProgress.setVisibility(View.INVISIBLE);
                saveProfileButton.setEnabled(true);


            }
        });

        editProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(EditProfile.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(EditProfile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    }else

                    selectImage();

                }else
                    selectImage();

            }
        });


        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String profile_name = userProfileName.getText().toString();
                final String profile_bio = userBio.getText().toString();

                if (isChanged) {

                    if (!TextUtils.isEmpty(profile_name) && !TextUtils.isEmpty(profile_bio) && mImageUri != null) {

                        user_id = firebaseAuth.getCurrentUser().getUid();

                        imageUploadProgress.setVisibility(View.VISIBLE);

                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeFirestore(task, profile_name, profile_bio);

                                    Toast.makeText(EditProfile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                                } else {

                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(EditProfile.this, "Image Upload Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                                    imageUploadProgress.setVisibility(View.INVISIBLE);
                                }

                                imageUploadProgress.setVisibility(View.INVISIBLE);

                            }
                        });

                    }

                }else{

                    storeFirestore(null, profile_name, profile_bio);

                }

            }
        });

    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String profile_name, String profile_bio) {


        Task<Uri> download_uri = task.getResult().getStorage().getDownloadUrl();


        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", profile_name);
        userMap.put("bio", profile_bio);
        userMap.put("image", download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    intent = new Intent(EditProfile.this, Main2Activity.class);
                    startActivity(intent);
                    finish();

                }else{

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(EditProfile.this, "FireStore Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                }

                imageUploadProgress.setVisibility(View.INVISIBLE);

            }
        });
    }

    //selecting an image from the gallery
    private void selectImage() {
        editProfileImage.setImageBitmap(null);
        if (Image != null)
            Image.recycle();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY && resultCode != 0) {
            mImageUri = data.getData();
            try {
                Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                if (getOrientation(getApplicationContext(), mImageUri) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getApplicationContext(), mImageUri));
                    if (rotateImage != null)
                        rotateImage.recycle();
                    rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
                    editProfileImage.setImageBitmap(rotateImage);
                } else
                    editProfileImage.setImageBitmap(Image);
                isChanged = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }


}
