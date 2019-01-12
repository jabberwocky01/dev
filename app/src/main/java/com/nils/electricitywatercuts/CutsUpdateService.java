/**
 * 
 */
package com.nils.electricitywatercuts;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.nils.electricitywatercuts.com.nils.electricitywatercuts.model.EuropeElectricityData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private final CutsHelper cutsHelper = new CutsHelper();

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
			if(bundle.getString(CutsConstants.INTENT_CUTS_FREQ)!=null) {
				String freqPreferenceStr = bundle.getString(CutsConstants.INTENT_CUTS_FREQ);
				freqPreference = Integer.parseInt(freqPreferenceStr);
				registerAlarm();
			} else if (bundle.getBoolean(CutsConstants.INTENT_CUTS_NOTIFICATION_FLAG, false)) {
				notificationFlag = true;
			} else if (bundle.getBoolean(CutsConstants.INTENT_CUTS_BOOT_FLAG, false)) {
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
		
		DateFormat df = new SimpleDateFormat(CutsConstants.yyyyMMddHHmmss, new Locale("tr-TR"));
		String oneMonthBeforeStr = df.format(oneMonthBefore);
		
		String selection = CutsConstants.KEY_ORDER_END_DATE + "<?";
		ContentResolver cr = getContentResolver();
		cr.delete(CutsConstants.CONTENT_URI, selection, new String[]{oneMonthBeforeStr});
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
			String selection = CutsConstants.KEY_DETAIL + "=? ";
			String[] selectionArgs = new String[] { cut.getDetail() };
	
			// If the cut is new, insert it into the provider.
			Cursor query = cr.query(CutsConstants.CONTENT_URI, null, selection, selectionArgs, null);
			boolean addOkFlag = true;
			if (query.getCount() == 0) {
				ContentValues values = new ContentValues();
	
				values.put(CutsConstants.KEY_OPERATOR_NAME, cut.getOperatorName());
				values.put(CutsConstants.KEY_START_DATE, cut.getStartDate());
				values.put(CutsConstants.KEY_END_DATE, cut.getEndDate());
				values.put(CutsConstants.KEY_LOCATION, cut.getLocation());
				values.put(CutsConstants.KEY_REASON, cut.getReason());
				values.put(CutsConstants.KEY_DETAIL, cut.getDetail());
				values.put(CutsConstants.KEY_TYPE, cut.getType());
				String cutSearchText = cut.getOperatorName() + ' ' + " " + cut.getLocation() + " " + cut.getReason();
				values.put(CutsConstants.KEY_SEARCH_TEXT, cutSearchText.toLowerCase(new Locale("tr-TR")));
				String orderStartDate = cutsHelper.formatDate(cut.getStartDate(),
						CutsConstants.ddMMyyyyHHmm, CutsConstants.yyyyMMddHHmmss);
			    String orderEndDate = cutsHelper.formatDate(cut.getEndDate(),
						CutsConstants.ddMMyyyyHHmm, CutsConstants.yyyyMMddHHmmss);
				values.put(CutsConstants.KEY_ORDER_START_DATE, orderStartDate);
				values.put(CutsConstants.KEY_ORDER_END_DATE, orderEndDate);
				values.put(CutsConstants.KEY_IS_CURRENT, "T");
				
				if (searchStrList.size()>0) {
					addOkFlag = false;
					for(int j=0; j<searchStrList.size(); j++) {
						if(cutsHelper.compareCutsStr(cutSearchText, searchStrList.get(j))) {
							addOkFlag=true;
							break;
						}
					}			
				}
				if(addOkFlag)
					cutsForNotification.add(cut);
				
			    // Add the new cut to the cuts provider.
				cr.insert(CutsConstants.CONTENT_URI, values);
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
		values.put(CutsConstants.KEY_IS_CURRENT, "T");
		String where = CutsConstants.KEY_ID + " IN (";

		for (int i=0; i<idArray.size() ; i++) {
			if(i == idArray.size()-1) 
				where += idArray.get(i) + ")";
			else
				where += idArray.get(i) + ", ";
		}
		// update as "current"
		cr.update(CutsConstants.CONTENT_URI, values, where, null);
	}
	
	private void updateCutsAsPrevious() {
		// update as "previous"
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(CutsConstants.KEY_IS_CURRENT, "F");
		String where = CutsConstants.KEY_IS_CURRENT + "=? ";
		String[] selectionArgs = new String[] { "T" };
		cr.update(CutsConstants.CONTENT_URI, values, where, selectionArgs);
	}

	public void refreshCuts(Boolean notificationFlag) {

		String[] urls = CutsConstants.CUTS_LINK_LIST;

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
            Calendar cal=Calendar.getInstance();
            Date date = cal.getTime();
			DateFormat paramDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            List<String> types = Arrays.asList(CutsConstants.BEDAS_CUT_TYPE_PLANNED, CutsConstants.BEDAS_CUT_TYPE_INSTANTANEOUS);

            // look up for 5 days
            for (int i=0; i<5; i++) {
                for (String type : types) {
                    String formattedUrl = String.format(link, "0", type, paramDateFormat.format(date));

//                    Document document = Jsoup.parse(new URL(formattedUrl).openStream(), "UTF-8", formattedUrl);

					Document document = Jsoup.connect(formattedUrl).timeout(10000).ignoreContentType(true).validateTLSCertificates(false).get();

                    // selector query
                    String text = document.text();
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> map = new HashMap<String, Object>();

                    List<EuropeElectricityData> cutList =
                            mapper.readValue(text, mapper.getTypeFactory().constructCollectionType(List.class, EuropeElectricityData.class));

                    if (cutList != null) {
                        for (EuropeElectricityData europeElectricityData : cutList) {
                            String startDate = dateFormat.format(date) + " " + europeElectricityData.getStartHour();
                            String endDate = dateFormat.format(date) + " " + europeElectricityData.getEndHour();

                            String location = europeElectricityData.getLocation();
							Pattern regex = Pattern.compile("\\((.*?)İlçesi\\)");
							Matcher regexMatcher = regex.matcher(location);

							String operatorName = "";
							if (regexMatcher.find()) {
								operatorName = regexMatcher.group(1);
							} else {
								int index = location.indexOf("İlçesi");
								if (index != -1) {
									operatorName = location.substring(0, index);
									index = location.indexOf(" ");
									if (index != -1) {
										operatorName = operatorName.substring(index+1);
									}
								}
							}

                            Cuts cut = new Cuts();
                            cut.setType("e");
                            cut.setIconResourceId(R.drawable.electricity);
							cut.setOperatorName(operatorName.trim());
                            cut.setReason(europeElectricityData.getReason());
                            cut.setStartDate(startDate);
                            cut.setEndDate(endDate);
                            cut.setLocation(europeElectricityData.getLocation());
                            cut.setDetail(operatorName + ' ' + startDate + "-" + endDate +
                                    " " + cut.getLocation() +
                                    " " + cut.getReason());

                            // Process a newly found cut
                            electricalCuts.add(cut);
                        }
                    }
                }
                cal.add(Calendar.DATE, 1);
                date = cal.getTime();
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
			Elements cutDateList = document.select("table.table-responsive tr");
			
			// check if any electricity cut exits
			if (cutDateList.size()>0) {
				// check results
				int startIndex, endIndex, dateCount;
				String cutListStr, startDate, endDate, hourStr, startHour, endHour, operatorName, reason="Planlı Kesinti", location;
				for (int i = 0; i < cutDateList.size(); i++) {
					try {
						operatorName = cutDateList.get(i).attr("data-ilce");
						startDate = cutDateList.get(i).attr("data-tarih");
						List<String> dateParsed = new ArrayList<String>(Arrays.asList(startDate.split("\\.")));
						startDate = ("00" + dateParsed.get(0)).substring(dateParsed.get(0).length()) +
								("00" + dateParsed.get(1)).substring(dateParsed.get(1).length()) + dateParsed.get(2);
						startDate = cutsHelper.formatDate(startDate, "ddMMyyyy", "dd.MM.yyyy");
						endDate = startDate;
						
						Elements cutLocationList = cutDateList.get(i).children();
						cutListStr = cutLocationList.get(0).text();
						cutListStr = cutListStr.replaceAll("\\s+", " ");

						endIndex = cutListStr.toLowerCase(new Locale("tr-TR")).indexOf("saat");
						hourStr = cutListStr.substring(0, endIndex).trim();
						String[] hourArr = hourStr.split(" - ");
						startHour = hourArr[0];
						endHour = hourArr[1];

						location = cutLocationList.get(1).text();

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
			Elements cutItemsList = document.select("table.table-bordered td");

			for (int i = 2; i < cutItemsList.size(); i+=16) {
				try {
					// get value
					String operatorName = cutItemsList.get(i).text();
					String location = cutItemsList.get(i + 3).text();
					String reason = cutItemsList.get(i + 6).text();
					String startDate = cutItemsList.get(i + 9).text();
					String[] dateArr = startDate.split(" - ");
					startDate = dateArr[0];
					startDate = cutsHelper.formatDate(startDate, "d.M.yyyy", "dd.MM.yyyy");
					String startHour = dateArr[1];

					String endDateTime = cutItemsList.get(i + 12).text();
					String endDate = endDateTime.substring(endDateTime.indexOf("olarak ") + 7);
					endDate = endDate.substring(0, endDate.indexOf(" "));
					endDate = cutsHelper.formatDate(endDate, "d.M.yyyy", "dd.MM.yyyy");
					String endHour = endDateTime.substring(endDateTime.indexOf("saat ") + 5);
					endHour = endHour.substring(0, endHour.indexOf(" "));

					Cuts cut = new Cuts();
					cut.setType("w");
					cut.setIconResourceId(R.drawable.water);
					cut.setOperatorName(operatorName);
					cut.setReason(reason);
					cut.setStartDate(startDate + " " + startHour);
					cut.setEndDate(endDate + " " + endHour);
					cut.setLocation(location);
					cut.setDetail(operatorName + ' ' + startDate + "-" + endDate + " " + location + " " + reason);

					// Process a newly found cut
					waterCuts.add(cut);
				} catch (Exception e) {
					sendToGoogleAnalytics(e);
				}
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

		Intent intentToFire = new Intent(CutsConstants.INTENT_CUTS_NOTIFICATION);
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

		String freqPreferenceStr = prefs.getString(CutsConstants.PREF_FREQ, "0");
		freqPreference = Integer.parseInt(freqPreferenceStr);
		searchStringPreference = prefs.getString(
				CutsConstants.PREF_SEARCH_STR_OPTION, "");
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
		String lang = prefs.getString(CutsConstants.PREF_LANG, "tr");
    	Locale appLocale = new Locale(lang);
        Locale.setDefault(appLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = appLocale;
        res.updateConfiguration(conf, dm);
    }
}
