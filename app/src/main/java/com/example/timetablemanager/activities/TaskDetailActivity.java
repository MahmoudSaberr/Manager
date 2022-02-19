package com.example.timetablemanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.timetablemanager.R;
import com.example.timetablemanager.databinding.ActivityTaskDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private String taskId;
    private static final String TAG = "TASK_Details_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //book id get from intent started from AdapterPdfAdmin
        taskId = getIntent().getStringExtra("taskId");

        loadTaskInfo();

        binding.taskBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void loadTaskInfo() {
        Log.d(TAG, "loadTaskInfo: loading task info...");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Tasks");
        refBooks.child(taskId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get task info
                        String dayId = ""+snapshot.child("dayId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        String time = ""+snapshot.child("time").getValue();

                        //set book info
                        binding.taskTitleTv.setText(title);
                        binding.taskDescriptionTv.setText(description);
                        binding.taskTimeTv.setText(time);

                        Log.d(TAG, "onDataChange: loading book category info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Days");
                        refBookCategory.child(dayId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String day = ""+snapshot.child("title").getValue();
                                        binding.taskDayTv.setText(day);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}