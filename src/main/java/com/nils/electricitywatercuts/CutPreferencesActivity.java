/**
 * 
 */
package com.nils.electricitywatercuts;

import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class CutPreferencesActivity extends PreferenceActivity {


	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.cuts_user_preferences);
		}
	}

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return CutPreferencesSecondFragment.class.getName().equals(fragmentName);
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBuildHeaders(List<Header> target) {
        // update app title
        setTitle(getString(R.string.app_name));
		loadHeadersFromResource(R.xml.cuts_user_preference_headers, target);
	}
	
    @Override
    public void onStart() {
      super.onStart();
      // for google analytics
      EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      // for google analytics
      EasyTracker.getInstance(this).activityStop(this);
    }
}
