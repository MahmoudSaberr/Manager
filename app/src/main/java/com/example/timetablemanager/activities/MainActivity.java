package com.example.timetablemanager.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.timetablemanager.databinding.ActivityMainBinding;
import com.example.timetablemanager.fragments.TaskUserFragment;
import com.example.timetablemanager.models.ModelDay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseAuth auth;

    //to show tabs
    private ArrayList<ModelDay> daysArrayList ;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase
        auth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.mainViewPager);
        binding.mainTabLayout.setupWithViewPager(binding.mainViewPager);

        binding.mainLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        binding.mainAddTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);

        daysArrayList = new ArrayList<>();

        //load days from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Days");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear before adding to list
                daysArrayList.clear();

                /*Load Categories - static e.g. All*/
                //add data to models
                ModelDay modelAll = new ModelDay(8,1,"All");
                //add models to list
                daysArrayList.add(modelAll);
                //add data to view pager adapter
                viewPagerAdapter.addFragment(TaskUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getTitle()
                ),modelAll.getTitle());

                //refresh list
                viewPagerAdapter.notifyDataSetChanged();

                //Now load from firebase
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelDay model = ds.getValue(ModelDay.class);
                    //add data to list
                    daysArrayList.add(model);
                    viewPagerAdapter.addFragment(TaskUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getTitle()
                    ),model.getTitle());
                    //Refresh List
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<TaskUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(TaskUserFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            //not logged in
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        else {
            //logged in

            }

    }


}