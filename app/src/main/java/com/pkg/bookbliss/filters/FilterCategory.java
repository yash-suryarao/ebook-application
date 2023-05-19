package com.pkg.bookbliss.filters;

import android.widget.Filter;

import com.pkg.bookbliss.adapter.AdaptorCategory;
import com.pkg.bookbliss.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    //arrayList in which we want to search
    ArrayList<ModelCategory> filterList;
    //adapter in which filter need to be implemented

    AdaptorCategory adaptorCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategory> filterList, AdaptorCategory adaptorCategory) {
        this.filterList = filterList;
        this.adaptorCategory = adaptorCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (constraint != null && constraint.length() >0) {

            //change to upper case
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    //add to firebase list
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
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adaptorCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        //notify changes
        adaptorCategory.notifyDataSetChanged();
    }
}
