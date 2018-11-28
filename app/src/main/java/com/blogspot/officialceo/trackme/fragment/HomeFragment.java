package com.blogspot.officialceo.trackme.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.officialceo.trackme.POJO.BlogPostItems;
import com.blogspot.officialceo.trackme.R;
import com.blogspot.officialceo.trackme.adapter.BlogPostRecyclerViewAdapter;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    RecyclerView blog_list_view;

    private List<BlogPostItems> blog_list;

    private FirebaseFirestore firebaseFirestore;
    private BlogPostRecyclerViewAdapter blogPostRecyclerViewAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        blog_list = new ArrayList<>();

        blogPostRecyclerViewAdapter = new BlogPostRecyclerViewAdapter(blog_list);

        blog_list_view = view.findViewById(R.id.blog_post_recyclerview);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogPostRecyclerViewAdapter);


        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){

                    if (doc.getType() == DocumentChange.Type.ADDED){

                        BlogPostItems blogPostItems = doc.getDocument().toObject(BlogPostItems.class);
                        blog_list.add(blogPostItems);

                        blogPostRecyclerViewAdapter.notifyDataSetChanged();

                    }

                }

            }
        });

        return view;
    }

}
