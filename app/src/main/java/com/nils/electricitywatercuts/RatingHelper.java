/**
 * 
 */
package com.nils.electricitywatercuts;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author NilS
 * 
 * In the sample below, after a week of use and opening the app up at least 10 times, the dialog appears to get their review/rating.
 * Clicking "No thanks/Already have" will dismiss the box and never show it again
 * Clicking "Remind me later" will dismiss the dialog and display it again after 15 days from this point and after opening the app 15 more times
 * Clicking "Rate YOUR_APP" the user will be taken to the Market to your app. This will start up a new cycle to show the dialog again in after 30 days and 
 * after opening the app 25 more times. This is due to the fact that there is no way to ensure that they did rate/review your app. 
 * At this point, they can then click the "No thanks/Already have" button and dismiss the rating dialog forever
 * 
 */

public class RatingHelper {

	private static void clearFirstLaunchPrefs(SharedPreferences.Editor editor) {
		editor.remove(CutsConstants.COUNT_APP_LAUNCH);
		editor.remove(CutsConstants.DATE_FIRST_LAUNCH);
		editor.commit();
	}

	private static void clearRemindPrefs(SharedPreferences.Editor editor) {
		editor.remove(CutsConstants.RATE_REMIND);
		editor.remove(CutsConstants.COUNT_REMIND_LAUNCH);
		editor.remove(CutsConstants.DATE_REMIND_START);
		editor.commit();
	}

	private static void clearRatedPrefs(SharedPreferences.Editor editor) {
		editor.remove(CutsConstants.RATE_CLICKED_RATE);
		editor.remove(CutsConstants.COUNT_RATED_LAUNCH);
		editor.remove(CutsConstants.DATE_RATED_START);
		editor.commit();
	}

	private static void addRemindPrefs(SharedPreferences.Editor editor) {
		editor.putBoolean(CutsConstants.RATE_REMIND, true);
		editor.putLong(CutsConstants.DATE_REMIND_START,
				System.currentTimeMillis());
		editor.commit();
	}

	private static void addRatedPrefs(SharedPreferences.Editor editor) {
		editor.putBoolean(CutsConstants.RATE_CLICKED_RATE, true);
		editor.putLong(CutsConstants.DATE_RATED_START,
				System.currentTimeMillis());
		editor.commit();
	}

	private static void addDontShowPref(SharedPreferences.Editor editor) {
		clearRemindPrefs(editor);
		clearRatedPrefs(editor);
		editor.putBoolean(CutsConstants.RATE_DONT_SHOW, true);
		editor.commit();
	}

	public static void app_launched(Context mContext) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		
		boolean dontShow = prefs.getBoolean(CutsConstants.RATE_DONT_SHOW,
				false);
		if (dontShow)
			return;

		long appLaunchCount = prefs.getLong(CutsConstants.COUNT_APP_LAUNCH,
				0);
		long remindLaunchCount = prefs.getLong(
				CutsConstants.COUNT_REMIND_LAUNCH, 0);
		long ratedLaunchCount = prefs.getLong(
				CutsConstants.COUNT_RATED_LAUNCH, 0);
		long appFirstLaunchDate = prefs.getLong(
				CutsConstants.DATE_FIRST_LAUNCH, 0);
		long remindStartDate = prefs.getLong(
				CutsConstants.DATE_REMIND_START, 0);
		long ratedStartDate = prefs.getLong(CutsConstants.DATE_RATED_START,
				0);

		SharedPreferences.Editor editor = prefs.edit();

		boolean ratedClicked = prefs.getBoolean(
				CutsConstants.RATE_CLICKED_RATE, false);
		if (ratedClicked) {
			long launches = ratedLaunchCount + 1;
			editor.putLong(CutsConstants.COUNT_RATED_LAUNCH, launches);

			if (ratedStartDate == 0) {
				ratedStartDate = System.currentTimeMillis();
				editor.putLong(CutsConstants.DATE_RATED_START,
						ratedStartDate);
			}

			if (launches >= CutsConstants.LAUNCHES_RATED) {
				if (System.currentTimeMillis() >= ratedStartDate
						+ (CutsConstants.DAYS_RATED_PROMPT * 24 * 60 * 60 * 1000)) {
					clearRatedPrefs(editor);
					showRateDialog(mContext, editor);
				} else
					editor.commit();
			} else
				editor.commit();

			return;
		}

		boolean remindLater = prefs.getBoolean(CutsConstants.RATE_REMIND,
				false);
		if (remindLater) {
			long launches = remindLaunchCount + 1;
			editor.putLong(CutsConstants.COUNT_REMIND_LAUNCH, launches);

			if (remindStartDate == 0) {
				remindStartDate = System.currentTimeMillis();
				editor.putLong(CutsConstants.DATE_REMIND_START,
						remindStartDate);
			}

			if (launches >= CutsConstants.LAUNCHES_REMIND) {
				if (System.currentTimeMillis() >= remindStartDate
						+ (CutsConstants.DAYS_REMIND_PROMPT * 24 * 60 * 60 * 1000)) {
					clearRemindPrefs(editor);
					showRateDialog(mContext, editor);
				} else
					editor.commit();
			} else
				editor.commit();

			return;
		}

		long launches = appLaunchCount + 1;
		editor.putLong(CutsConstants.COUNT_APP_LAUNCH, launches);

		if (appFirstLaunchDate == 0) {
			appFirstLaunchDate = System.currentTimeMillis();
			editor.putLong(CutsConstants.DATE_FIRST_LAUNCH,
					appFirstLaunchDate);
		}

		if (launches >= CutsConstants.LAUNCHES_FIRST_PROMPT) {
			if (System.currentTimeMillis() >= appFirstLaunchDate
					+ (CutsConstants.DAYS_FIRST_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(mContext, editor);
			} else
				editor.commit();
		} else
			editor.commit();
	}

	public static void showRateDialog(final Context mContext,
			final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(mContext);

		String appTitle = mContext.getString(R.string.app_name);

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout rateView = (LinearLayout) inflater.inflate(
				R.layout.app_rate_dialog, null, false);
		TextView rateTitle = (TextView) rateView
				.findViewById(R.id.rating_title);
		rateTitle.setText(mContext.getString(R.string.cuts_app_rate_sum, appTitle));
		TextView rateDesc = (TextView) rateView
				.findViewById(R.id.rating_description);
		rateDesc.setText(mContext.getString(R.string.cuts_app_rate, appTitle));

		Button b1 = (Button) rateView.findViewById(R.id.rating_ratebtn);
		b1.setText(mContext.getString(R.string.cuts_app_rate_btn));
		b1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					clearFirstLaunchPrefs(editor);
					clearRemindPrefs(editor);
					addRatedPrefs(editor);
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("market://details?id="
									+ mContext.getPackageName())));
				} 
				dialog.dismiss();
			}
		});

		Button b2 = (Button) rateView.findViewById(R.id.rating_remindbtn);
		b2.setText(mContext.getString(R.string.cuts_app_remind_btn));
		b2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					clearFirstLaunchPrefs(editor);
					clearRatedPrefs(editor);
					addRemindPrefs(editor);
				}
				dialog.dismiss();
			}
		});

		Button b3 = (Button) rateView.findViewById(R.id.rating_nothanksbtn);
		b3.setText(mContext.getString(R.string.cuts_app_already_btn));
		b3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					clearFirstLaunchPrefs(editor);
					clearRemindPrefs(editor);
					clearRatedPrefs(editor);
					addDontShowPref(editor);
				} 
				dialog.dismiss();
			}
		});

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(rateView);
		dialog.show();
	}
}
