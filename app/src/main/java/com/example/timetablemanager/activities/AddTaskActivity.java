package com.example.timetablemanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.timetablemanager.databinding.ActivityAddTaskBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AddTaskActivity extends AppCompatActivity {

    //view binding
    private ActivityAddTaskBinding binding;

    //array to hold pdf categories
    private ArrayList<String> daysTitleArrayList,daysIdArrayList;

    //firebase
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    //TAg for debugging
    private static final String TAG = "ADD_TASK_TAG";

    private int tHour,tMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase service
        auth = FirebaseAuth.getInstance();
        LoadDaysOfTasks();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.addTaskBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, pick Day
        binding.addTaskDayEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               DaysPickDialog();
            }
        });

        //handle click, pick time
        binding.addTaskSelectTimeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               timePickDialog();
            }
        });

        //handle click, upload pdf
        binding.addTaskSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateDate();
            }
        });
    }

    private void LoadDaysOfTasks() {
        Log.d(TAG, "LoadDaysOfTasks: Loading Days...");

        daysTitleArrayList = new ArrayList<>();
        daysIdArrayList = new ArrayList<>();

        //db ref for loading categories ... db>Days
        DatabaseReference daysRef = FirebaseDatabase.getInstance().getReference("Days");
        daysRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                daysTitleArrayList.clear(); //clear before adding data
                daysIdArrayList.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get id and title of category
                    String dayId = ""+ds.child("id").getValue();
                    String dayTitle = ""+ds.child("title").getValue();

                    //add to respective array lists
                    daysIdArrayList.add(dayId);
                    daysTitleArrayList.add(dayTitle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //Select day id and title
    private String selectedDayId, selectedDayTitle;
    private void DaysPickDialog() {
        Log.d(TAG, "DaysPickDialog: showing days pick dialog");

        //get String array of days from arrayList
        String[] daysArray = new String[daysTitleArrayList.size()];
        for (int i = 0; i< daysTitleArrayList.size(); i++) {
            daysArray[i] = daysTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Day")
                .setItems(daysArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle item click
                        //get click item from list
                        selectedDayTitle = daysTitleArrayList.get(i);
                        selectedDayId = daysIdArrayList.get(i);
                        //set to day text view
                        binding.addTaskDayEt.setText(selectedDayTitle);

                        Log.d(TAG, "onClick: selected category Id ("+selectedDayId+") & selected category title ("+selectedDayTitle+")");
                    }
                })
                .show();
    }

    private String selectedTime ="";
    private void timePickDialog() {
        //Initialize time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                AddTaskActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        //initialize hour and time
                        tHour = hourOfDay;
                        tMinute = minute;
                        //initialize calender
                        Calendar calendar = Calendar.getInstance();
                        //set hour and minute
                        calendar.set(0,0,0,tHour,tMinute);
                        //set selected time on text view
                        selectedTime = (String) DateFormat.format("hh:mm aa",calendar);
                        binding.addTaskSelectTimeEt.setText(selectedTime);
                    }
                },12,0,false);
        //Displayed previous selected dialog
        timePickerDialog.updateTime(tHour,tMinute);
        //show dialog
        timePickerDialog.show();
    }

    private String title = "",description = "";
    private void ValidateDate() {
        Log.d(TAG, "ValidateDate: validating data...");

        //get data
        title = binding.addTaskTitleEt.getText().toString().trim();
        description = binding.addTaskDescriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)) {
            showError(binding.addTaskTitleEt,"Enter title!");
        }
        else if (TextUtils.isEmpty(description)) {
            showError(binding.addTaskDescriptionEt,"at least 8 characters long!");
        }
        else if (TextUtils.isEmpty(selectedDayTitle)) {
            showError((EditText) binding.addTaskDayEt,"Pick day!");
        }
        else if (TextUtils.isEmpty(selectedTime)) {
            showError((EditText) binding.addTaskSelectTimeEt,"Pick time!");
        }
        else {
            //all data is valid, can upload now
            UploadTaskToFirebase();
            startActivity(new Intent(AddTaskActivity.this, MainActivity.class));
        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus(); // will help us to show the error
    }

    private void UploadTaskToFirebase() {
        //show progress dialog
        progressDialog.setMessage("Adding Task...");
        progressDialog.show();

        //get Timestamp
        String timestamp = ""+System.currentTimeMillis();

        //setup info to add in firebase db
        HashMap<String,Object> taskMap = new HashMap<>();
        taskMap.put("id",""+timestamp);
        taskMap.put("title",""+title);
        taskMap.put("description",""+description);
        taskMap.put("dayId",""+selectedDayId);
        taskMap.put("time",""+selectedTime);
        taskMap.put("timestamp",""+timestamp);
        taskMap.put("uid",""+auth.getUid());

        //db reference: db > Tasks
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tasks");
        ref.child(""+timestamp)
                .setValue(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded...");
                        Toast.makeText(AddTaskActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to "+e.getMessage());
                        Toast.makeText(AddTaskActivity.this, "Failed to upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}