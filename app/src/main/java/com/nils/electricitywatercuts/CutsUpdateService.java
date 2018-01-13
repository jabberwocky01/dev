/**
 * 
 */
package com.nils.electricitywatercuts;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

/**
 * @author NilS
 * 
 */
public class CutsUpdateService extends IntentService {

	public static String TAG = "CUTS_UPDATE_SERVICE";
	
	private NotificationCompat.Builder cutsNotificationBuilder;
	public static final int NOTIFICATION_ID = 1;
	private AlarmManager alarmManager;
	private PendingIntent alarmIntent;
	
	private ArrayList<Cuts> cutsForNotification = new ArrayList<Cuts>();
	private List<String> searchStrList = new ArrayList<String>();
	
	private Integer freqPreference;
	private String searchStringPreference;

	public CutsUpdateService() {
		super("CutsUpdateService");
	}

	public CutsUpdateService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Boolean notificationFlag = false;
		Bundle bundle = intent.getExtras();
		
		if (bundle!=null) {
			if(bundle.getString(CutsConstants.getIntentCutsFreq())!=null) {
				String freqPreferenceStr = bundle.getString(CutsConstants.getIntentCutsFreq());
				freqPreference = Integer.parseInt(freqPreferenceStr);
				registerAlarm();
			} else if (bundle.getBoolean(CutsConstants.getIntentCutsNotificationFlag(), false)) {
				notificationFlag = true;
			} else if (bundle.getBoolean(CutsConstants.getIntentCutsBootFlag(), false)) {
				organizeCutsDB();
				notificationFlag = true;
			}
		}
		refreshCuts(notificationFlag);
	}
	
	private void organizeCutsDB() {
		// Construct a where clause to make sure we don't already have this
		// cut in the provider.
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		Date oneMonthBefore = cal.getTime();
		
		DateFormat df = new SimpleDateFormat(CutsConstants.getYyyymmddhhmmss(), new Locale("tr-TR"));
		String oneMonthBeforeStr = df.format(oneMonthBefore);
		
		String selection = CutsConstants.getKeyOrderEndDate() + "<?";
		ContentResolver cr = getContentResolver();
		cr.delete(CutsConstants.getContentUri(), selection, new String[]{oneMonthBeforeStr});
	}
	
	private void registerAlarm() {
		if (freqPreference > 0) {
			long timeToRefresh = freqPreference	* 60 * DateUtils.MINUTE_IN_MILLIS;
			long startToRefresh = SystemClock.elapsedRealtime() + timeToRefresh;
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startToRefresh,
					timeToRefresh, alarmIntent);
		} else {
			alarmManager.cancel(alarmIntent);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void addNewCuts(ArrayList<Cuts> cutsList) {
		
		ContentResolver cr = getContentResolver();

		ArrayList<String> idArray = new ArrayList<String>();
		for (int i=0; i<cutsList.size(); i++) {
			Cuts cut = cutsList.get(i);
			// Construct a where clause to make sure we don't already have this
			// cut in the provider.
			String selection = CutsConstants.getKeyDetail() + "=? ";
			String[] selectionArgs = new String[] { cut.getDetail() };
	
			// If the cut is new, insert it into the provider.
			Cursor query = cr.query(CutsConstants.getContentUri(), null, selection, selectionArgs, null);
			boolean addOkFlag = true;
			if (query.getCount() == 0) {
				ContentValues values = new ContentValues();
	
				values.put(CutsConstants.getKeyOperatorName(), cut.getOperatorName());
				values.put(CutsConstants.getKeyStartDate(), cut.getStartDate());
				values.put(CutsConstants.getKeyEndDate(), cut.getEndDate());
				values.put(CutsConstants.getKeyLocation(), cut.getLocation());
				values.put(CutsConstants.getKeyReason(), cut.getReason());
				values.put(CutsConstants.getKeyDetail(), cut.getDetail());
				values.put(CutsConstants.getKeyType(), cut.getType());
				String cutSearchText = cut.getOperatorName() + ' ' + " " + cut.getLocation() + " " + cut.getReason();
				values.put(CutsConstants.getKeySearchText(), cutSearchText.toLowerCase(new Locale("tr-TR"))); 
				String orderStartDate = CutsConstants.formatDate(cut.getStartDate(), 
						CutsConstants.getDdmmyyyyhhmm(), CutsConstants.getYyyymmddhhmmss());
			    String orderEndDate = CutsConstants.formatDate(cut.getEndDate(), 
						CutsConstants.getDdmmyyyyhhmm(), CutsConstants.getYyyymmddhhmmss());
				values.put(CutsConstants.getKeyOrderStartDate(), orderStartDate);
				values.put(CutsConstants.getKeyOrderEndDate(), orderEndDate);
				values.put(CutsConstants.getKeyIsCurrent(), "T");
				
				if (searchStrList.size()>0) {
					addOkFlag = false;
					for(int j=0; j<searchStrList.size(); j++) {
						if(CutsConstants.compareCutsStr(cutSearchText, searchStrList.get(j))) {
							addOkFlag=true;
							break;
						}
					}			
				}
				if(addOkFlag)
					cutsForNotification.add(cut);
				
			    // Add the new cut to the cuts provider.
				cr.insert(CutsConstants.getContentUri(), values);
			} else {
				while(query.moveToNext()){
					Long id = query.getLong(query.getColumnIndex("_id"));
					idArray.add(id.toString());
				}
			}
			query.close();
		}
		// set previously inserted but still current cuts is_current = 'T'
		updateAsCurrent(idArray);
	}
		
	private void updateAsCurrent(ArrayList<String> idArray) {
		
		if (idArray.size() == 0)
			return;
		
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(CutsConstants.getKeyIsCurrent(), "T");
		String where = CutsConstants.getKeyId() + " IN (";

		for (int i=0; i<idArray.size() ; i++) {
			if(i == idArray.size()-1) 
				where += idArray.get(i) + ")";
			else
				where += idArray.get(i) + ", ";
		}
		// update as "current"
		cr.update(CutsConstants.getContentUri(), values, where, null);
	}
	
	private void updateCutsAsPrevious() {
		// update as "previous"
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(CutsConstants.getKeyIsCurrent(), "F");
		String where = CutsConstants.getKeyIsCurrent() + "=? ";
		String[] selectionArgs = new String[] { "T" };
		cr.update(CutsConstants.getContentUri(), values, where, selectionArgs);
	}

	public void refreshCuts(Boolean notificationFlag) {

		String[] urls = CutsConstants.getCutsLinkList();

		cutsForNotification.clear();
		updateCutsAsPrevious();
		
		ArrayList<Cuts> cutsList = new ArrayList<Cuts>();
		ArrayList<Cuts> temp = new ArrayList<Cuts>();
		for (int i = 0; i < urls.length; i++) {
			if (urls[i].contains("bedas")) {
				temp = getEuropeElectricityData(urls[i]);
				for (int j=0; j<temp.size() ;j++)
					cutsList.add(temp.get(j));
			} else if (urls[i].contains("ayedas")) {
				temp = getAnatoliaElectricityData(urls[i]);
				for (int j=0; j<temp.size() ;j++)
					cutsList.add(temp.get(j));
			} else if (urls[i].contains("iski")) {
				temp = getWaterData(urls[i]);
				for (int j=0; j<temp.size() ;j++)
					cutsList.add(temp.get(j));
			}
		}
		addNewCuts(cutsList);
		
		if (notificationFlag) {
		    // Trigger a notification.
		    broadcastNotification();
		}

	}

	public ArrayList<Cuts> getEuropeElectricityData(String link) {
		
		ArrayList<Cuts> electricalCuts = new ArrayList<Cuts>();

		// get html document structure
		try {
			Document document = Jsoup.parse(new URL(link).openStream(), "UTF-8", link);

			// selector query
			Elements cutDateList = document
					.select("div.ic-sayfa-content > table > tbody > tr > td:contains(Tarihleri arasında)");
			
			// check if any electricity cut exits
			if (cutDateList.size()>0) {
				Elements cutHourList = document
						.select("div.ic-sayfa-content > table > tbody > tr > td:contains(Kesinti Saatleri)");
				Elements cutReasonList = document
						.select("div.ic-sayfa-content > table > tbody > tr > td:contains(Kesinti Nedeni)");
				Elements cutDistrictList = document
						.select("div.ic-sayfa-content > table > tbody > tr > td:contains(bölge ve mahalleler)");
				// check results
				int index;
				String startDate, endDate, hourStr, startHour, endHour, operatorName, reason, location;
				for (int i = 1; i < cutDateList.size(); i++) {
					// get value	
					String dateStr = (cutDateList.get(i).getElementsByTag("b")).get(0).text();
					index = dateStr.indexOf("-");
					startDate = dateStr.substring(0,index);
					endDate = dateStr.substring(index+1);
					
					hourStr = cutHourList.get(i).text();
					index = hourStr.indexOf(":");
					hourStr = hourStr.substring(index+2);
					index = hourStr.indexOf("-");
					startHour = hourStr.substring(0,index).trim();
					endHour = hourStr.substring(index+1).trim();
					
					startDate = startDate + " " + startHour;
					endDate = endDate + " " + endHour;
					
					operatorName = (cutDateList.get(i).getElementsByTag("b")).get(1).text().substring(1);
					reason = cutReasonList.get(i).text();
					index = reason.indexOf(":");
					reason = reason.substring(index+2);
					location = cutDistrictList.get(i).text();
					index = location.indexOf(":");
					location = location.substring(index+1).trim();
					
					Cuts cut = new Cuts();
					cut.setType("e");
					cut.setIconResourceId(R.drawable.electricity);
					cut.setOperatorName(operatorName);
					cut.setReason(reason);
					cut.setStartDate(startDate);
					cut.setEndDate(endDate);
					cut.setLocation(location);
					cut.setDetail(operatorName + ' ' + startDate + "-" + endDate + " " + location + " " + reason);
	
					// Process a newly found cut
					electricalCuts.add(cut);
				}
			}
			
		} catch (IOException e) {
			//e.printStackTrace();
			sendToGoogleAnalytics(e);
		}
		
		return electricalCuts;
	}
	
	public ArrayList<Cuts> getAnatoliaElectricityData(String link) {
		
		ArrayList<Cuts> electricalCuts = new ArrayList<Cuts>();

		// get html document structure
		try {
			Document document = Jsoup.connect(link).timeout(10000).get();

			// selector query
			Elements cutDateList = document.select("div.grid1 span[id^=ctl00_SPWebPartManager1]");
			
			// check if any electricity cut exits
			if (cutDateList.size()>0) {
				Elements cutInfo = document.select("div[class^=ExternalClass]");
				// check results
				int startIndex, endIndex, dateCount;
				String cutListStr, startDate, endDate, hourStr, startHour, endHour, operatorName, reason="", location;
				for (int i = 0; i < cutDateList.size(); i++) {
					try {
						// get value
						String dateStr = cutDateList.get(i).text();
						startIndex = dateStr.indexOf(" ");
						startDate = dateStr.substring(0, startIndex);
						endDate = startDate;
						
						Elements cutLocationList = cutInfo.get(i).children();
						for (int j = 0; j<cutLocationList.size(); j++) {
							cutListStr = cutLocationList.get(j).text();
							cutListStr = cutListStr.replaceAll("\\s+", " ");

							endIndex = cutListStr.toLowerCase(new Locale("tr-TR")).indexOf("ilçe");
                            if (endIndex == -1)
                                continue;
                            startIndex = cutListStr.substring(0, endIndex-1).lastIndexOf(" ");

							operatorName = cutListStr.substring(startIndex, endIndex).trim();
							cutListStr = cutListStr.substring(endIndex);
							startIndex = cutListStr.indexOf(" ");
							cutListStr = cutListStr.substring(startIndex);

					        Pattern p = Pattern.compile("(([01]?[0-9]|2[0-3]):[0-5][0-9])\\s*-\\s*(([01]?[0-9]|2[0-3]):[0-5][0-9])");
					        Matcher matcher = p.matcher(cutListStr);
					        dateCount = 0;
					        while (matcher.find()) {
					        	dateCount++;
					        }
					        
					        for (int k=0; k<dateCount; k++) {
					        	endIndex = cutListStr.toLowerCase(new Locale("tr-TR")).indexOf("saatleri arasında");
								if (endIndex == -1)
									endIndex = cutListStr.indexOf(" ");
								hourStr = cutListStr.substring(0, endIndex).trim();
								while (hourStr.length()>0 && !Character.isDigit(hourStr.charAt(0))) {
									hourStr = hourStr.substring(1);
								}

                                if (hourStr.length() == 0) {
                                    cutListStr = cutListStr.substring(endIndex + 17);
                                    continue;
                                }

								startIndex = hourStr.indexOf("-");
								startHour = hourStr.substring(0,startIndex).trim();
								endHour = hourStr.substring(startIndex+1).trim();
								cutListStr = cutListStr.substring(endIndex + 17);
								
								startIndex = cutListStr.toLowerCase(new Locale("tr-TR")).indexOf("nedeniyle");
                                endIndex = cutListStr.toLowerCase(new Locale("tr-TR")).indexOf("dolayı");
								if (startIndex != -1 && endIndex != -1) {
									if (startIndex < endIndex) {
										reason = cutListStr.substring(0, startIndex).trim();
										cutListStr = cutListStr.substring(startIndex+10);
									} else {		
										reason = cutListStr.substring(0, endIndex+6).trim();
										cutListStr = cutListStr.substring(endIndex+7);
									}
						        } else {
									if (startIndex != -1) {
										reason = cutListStr.substring(0, startIndex).trim();
										cutListStr = cutListStr.substring(startIndex+10);
									} else if (endIndex != -1) {		
										reason = cutListStr.substring(0, endIndex+6).trim();
										cutListStr = cutListStr.substring(endIndex+7);
									}
						        }
		
								endIndex = cutListStr.toLowerCase(new Locale("tr-TR")).indexOf("enerji verilemeyecektir");
						        matcher = p.matcher(cutListStr);
						        if (matcher.find() && (endIndex == -1 || matcher.start() < endIndex)) {
						        	endIndex = matcher.start();
						        }
						        if (endIndex != -1) {
									location = cutListStr.substring(0, endIndex).trim();
									cutListStr = cutListStr.substring(endIndex);
								} else {
									location = cutListStr;
								}
								
								Cuts cut = new Cuts();
								cut.setType("e");
								cut.setIconResourceId(R.drawable.electricity);
								cut.setOperatorName(operatorName);
								cut.setReason(reason);
								cut.setStartDate(startDate + " " + startHour);
								cut.setEndDate(endDate + " " + endHour);
								cut.setLocation(location);
								cut.setDetail(operatorName + ' ' + startDate + "-" + endDate + " " + location + " " + reason);
				
								// Process a newly found cut
								electricalCuts.add(cut);
					        }
						}
			        } catch (Exception e) {
			        	sendToGoogleAnalytics(e);
			        }
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
			sendToGoogleAnalytics(e);
		}
		
		return electricalCuts;
	}

	public ArrayList<Cuts> getWaterData(String link) {
		
		ArrayList<Cuts> waterCuts = new ArrayList<Cuts>();

		// get html document structure
		try {
			Document document = Jsoup.connect(link).timeout(10000).get();

			// selector query
            Elements cutItemsList = document
                    .select("span#ctl00_ContentPlaceHolder1_lblContent table td[style=font-weight:normal]");


			for (int i = 0; i < cutItemsList.size(); i+=5) {
				// get value
				String operatorName = cutItemsList.get(i+1).text();
				String reason = cutItemsList.get(i+2).text();
				String startDate = cutItemsList.get(i).text();
				// format start date
				startDate = CutsConstants.formatDate(startDate, 
						CutsConstants.getYyyymmddHhmmss(), CutsConstants.getDdmmyyyyhhmmss());
				String endDateText = cutItemsList.get(i+4).text();
				String endDate = "", day="", month="", year="", time="";
				for (int j = 0; j < endDateText.length(); j++) {	
					if (Character.isDigit(endDateText.charAt(j))) {
						endDate += endDateText.charAt(j);
						if (endDate.length() == 4 && "".equals(year)) {
							year = endDate;
							endDate = "";
						}
					}
					
					if (endDateText.charAt(j) == '.') {
						if ("".equals(month)) {
							endDate = endDate.length()!=2 ? "0"+endDate : endDate;
							endDate += '.';
							if ("".equals(day))
								day = endDate;
							else
								month = endDate;
							endDate = "";
						} else {
							time = endDate;
						}
					} else if (endDateText.charAt(j) == ':') {
						endDate = endDate.length()!=2 ? "0"+endDate : endDate;
						endDate += ':';
					}
				}
				endDate = day + month + year + " " + time + ":00";

				String location = cutItemsList.get(i+3).text();
				location = location.replaceAll("<br>+", ", ");

				Cuts cut = new Cuts();
				cut.setType("w");
				cut.setIconResourceId(R.drawable.water);
				cut.setOperatorName(operatorName);
				cut.setReason(reason);
				cut.setStartDate(startDate);
				cut.setEndDate(endDate);
				cut.setLocation(location);
				cut.setDetail(operatorName + ' ' + startDate + "-" + endDate + " " + location + " " + reason);

				// Process a newly found cut
				waterCuts.add(cut);
			}
		} catch (IOException e) {
			//e.printStackTrace();
			sendToGoogleAnalytics(e);
		}
		
		return waterCuts;
	}
	
	void sendToGoogleAnalytics(Exception e) {
		// May return null if EasyTracker has not yet been initialized with a property ID.
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		if (easyTracker != null) {
			// StandardExceptionParser is provided to help get meaningful Exception descriptions.
			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null) // Context and optional collection of package names
			                                                                 // to be used in reporting the exception.
					.getDescription(Thread.currentThread().getName(), // The name of the thread on which the exception occurred.
							e), // The exception.
							false) // False indicates a fatal exception
			.build()
			);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		getCutsPreferences();
		if (freqPreference==0) {
			// initialize
			freqPreference = 1;
			registerAlarm();
		}
		return super.onStartCommand(intent, flags, startId);
	};

	@Override
	public void onCreate() {
		super.onCreate();
		// for notifications
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		Intent intentToFire = new Intent(CutsConstants.getIntentCutsNotification());
		alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
		
	    cutsNotificationBuilder = new NotificationCompat.Builder(this);
	    cutsNotificationBuilder
	      .setAutoCancel(true)
	      .setTicker(getString(R.string.cuts_ticker))
	      .setSmallIcon(R.drawable.notification_icon)
	      .setDefaults(Notification.DEFAULT_ALL);
	}
	
	private void getCutsPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		String freqPreferenceStr = prefs.getString(CutsConstants.getPrefFreq(), "0");
		freqPreference = Integer.parseInt(freqPreferenceStr);
		searchStringPreference = prefs.getString(
				CutsConstants.getPrefSearchStrOption(), "");
		if(!"".equals(searchStringPreference)) {
			searchStrList.clear();
			String[] searchStrAll = searchStringPreference.split(",");
			if(searchStrAll.length>2) {
				String str = "";
				for (int i=0; i<searchStrAll.length; i++) {
					str = searchStrAll[i].trim();
					searchStrList.add(str);
				}
			} else {
				searchStrList.add(searchStrAll[0]);
			}
		}
	}
	

	private void broadcastNotification() {
		
		if(cutsForNotification.isEmpty())
			return;
		
		// set selected locale for the notification text
		setLocale();
		
	    Intent startActivityIntent = new Intent(this, MainActivity.class);
	    PendingIntent launchIntent =
	      PendingIntent.getActivity(this, 0, startActivityIntent, 0);
	    
	    NotificationManager notificationManager 
	      = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	    
		cutsNotificationBuilder.setContentIntent(launchIntent)
							.setContentTitle(getString(R.string.cuts_ticker))
							.setContentText(getString(R.string.cuts_notify_text));
	    
		int numOfMessages = 0;
		String detailedText = "", cutTitle = "";
		
		if (cutsForNotification.size() == 1) {
			
			Cuts cut = cutsForNotification.get(0);
			
			cutTitle = getString(R.string.water_label);
			if ("e".equals(cut.getType()))
				cutTitle = getString(R.string.electricity_label);
			cutTitle = getString(R.string.cuts_notify_header, cutTitle);
			
			detailedText += cut.getLocation() + " " + cut.getStartDate() + " - " + cut.getEndDate();
			
			cutsNotificationBuilder.setContentTitle(cutTitle)
								.setContentText(detailedText)
								.setStyle(new NotificationCompat.BigTextStyle().bigText(detailedText));			
			
		} else {
		    for (Cuts cut : cutsForNotification) {
		    	
				cutTitle = getString(R.string.water_label);
				if ("e".equals(cut.getType()))
					cutTitle = getString(R.string.electricity_label);
			
				detailedText += cutTitle + ": " + cut.getLocation() + " " + 
						cut.getStartDate() + " - " + cut.getEndDate() + System.getProperty("line.separator");
			    	
			    cutsNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(detailedText))
			    					 .setNumber(++numOfMessages);
	
		  }
		}
			
	    notificationManager.notify(NOTIFICATION_ID, cutsNotificationBuilder.build());
	}
	
    private void setLocale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String lang = prefs.getString(CutsConstants.getPrefLang(), "tr");
    	Locale appLocale = new Locale(lang);
        Locale.setDefault(appLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = appLocale;
        res.updateConfiguration(conf, dm);
    }
}
