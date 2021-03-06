package com.example.chatappdemo.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.chatappdemo.R;
import com.example.chatappdemo.adapter.AdapterGroupList;
import com.example.chatappdemo.model.GroupsList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GroupsFragment extends Fragment {

    private ShimmerRecyclerView recyclerViewGroup;
    private static final int NUM_COLUMNS = 2;
    private FirebaseAuth firebaseAuth;
    private ArrayList<GroupsList> groupsLists;
    private AdapterGroupList adapterGroupList;
    SwipeRefreshLayout swipeRefreshLayout;
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);


        recyclerViewGroup = view.findViewById(R.id.recyclerGroup);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadGroupChatsList();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadGroupChatsList();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadGroupChatsList() {
        groupsLists = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupsLists.size();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()) {
                        GroupsList model = ds.getValue(GroupsList.class);
                        groupsLists.add(model);
                    }
                }
                adapterGroupList = new AdapterGroupList(getActivity(), groupsLists);
                StaggeredGridLayoutManager staggeredGridLayoutManager = new
                        StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
                recyclerViewGroup.setLayoutManager(staggeredGridLayoutManager);
                recyclerViewGroup.setAdapter(adapterGroupList);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchGroupChatsList(String query) {
        groupsLists = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupsLists.size();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()) {
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            GroupsList model = ds.getValue(GroupsList.class);
                            groupsLists.add(model);
                        }

                    }
                }
                adapterGroupList = new AdapterGroupList(getActivity(), groupsLists);
                StaggeredGridLayoutManager staggeredGridLayoutManager = new
                        StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
                recyclerViewGroup.setLayoutManager(staggeredGridLayoutManager);
                recyclerViewGroup.setAdapter(adapterGroupList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
