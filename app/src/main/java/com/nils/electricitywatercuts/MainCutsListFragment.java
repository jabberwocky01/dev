/**
 * 
 */
package com.nils.electricitywatercuts;

import java.util.ArrayList;
import java.util.Locale;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

/**
 * @author NilS
 * 
 */
public class MainCutsListFragment extends ListFragment implements 
		LoaderManager.LoaderCallbacks<Cursor> {

	CutItemAdapter cutsArrayAdapter;
	ArrayList<Cuts> allCuts = new ArrayList<Cuts>();
	Handler handler = new Handler();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);	
		
		setEmptyText(getActivity().getString(R.string.no_cuts_text));

		// /** Getting FragmentManager object */
		 FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		
	    // Create a new Adapter and bind it to the List View
		cutsArrayAdapter = new CutItemAdapter(getActivity(),
				 fragmentManager, R.layout.cut_list_row, allCuts);
	    setListAdapter(cutsArrayAdapter);
	    
	    this.getListView().setTextFilterEnabled(true);
	    
	    
        String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
		Bundle args = new Bundle();
	    args.putString("searchCutsString", query);
	    
	    getLoaderManager().initLoader(0, args, this); 
	    
	    Thread t = new Thread(new Runnable() {
	      public void run() {
	    	  refreshCuts(); 
	      }
	    });
	    t.start();
	}
	
	public void refreshCuts() {
		handler.post(new Runnable() {
			public void run() {
		        String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
				Bundle args = new Bundle();
			    args.putString("searchCutsString", query);
			    
				getLoaderManager().restartLoader(0, args,
						MainCutsListFragment.this);

			}
		});

		getActivity().startService(
				new Intent(getActivity(), CutsUpdateService.class));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		String selectionArg = "";
		ArrayList<String> selectionArgList = new ArrayList<String>();
		
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String range = prefs.getString(CutsConstants.PREF_RANGE, "0");
		if ("0".equals(range)) {
			selectionArg = CutsConstants.KEY_IS_CURRENT + "=?";
			selectionArgList.add("T");
		}
		
	    if (args!=null && !"".equals(args.getString("searchCutsString"))) {
	    	String searchCutsStr = args.getString("searchCutsString");
	    	if (searchCutsStr!=null && !"".equals(searchCutsStr)) {
	    		searchCutsStr = searchCutsStr.toLowerCase(new Locale("tr-TR"));
	    		selectionArg += CutsConstants.KEY_SEARCH_TEXT + " LIKE ?";
	    		selectionArgList.add("%" + searchCutsStr + "%");
	    	}
	    }
			
		String[] projection = new String[] { CutsConstants.KEY_OPERATOR_NAME, CutsConstants.KEY_START_DATE,
				CutsConstants.KEY_END_DATE, CutsConstants.KEY_LOCATION, CutsConstants.KEY_REASON,
				CutsConstants.KEY_DETAIL, CutsConstants.KEY_TYPE };
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		String orderCriteriaOption = sharedPrefs.getString(CutsConstants.PREF_ORDER_CRITERIA, "end");
		String orderOption = sharedPrefs.getString(CutsConstants.PREF_ORDER, "desc");
		
		String sortOrderBy = CutsConstants.KEY_ORDER_END_DATE;
		if(orderCriteriaOption!=null && "start".equals(orderCriteriaOption)) {
			sortOrderBy = CutsConstants.KEY_ORDER_START_DATE;
		} 
		String sortOrder = " DESC";
		if(orderOption!=null && "asc".equals(orderOption)) {
			sortOrder = " ASC";
		}
		
		String[] selectionArgs = null;
		String selection = null;
		if(selectionArgList.size()>0) {
			selectionArgs = selectionArgList.toArray(new String[selectionArgList.size()]);
			selection = selectionArg;
		}

		CursorLoader loader = new CursorLoader(getActivity(),
				CutsConstants.CONTENT_URI, projection, selection, selectionArgs, sortOrderBy + sortOrder);

		return loader;
	}
	
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		allCuts.clear();
		
		int keyTaskIndex = 0;
	    while (cursor.moveToNext()) {
	    	Cuts cut = new Cuts();
	    	keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_OPERATOR_NAME);
	    	cut.setOperatorName(cursor.getString(keyTaskIndex));
	    	keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_START_DATE);
			cut.setStartDate(cursor.getString(keyTaskIndex));
			keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_END_DATE);
			cut.setEndDate(cursor.getString(keyTaskIndex));
			keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_LOCATION);
			cut.setLocation(cursor.getString(keyTaskIndex));
			keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_REASON);
			cut.setReason(cursor.getString(keyTaskIndex));
			keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_DETAIL);
			cut.setDetail(cursor.getString(keyTaskIndex));
			keyTaskIndex = cursor.getColumnIndexOrThrow(CutsConstants.KEY_TYPE);
			cut.setType(cursor.getString(keyTaskIndex));
			if ("e".equals(cut.getType()))
				cut.setIconResourceId(R.drawable.electricity);
			else
				cut.setIconResourceId(R.drawable.water);
	    	allCuts.add(cut);
	    }
	    cutsArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		allCuts.clear();
		cutsArrayAdapter.notifyDataSetChanged();
	}

}
