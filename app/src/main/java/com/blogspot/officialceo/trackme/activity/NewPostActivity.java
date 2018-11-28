package com.blogspot.officialceo.trackme.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    @BindView(R.id.new_post_image)
    ImageView newPostImage;

    @BindView(R.id.new_post_text)
    EditText newPostText;

    @BindView(R.id.new_post_upload_button)
    Button newPostUploadButton;

    @BindView(R.id.new_post_upload_progress)
    ProgressBar newPostUploadProgress;

    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    private static final int GALLERY = 1;
    private FirebaseAuth firebaseAuth;
    private boolean isChanged = false;
    private Uri postImageUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private Intent intent;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newPostImage.setImageBitmap(null);
                if (Image != null)
                    Image.recycle();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);

            }
        });

        newPostUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String postDescription = newPostText.getText().toString();

                if (!TextUtils.isEmpty(postDescription) && postImageUri != null){

                    newPostUploadProgress.setVisibility(View.VISIBLE);

                    final String randomText = UUID.randomUUID().toString();

                    StorageReference filePath = storageReference.child("post_images").child(randomText + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();

                            if (task.isSuccessful()){

                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200)
                                            .setMaxWidth(200)
                                            .setQuality(10)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                //compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                        .child(randomText + ".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadThumbUri = taskSnapshot.getStorage().getDownloadUrl().toString();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", downloadUri);
                                        postMap.put("image_thumb", downloadThumbUri);
                                        postMap.put("desc", postDescription);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("time_stamp", FieldValue.serverTimestamp());


                                        firebaseFirestore.collection("posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                                    intent = new Intent(NewPostActivity.this, Main2Activity.class);
                                                    startActivity(intent);
                                                    finish();

                                                }else{

                                                    String errorMessage = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this, "Post Image upload Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                                                }

                                                newPostUploadProgress.setVisibility(View.GONE);

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(NewPostActivity.this, "Failure Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                                    }
                                });



                            }else{

                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Post Image upload Error 1 : " + errorMessage, Toast.LENGTH_SHORT).show();
                                newPostUploadProgress.setVisibility(View.GONE);

                            }

                        }
                    });

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY && resultCode != 0) {
            assert data != null;
            postImageUri = data.getData();
            try {
                Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postImageUri);
                if (getOrientation(getApplicationContext(), postImageUri) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getApplicationContext(), postImageUri));
                    if (rotateImage != null)
                        rotateImage.recycle();
                    rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
                    newPostImage.setImageBitmap(rotateImage);
                } else
                    newPostImage.setImageBitmap(Image);
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
