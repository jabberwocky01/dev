/**
 * 
 */
package com.nils.electricitywatercuts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * @author NilS
 *
 */

@SuppressLint("NewApi")
public class CutPreferencesSecondFragment extends PreferenceFragment {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int preferenceToLoad=-1;
        String settings = getArguments().getString("cuts_preference");
        if (CutsConstants.SETTING_RANGE.equals(settings)) {
        	preferenceToLoad= R.xml.cuts_user_preference_range; 
    	} else if (CutsConstants.SETTING_FREQ.equals(settings)) {
        	preferenceToLoad= R.xml.cuts_user_preference_refresh;       	
        } else if (CutsConstants.SETTING_ORDER.equals(settings)) {
        	preferenceToLoad=R.xml.cuts_user_preference_order;
        } else if (CutsConstants.SETTING_LANG.equals(settings)) {
        	preferenceToLoad=R.xml.cuts_user_preference_language;
        }
        
        addPreferencesFromResource(preferenceToLoad);
        
        if (CutsConstants.SETTING_FREQ.equals(settings)) {
	    	ListPreference itemList = (ListPreference)findPreference(CutsConstants.PREF_FREQ);
	    	String freqPrefStr = itemList.getValue();
	    	disableSearchPreference(freqPrefStr);
	    	itemList.setOnPreferenceChangeListener(new
	        	Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						final String val = newValue.toString();
						disableSearchPreference(val);
						return true;
					}
	    	});
	    }
    }
    
    private void disableSearchPreference(String searchStr) {
		EditTextPreference itemList2 = (EditTextPreference)findPreference(CutsConstants.PREF_SEARCH_STR_OPTION);
		if("-1".equals(searchStr))
			itemList2.setEnabled(false);
		else
			itemList2.setEnabled(true);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    }

}
