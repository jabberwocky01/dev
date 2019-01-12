package com.nils.electricitywatercuts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
	OnSharedPreferenceChangeListener {

	private Locale appLocale = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.cutsListLayout) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            
            createShortCut();
            
    		setLocale(getAppLanguage(), false);
            
    		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    	    MainCutsListFragment myFragment = new MainCutsListFragment();	
    	    fragmentTransaction.add(R.id.cutsListLayout, myFragment);
    	    fragmentTransaction.commit();
    	    // rate app
    	    RatingHelper.app_launched(this);
        }

	}
	
	public void createShortCut() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean dontCreateShortcut = prefs.getBoolean(CutsConstants.INSTALL_SHORTCUT,
				false);
		if (dontCreateShortcut)
			return;
		
		// a Intent to create a shortCut
		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// repeat to create is forbidden
		shortcutIntent.putExtra("duplicate", false);
		// set the name of shortCut
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		// set icon
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				getApplicationContext(), R.drawable.ic_launcher);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// set the application to launch when you click the icon
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
				getApplicationContext(), MainActivity.class));
		// set pref
		dontCreateShortcut = true;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(CutsConstants.INSTALL_SHORTCUT, dontCreateShortcut);
		editor.commit();
		// sendBroadcast,done
		sendBroadcast(shortcutIntent);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	    if (CutsConstants.PREF_ORDER_CRITERIA.equals(key) ||
	    	CutsConstants.PREF_ORDER.equals(key) ||
	    	CutsConstants.PREF_RANGE.equals(key)) {
	    	refreshCutsList();
	    } else if (CutsConstants.PREF_LANG.equals(key)) {
	    	String lang = prefs.getString(CutsConstants.PREF_LANG, "tr");
	    	setLocale(lang, true);
	    } else if (CutsConstants.PREF_FREQ.equals(key)) {
	   	    Intent startIntent = new Intent(getApplicationContext(), CutsUpdateService.class);
	   	    String freqPreferenceStr = prefs.getString(CutsConstants.PREF_FREQ, "1");
	   	    startIntent.putExtra(CutsConstants.INTENT_CUTS_FREQ, freqPreferenceStr);
	   	    getApplicationContext().startService(startIntent);
	    }
	}	

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		refreshCutsList();
	}
	
	private void refreshCutsList() {
		/** Getting FragmentManager object */
		FragmentManager fragmentManager = getSupportFragmentManager();
		MainCutsListFragment earthquakeList = (MainCutsListFragment) fragmentManager
				.findFragmentById(R.id.cutsListLayout);
		
		earthquakeList.refreshCuts();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);	
		
	    MenuItem searchItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	    // Configure the search info and add any event listeners
	    final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			/** Getting FragmentManager object */
			FragmentManager fragmentManager = getSupportFragmentManager();
			MainCutsListFragment earthquakeList = (MainCutsListFragment) fragmentManager
					.findFragmentById(R.id.cutsListLayout);
			
	        @Override
	        public boolean onQueryTextChange(String searchStr) {
	        	earthquakeList.cutsArrayAdapter.getFilter().filter(searchStr);
	            return true;
	        }

	        @Override
	        public boolean onQueryTextSubmit(String searchStr) {
	        	earthquakeList.cutsArrayAdapter.getFilter().filter(searchStr);
	            return true;
	        }
	    };
	    searchView.setOnQueryTextListener(queryTextListener);
		
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int menuItemId = item.getItemId();
		if (menuItemId == R.id.menu_update) {
			refreshCutsList();
			return true;
		} else if (menuItemId == R.id.action_settings) {
			Intent i = new Intent(this, CutPreferencesActivity.class);
			startActivity(i);
			return true;
		}
		return false;
	}
	
    private void setLocale(String lang, boolean reloadFlag) {
    	 
    	appLocale = new Locale(lang);
        Locale.setDefault(appLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = appLocale;
        res.updateConfiguration(conf, dm);
        if (reloadFlag) {
	        Intent refresh = new Intent(this, MainActivity.class);
	        startActivity(refresh);
        }
        // update app title
        setTitle(getString(R.string.app_name));
    }
    
    private String getAppLanguage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
		String lang = prefs.getString(CutsConstants.PREF_LANG, "tr");
		return lang;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLocale(getAppLanguage(), false);
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
