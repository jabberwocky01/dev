/**
 * 
 */
package com.nils.electricitywatercuts;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * @author NilS
 * 
 */

public class CutItemAdapter extends ArrayAdapter<Cuts> implements Filterable {
	
	private Context adapterContext;
	private FragmentManager mFragmentManager;
    private TextView cutText;
    private List<Cuts> originalCutItems = new ArrayList<Cuts>(); // Original Values
    private List<Cuts> filteredCutItems  = new ArrayList<Cuts>(); // Values to be displayed
    private CutsFilter cutFilter;
    private final Object mLock = new Object();
    private final CutsHelper cutsHelper = new CutsHelper();
 
    public CutItemAdapter(Context context, FragmentManager fm, int textViewResourceId,
            List<Cuts> objects) {
        super(context, textViewResourceId, objects);
        adapterContext = context;
        mFragmentManager = fm;
        this.originalCutItems = objects;
        this.filteredCutItems = objects;
    }
 
    @Override
    public int getCount() {
        return this.filteredCutItems.size();
    }
 
    @Override
    public Cuts getItem(int index) {
        return this.filteredCutItems.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.cut_list_row, null);
        }
 
        // Get item
        Cuts cutItem = getItem(position);
      
        // Get reference to TextView
        cutText = (TextView) row.findViewById(R.id.cutText);
        // set icon
        cutText.setCompoundDrawablesWithIntrinsicBounds(cutItem.getIconResourceId(), 
                0, 0, 0 );   
        cutText.setText(cutItem.toString());
        
        cutText.setTag(""+position);
        
        // can be either no cuts item or real data
        if (cutItem.getType()!=null) {
    	 
        	cutText.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                
                    /** Instantiating TimeDailogFragment, which is a DialogFragment object */
                	CutDetailsDialogFragment tFragment = new CutDetailsDialogFragment();
     
                    /** Creating a bundle object to store the position of the selected country */
                    Bundle b = new Bundle();
                    /** Storing the position in the bundle object */
                    
                    int position = Integer.parseInt((String) v.getTag());
                    Cuts selectedCut = getItem(position);   
                    
                    b.putString("selectedCutDetails", selectedCut.getDetailedText(adapterContext.getString(R.string.operator_title), 
    														                		adapterContext.getString(R.string.start_end_date_title), 
    														                		adapterContext.getString(R.string.location_title), 
    														                		adapterContext.getString(R.string.reason_title))); 
                    
                    // final Uri uri = Uri.parse(CutsConstants.getDefaultTargetUri() + getContext().getPackageName());
                    final Uri uri = Uri.parse(CutsConstants.DEFAULT_TARGET_URI);
                    
                    b.putString("selectedCutPlainText", selectedCut.getPlainText(adapterContext.getString(R.string.electricity_cut_label),
    														                		adapterContext.getString(R.string.water_cut_label),
    														                		adapterContext.getString(R.string.operator_title), 
    														                		adapterContext.getString(R.string.start_end_date_title), 
    														                		adapterContext.getString(R.string.location_title), 
    														                		adapterContext.getString(R.string.reason_title))
    											+ ". " + adapterContext.getString(R.string.for_more_cuts_label) + " " + uri.toString()); 
                    
                    /** Setting the bundle object as an argument to the DialogFragment object */
                    tFragment.setArguments(b);
                    
                	/** Opening the fragment object */
                	tFragment.show(mFragmentManager, "cut_details_dialog");
                }
            });
        } 
        
        return row;
    }
    

    /**
     * Implementing the Filterable interface.
     */
    public Filter getFilter() {
        if (cutFilter == null) {
        	cutFilter = new CutsFilter();
        }
        return cutFilter;
    }
    
    /**
     * Custom Filter implementation for the items adapter.
     *
     */
    @SuppressLint("DefaultLocale")
	private class CutsFilter extends Filter {
		protected FilterResults performFiltering(CharSequence prefix) {
            // Initiate our results object
            FilterResults results = new FilterResults(); // Holds the results of a filtering operation in values
            ArrayList<Cuts> itemsToShow = new ArrayList<Cuts>();
            // If the adapter array is empty, check the actual items array and use it
            if (originalCutItems == null) {
                synchronized (mLock) { // Notice the declaration above
                	originalCutItems = new ArrayList<Cuts>(filteredCutItems); // saves the original data in mOriginalValues
                }
            }
            // No prefix is sent to filter by so we're going to send back the original array
            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    results.values = originalCutItems;
                    results.count = originalCutItems.size();
                }
            } else {
                // Compare lower case strings
                String prefixString = prefix.toString();
                for (int i = 0; i < originalCutItems.size(); i++) {
                    Cuts item = originalCutItems.get(i);
                    //skip filter if no cuts item
                    if (item.getType()!=null) {
	                    String itemName = item.toString();
	                    // First match against the whole, non-splitted value
	                    if (cutsHelper.compareCutsStr(itemName, prefixString)) {
	                    	itemsToShow.add(item);
	                    }
                    }
                }
                // Set and return
                results.values = itemsToShow;
                results.count = itemsToShow.size();
            }
            return results;
        }
		
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence prefix, FilterResults results) {
            //noinspection unchecked
        	filteredCutItems = (ArrayList<Cuts>) results.values; // has the filtered values
            // Let the adapter know about the updated list
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
  
}
