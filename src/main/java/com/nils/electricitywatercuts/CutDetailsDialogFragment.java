/**
 * 
 */
package com.nils.electricitywatercuts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author TOSHIBA
 *
 */
public class CutDetailsDialogFragment extends DialogFragment {
	
	private String[] appNames = null;
	private String shareContent = null;
	
	// http://wptrafficanalyzer.in/blog/opening-a-dailogfragment-on-an-item-click-in-android/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	/** Inflating layout for the dialog */
        View view = inflater.inflate(R.layout.cut_details, container, false);
 
        /** Getting the arguments passed to this fragment. Here we expects the selected item's position as argument */
        Bundle bundle = getArguments();
 
        /** Setting the title for the dialog window */
        getDialog().setTitle(R.string.cut_details);
 
        /** Getting the reference to the TextView object of the layout */
        TextView cutDetailsTextView = (TextView) view.findViewById(R.id.cutDetailsTextView);
 
        /** Setting the current time to the TextView object of the layout */
        cutDetailsTextView.setText(Html.fromHtml(bundle.getString("selectedCutDetails")));
        
        // initialize share options array
        appNames = getResources().getStringArray(R.array.cuts_share_opts);
        shareContent = bundle.getString("selectedCutPlainText");
        
        Button button = (Button) view.findViewById(R.id.shareButton);

        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	shareCutNews(v);
            }
        });
 
        /** Returns the View object */
        return view;
    }
    
    public void shareCutNews(View view) {
        List<Intent> targetedShareIntents = new ArrayList<Intent>();
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        share.setType("text/plain");
        String title = getString(R.string.app_name) + " - " + getString(R.string.cut_details);
        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                targetedShare.setType("text/plain"); // put here your mime type

                if (checkIfContains(info.activityInfo.packageName.toLowerCase(Locale.ENGLISH)) || 
                		checkIfContains(info.activityInfo.name.toLowerCase(Locale.ENGLISH))) {
                	targetedShare.putExtra(Intent.EXTRA_TITLE, title);
                	targetedShare.putExtra(Intent.EXTRA_SUBJECT, title);
                    targetedShare.putExtra(Intent.EXTRA_TEXT, shareContent);
                    targetedShare.setPackage(info.activityInfo.packageName);
                    targetedShareIntents.add(targetedShare);
                }
            }

            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), getString(R.string.choose_app_to_share_msg));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        } else {
            Toast.makeText(getActivity(), R.string.no_share_apps_err_msg, Toast.LENGTH_LONG).show();
        }
    }
    
    private boolean checkIfContains(String mainStr) {
    	for(int i=0; i<appNames.length;i++) {
    		if(mainStr.contains(appNames[i]))
    			return true;
    	}
    	return false;
    }

    
}
