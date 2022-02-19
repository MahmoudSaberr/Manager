package com.example.timetablemanager.filters;

import android.widget.Filter;

import com.example.timetablemanager.adapters.AdapterTask;
import com.example.timetablemanager.models.ModelTask;

import java.util.ArrayList;

public class FilterTask extends Filter {

    //arrayList in which we want to search
    ArrayList<ModelTask> filterList;
    //adapter in which filter need to be implemented
    AdapterTask adapterTask;

    //Constructor


    public FilterTask(ArrayList<ModelTask> filterList, AdapterTask adapterTask) {
        this.filterList = filterList;
        this.adapterTask = adapterTask;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (charSequence != null && charSequence.length() > 0) {
            //change to upper case, or Lower case to avoid case sensitivity
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelTask> filteredModels = new ArrayList<>();

            for (int i= 0;i<filterList.size();i++)
            {
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence))
                {
                    //add to filteredModels
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //apply filter changes
        adapterTask.taskArrayList = (ArrayList<ModelTask>) filterResults.values;

        //notify changes
        adapterTask.notifyDataSetChanged();
    }
}
