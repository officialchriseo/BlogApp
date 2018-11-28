package com.blogspot.officialceo.trackme.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.officialceo.trackme.POJO.BlogPostItems;
import com.blogspot.officialceo.trackme.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class BlogPostRecyclerViewAdapter extends RecyclerView.Adapter<BlogPostRecyclerViewAdapter.ViewHolder> {

    public List<BlogPostItems> blog_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogPostRecyclerViewAdapter(List<BlogPostItems> blog_list){

        this.blog_list = blog_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_post_items, viewGroup, false);
        context = viewGroup.getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        String blog_desc = blog_list.get(i).getDesc();
        viewHolder.setDescText(blog_desc);

        String image_url = blog_list.get(i).getImage_uri();
        String thumbUri = blog_list.get(i).getImage_thumb();
        viewHolder.setBlogPostImage(image_url, thumbUri);

        String user_id = blog_list.get(i).getUser_id();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    viewHolder.setUserData(userName, userImage);

                }else{

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(context, "POST IMAGE UPLOAD ERROR : " + errorMessage, Toast.LENGTH_SHORT).show();

                }

            }
        });

        try {
            long millisecond = blog_list.get(i).getTimeStamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            viewHolder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View view;

        private TextView postDescTextView, postUserName, blogDate;
        private ImageView blogPostImage, blogUserImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDescText(String descText){

            postDescTextView = view.findViewById(R.id.post_details);
            postDescTextView.setText(descText);

        }

        public void setBlogPostImage(String image_url, String downloadUri){

            blogPostImage = view.findViewById(R.id.post_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.defaultp);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(image_url)
            ).into(blogPostImage);

            Glide.with(context).load(downloadUri).into(blogPostImage);

        }

        @SuppressLint("CheckResult")
        public void setUserData(String name, String image){

            blogUserImage = view.findViewById(R.id.post_profile_image);
            postUserName = view.findViewById(R.id.post_profile_name);

            postUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.pp);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }

        public void setTime(String date) {

            blogDate = view.findViewById(R.id.post_date);
            blogDate.setText(date);

        }

    }


}
