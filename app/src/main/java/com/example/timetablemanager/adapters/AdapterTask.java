package com.example.timetablemanager.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetablemanager.R;
import com.example.timetablemanager.activities.EditTaskActivity;
import com.example.timetablemanager.activities.MainActivity;
import com.example.timetablemanager.activities.TaskDetailActivity;
import com.example.timetablemanager.databinding.RowTaskUserBinding;
import com.example.timetablemanager.filters.FilterTask;
import com.example.timetablemanager.models.ModelTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AdapterTask extends RecyclerView.Adapter<AdapterTask.HolderTask> implements Filterable {

    private Context context;
    public ArrayList<ModelTask> taskArrayList, filterList;

    private RowTaskUserBinding binding;

    private static final String TAG ="ADAPTER_TASK_TAG";

   //progress
    private ProgressDialog progressDialog;

    private FilterTask filter;


    public AdapterTask(Context context, ArrayList<ModelTask> taskArrayList) {
        this.context = context;
        this.taskArrayList = taskArrayList;
        this.filterList = taskArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderTask onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding row_pdf_admin.xml
        binding = RowTaskUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderTask(binding.getRoot());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull HolderTask holder, int position) {
        /*Get Data , Set Data, Handle clicks etc.*/

        //get data
        ModelTask model = taskArrayList.get(position);
        String taskId = model.getId();
        String title = model.getTitle();
        //String description = model.getDescription();
        String dayId = model.getDayId();
        String timestamp = model.getTimestamp();
        String time = model.getTime();

        //we need to convert timestamp dd/MM/yyyy format
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
       // String formattedDate = DateFormat.format("dd/MM/yyyy", cal).toString();

        //set data
        holder.titleTv.setText(title);
        holder.timeTv.setText(time);

     /*   //get Day using DayId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Days");
        ref.child(dayId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get day
                        String nameOfDay = "" + snapshot.child("title").getValue();
                        binding.rowDayTv.setText(nameOfDay);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/
/*

        //handle click, show dialog with options 1) Edit , 2) Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model,holder);
            }
        });
*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskDetailActivity.class);
                intent.putExtra("taskId",taskId);
                context.startActivity(intent);
            }
        });

        int colorCode = getRandomColor();
        holder.layout.setCardBackgroundColor(holder.itemView.getResources().getColor(colorCode,null));
    }

/*

    private void moreOptionsDialog(ModelTask model, HolderTask holder) {
        String taskId = model.getId();
        String taskTitle = model.getTitle();

        //options to show in dialog
        String[] options = {"Edit","Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if (which == 0) {
                            //Edit Clicked, open Edit Activity to edit the book info
                            Intent intent = new Intent(context, EditTaskActivity.class);
                            intent.putExtra("taskId",taskId);
                            context.startActivity(intent);

                        }
                        else if (which == 1) {
                            //Delete Clicked
                            String TAG = "DELETE_TASK_TAG";

                            Log.d(TAG, "deleteBook: Deleting.....");
                            ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setTitle("Please Wait");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setMessage("Deleting "+taskTitle+"...");
                            progressDialog.show();

                            Log.d(TAG, "onSuccess: deleting info from db");
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tasks");
                            reference.child(taskId)
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Book Deleted Successfully...");
                                            progressDialog.dismiss();
                                            Toast.makeText(context, taskTitle+" Deleted Successfully...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: Failed to delete from db due to"+e.getMessage());
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                         }

                        }

                })
                .show();
    }
*/

    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
        {
            filter = new FilterTask(filterList,this);
        }

        return filter;
    }

    class HolderTask extends RecyclerView.ViewHolder{
        //UI views for row_pdf_admin.xml
        TextView titleTv, timeTv;
        CardView layout;
        public HolderTask(@NonNull View itemView) {
            super(itemView);

            //init ui views
            layout = binding.rowLayout;
            titleTv = binding.rowTaskTitleTv;
          //  dayTv = binding.rowDayTv;
            timeTv = binding.rowTaskTime;
        }
    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.green);
        colorCode.add(R.color.color1);
        colorCode.add(R.color.color2);
        colorCode.add(R.color.color4);
        colorCode.add(R.color.color6);
        colorCode.add(R.color.gray04);
        colorCode.add(R.color.color9);
        colorCode.add(R.color.color8);
        colorCode.add(R.color.color10);
        colorCode.add(R.color.color11);
        colorCode.add(R.color.color12);
        colorCode.add(R.color.color13);
        colorCode.add(R.color.color14);
        colorCode.add(R.color.color15);
        colorCode.add(R.color.color17);
        colorCode.add(R.color.color18);
        colorCode.add(R.color.color19);
        colorCode.add(R.color.color20);
        colorCode.add(R.color.color23);
        colorCode.add(R.color.color24);
        colorCode.add(R.color.color25);
        colorCode.add(R.color.color26);
        colorCode.add(R.color.color22);

        Random random = new Random();
        int number = random.nextInt(colorCode.size());

        return colorCode.get(number);
    }

    public ArrayList<ModelTask> getList(){
        return taskArrayList;
    }

    public void removeItem(int position) {
        taskArrayList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(ModelTask item , int position) {
        taskArrayList.add(position, item);
        notifyItemInserted(position);
    }

    public void editItem(int position) {
        //Edit Clicked, open Edit Activity to edit the book info
        Intent intent = new Intent(context, EditTaskActivity.class);
        intent.putExtra("taskId",taskArrayList.get(position).getId());
        context.startActivity(intent);
    }
}
