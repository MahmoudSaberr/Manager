package com.example.timetablemanager.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.timetablemanager.R;
import com.example.timetablemanager.adapters.AdapterTask;
import com.example.timetablemanager.databinding.FragmentTaskUserBinding;
import com.example.timetablemanager.models.ModelTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TaskUserFragment extends Fragment {


    //that we passed while creating instance of this fragment
    private String dayId, dayTitle;
    final String LINEAR = "Linear";
    final String GRID = "Grid";
    String currentVisibleView = LINEAR;
    private ArrayList<ModelTask> taskArrayList;
    private AdapterTask adapterTask;

    private FragmentTaskUserBinding binding;

    private static final String TAG = "TASK_USER_TAG";

    public TaskUserFragment() {
        // Required empty public constructor
    }

    public static TaskUserFragment newInstance(String dayId, String dayTitle) {
        TaskUserFragment fragment = new TaskUserFragment();
        Bundle args = new Bundle();
        args.putString("dayId", dayId);
        args.putString("dayTitle", dayTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayId = getArguments().getString("dayId");
            dayTitle = getArguments().getString("dayTitle");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentTaskUserBinding.inflate(LayoutInflater.from(getContext()),container,false);

        Log.d(TAG, "onCreateView: Day:"+dayTitle);
        if (dayTitle.equals("All")) {
            //load all Tasks
            loadAllTasks();
        }
        else {
            //load selected category books
            loadCDayTasks();
        }
        //search
        binding.fTaskSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called when user type any letter
                try {
                    adapterTask.getFilter().filter(s);
                }
                catch (Exception e) {
                    Log.d(TAG, "onTextChanged: "+ e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.fTaskChangeView.setOnClickListener(view -> {
            if (currentVisibleView.equals(LINEAR)) {
                binding.fTaskChangeView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_linear_view_sky));
                gridView();
            } else {
                binding.fTaskChangeView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_grid_view_sky));
                linearView();
            }
        });

        ItemTouchHelper helper =new ItemTouchHelper(callback);
        helper.attachToRecyclerView(binding.fTaskTasksRv);

        ItemTouchHelper itemTouchHelper =new ItemTouchHelper(edit);
        itemTouchHelper.attachToRecyclerView(binding.fTaskTasksRv);

        return  binding.getRoot();
    }

    private void loadAllTasks() {
        //init list
        taskArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tasks");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before start adding into it
                taskArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelTask model = ds.getValue(ModelTask.class);
                    //add to list
                    taskArrayList.add(model);
                }

                //Setup adapter
                adapterTask = new AdapterTask(getContext(),taskArrayList);
                //set adapter to recyclerview
                binding.fTaskTasksRv.setAdapter(adapterTask);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCDayTasks() {
        //init list
        taskArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tasks");
        ref.orderByChild("dayId").equalTo(dayId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before start adding into it
                        taskArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            //get data
                            ModelTask model = ds.getValue(ModelTask.class);
                            //add to list
                            taskArrayList.add(model);
                        }
                        //Setup adapter
                        adapterTask = new AdapterTask(getContext(),taskArrayList);
                        //set adapter to recyclerview
                        binding.fTaskTasksRv.setAdapter(adapterTask);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void linearView() {
        currentVisibleView = LINEAR;
        binding.fTaskTasksRv.setLayoutManager(new LinearLayoutManager(getContext()));
        AdapterTask adapter = new AdapterTask(getContext(),taskArrayList);
        binding.fTaskTasksRv.setAdapter(adapter);
    }

    private void gridView() {
        currentVisibleView = GRID;
        binding.fTaskTasksRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        AdapterTask adapter = new AdapterTask(getContext(),taskArrayList);
        binding.fTaskTasksRv.setAdapter(adapter);
    }

    ItemTouchHelper.SimpleCallback callback= new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();
            ModelTask item =adapterTask.getList().get(position);

            adapterTask.removeItem(position);

            Snackbar snackbar = Snackbar.make(binding.fTaskLayout,"Item Deleted",Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//this situation up when Snackbar is visible(means undo remove the item)
                            adapterTask.restoreItem(item,position);
                            binding.fTaskTasksRv.scrollToPosition(position);
                        }
                    }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {// is non-visible(means remove the item)
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);

                            if(!(event==DISMISS_EVENT_ACTION))
                            {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tasks");
                                reference.child(item.getId())
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: Book Deleted Successfully...");
                                                Toast.makeText(getContext(), item.getTitle()+" Deleted Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: Failed to delete from db due to"+e.getMessage());
                                                Toast.makeText(getContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });
            snackbar.setActionTextColor(Color.WHITE);
            snackbar.show();
        }
        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
            new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark))
                    .addSwipeRightActionIcon(R.drawable.ic_delete_white)
                    .setActionIconTint(ContextCompat.getColor(recyclerView.getContext(), android.R.color.white))
                    .addSwipeRightLabel("Delete")
                    .setSwipeRightLabelColor(Color.parseColor("#FFFFFF"))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    ItemTouchHelper.SimpleCallback edit= new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();
            adapterTask.editItem(position);
            adapterTask.notifyDataSetChanged();
        }

        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
            new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_500))
                    .addSwipeLeftActionIcon(R.drawable.ic_edit)
                    .setActionIconTint(ContextCompat.getColor(recyclerView.getContext(), android.R.color.white))
                    .addSwipeLeftLabel("Edit")
                    .setSwipeLeftLabelColor(Color.parseColor("#FFFFFF"))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}