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

import com.example.timetablemanager.databinding.ActivityEditTaskBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class EditTaskActivity extends AppCompatActivity {

    private ActivityEditTaskBinding binding;

    private String taskId;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> daysTitleArrayList, daysIdArrayList;

    private static final String TAG = "TASK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //book id get from intent started from AdapterPdfAdmin
        taskId = getIntent().getStringExtra("taskId");

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadDays();
        loadTaskInfo();

        //handle click, pick category
        binding.editTaskDayEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayPickDialog();
            }
        });

        //handle click, pick time
        binding.editTaskSelectTimeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickDialog();
            }
        });

        //handle click, go to previous screen
        binding.editTaskBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, begin upload
        binding.editTaskSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }


    private String title = "",description = "";
    private void validateData() {
        Log.d(TAG, "ValidateDate: validating data...");

        //get data
        title = binding.editTaskTitleEt.getText().toString().trim();
        description = binding.editTaskDescriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)) {
            showError(binding.editTaskTitleEt,"Enter title!");
        }
        else if (TextUtils.isEmpty(description)) {
            showError(binding.editTaskDescriptionEt,"at least 8 characters long!");
        }
        else if (TextUtils.isEmpty(selectedDayTitle)) {
            showError((EditText) binding.editTaskDayEt,"Pick day!");
        }
        else if (TextUtils.isEmpty(selectedTime)) {
            showError((EditText) binding.editTaskSelectTimeEt,"Pick time!");

        }
        else {
            //all data is valid, can upload now
            updateTaskToFirebase();
            startActivity(new Intent(EditTaskActivity.this, MainActivity.class));
        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus(); // will help us to show the error
    }

    private void updateTaskToFirebase() {
        Log.d(TAG, "updateTaskToFirebase: Start updating task info to firebase db...");

        //show progress
        progressDialog.setMessage("Updating task info...");
        progressDialog.show();

        //setup info to add in firebase db
        HashMap<String,Object> taskMap = new HashMap<>();
        taskMap.put("title",""+title);
        taskMap.put("description",""+description);
        taskMap.put("dayId",""+selectedDayId);
        taskMap.put("time",""+selectedTime);

        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tasks");
        ref.child(taskId)
                .updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Book updated...");
                        progressDialog.dismiss();
                        Toast.makeText(EditTaskActivity.this, "Task info updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(EditTaskActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        startActivity(new Intent(EditTaskActivity.this,MainActivity.class));
    }

    private int tHour,tMinute;
    private String selectedTime = "";
    private void timePickDialog() {
        //Initialize time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                EditTaskActivity.this,
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
                        binding.editTaskSelectTimeEt.setText(selectedTime);
                    }
                },12,0,false);
        //Displayed previous selected dialog
        timePickerDialog.updateTime(tHour,tMinute);
        //show dialog
        timePickerDialog.show();
    }

    private String selectedDayId="", selectedDayTitle="";
    private void loadDays() {
        Log.d(TAG, "LoadDays: Loading Days of task..");

        daysTitleArrayList = new ArrayList<>();
        daysIdArrayList = new ArrayList<>();

        //db ref for loading categories ... db>Categories
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("Days");
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                daysTitleArrayList.clear(); //clear before adding data
                daysIdArrayList.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {

                    //get id and title of day
                    String dayId = ""+ds.child("id").getValue();
                    String dayTitle = ""+ds.child("title").getValue();

                    //add to respective array lists
                    daysIdArrayList.add(dayId);
                    daysTitleArrayList.add(dayTitle);

                    Log.d(TAG, "onDataChange: dayId: "+ dayId);
                    Log.d(TAG, "onDataChange: dayTitle: "+dayTitle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dayPickDialog() {
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
                        binding.editTaskDayEt.setText(selectedDayTitle);

                        Log.d(TAG, "onClick: selected category Id ("+selectedDayId+") & selected category title ("+selectedDayTitle+")");
                    }
                })
                .show();
    }

    private void loadTaskInfo() {
        Log.d(TAG, "loadTaskInfo: loading task info...");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Tasks");
        refBooks.child(taskId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get task info
                        selectedDayId = ""+snapshot.child("dayId").getValue();
                        description = ""+snapshot.child("description").getValue();
                        title = ""+snapshot.child("title").getValue();
                        selectedTime = ""+snapshot.child("time").getValue();

                        //set task info
                        binding.editTaskTitleEt.setText(title);
                        binding.editTaskDescriptionEt.setText(description);
                        binding.editTaskSelectTimeEt.setText(selectedTime);

                        Log.d(TAG, "onDataChange: loading book category info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Days");
                        refBookCategory.child(selectedDayId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get day
                                        selectedDayTitle = ""+snapshot.child("title").getValue();

                                        //set to category tv
                                        binding.editTaskDayEt.setText(selectedDayTitle);
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