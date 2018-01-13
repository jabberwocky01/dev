/**
 * 
 */
package com.nils.electricitywatercuts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.net.Uri;

/**
 * @author NilS
 *
 */
public final class CutsConstants {
	
    private static final String water_cuts = "iski";
	private static final String europe_electricity_cuts = "bedas";
    private static final String anatolia_electricty_cuts = "ayedas";
    private static final String[] CUTS_LINK_LIST = {"http://www.iski.gov.tr/web/arizaKesinti.aspx", 
    	"https://www.bedas.com.tr/sayfa.asp?mdl=kesinti&sehir=0", "https://www.bedas.com.tr/sayfa.asp?mdl=kesinti&sehir=1",
    	"http://www.ayedas.com.tr/tr-TR/ElektrikDagitimi/Pages/BakimArizaBilgi.aspx"};

	private static final String PREF_RANGE = "pref_cuts_range_option";
	private static final String PREF_FREQ = "pref_cuts_freq_option";
	private static final String PREF_ORDER = "pref_cuts_order_option";

	private static final String PREF_ORDER_CRITERIA = "pref_cuts_order_criteria_option";
	private static final String PREF_SEARCH_STR_OPTION = "pref_cuts_search_str";
	private static final String PREF_LANG = "pref_cuts_lang_option";

	private static final String SETTING_RANGE = "PREF_RANGE";
	private static final String SETTING_FREQ = "PREF_FREQ";
	private static final String SETTING_ORDER = "PREF_ORDER";
	private static final String SETTING_ORDER_CRITERIA = "PREF_ORDER_OPTION";
	private static final String SETTING_SEARCH_STR_OPTION = "PREF_SEARCH_STR_OPTION";
	private static final String SETTING_LANG = "PREF_LANG";

	private static final Uri CONTENT_URI = Uri.parse("content://com.nils.electricitywatercuts/cuts");
	
	private static final String DATABASE_NAME = "electricitywatercuts.db";
	private static final int DATABASE_VERSION = 1;
	private static final String CUTS_TABLE = "electricitywatercuts";

	// Column Names
	private static final String KEY_ID = "_id";
	private static final String KEY_OPERATOR_NAME = "operator_name";
	private static final String KEY_START_DATE = "start_date";	
	private static final String KEY_END_DATE = "end_date";
	private static final String KEY_LOCATION = "location";
	private static final String KEY_REASON = "reason";
	private static final String KEY_DETAIL = "detail";

	private static final String KEY_TYPE = "type";
	private static final String KEY_SEARCH_TEXT = "search_text";
	private static final String KEY_ORDER_START_DATE = "order_start_date";
	private static final String KEY_ORDER_END_DATE = "order_end_date";
	private static final String KEY_INSERT_DATE = "insert_date";
	private static final String KEY_IS_CURRENT = "is_current";

	private static final String INTENT_CUTS_NOTIFICATION = "com.nils.electricitywatercuts.ACTION_REFRESH_CUTS_ALARM";
	private static final String INTENT_CUTS_ORGANIZE_DB = "com.nils.electricitywatercuts.ACTION_ORGANIZE_CUTS_DB_ALARM";
	private static final String INTENT_CUTS_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final String INTENT_CUTS_NOTIFICATION_FLAG = "cutsNotificationFlag";
	private static final String INTENT_CUTS_BOOT_FLAG = "cutsBootFlag";
	private static final String INTENT_CUTS_FREQ = "cutsFreq";

	private static final String DEFAULT_TARGET_URI = "http://goo.gl/jgqzvz";
	//"http://play.google.com/store/apps/details?id=com.nils.electricitywatercuts"
	
	private static final String yyyyMMddTHHmmss = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss";
	private static final String ddMMyyyyHHmm = "dd.MM.yyyy HH:mm";
	private static final String ddMMyyyyHHmmss = "dd.MM.yyyy HH:mm:ss";
	private static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	
	private static char[] turkishChars = new char[] {0x131, 0x130, 0xFC, 0xDC, 0xF6, 0xD6, 0x15F, 0x15E, 0xE7, 0xC7, 0x11F, 0x11E};
	private static char[] englishChars = new char[] {'i', 'I', 'u', 'U', 'o', 'O', 's', 'S', 'c', 'C', 'g', 'G'};
	
	// app rate
    private final static String RATE_DONT_SHOW = "rate_dontshowagain";
    private final static String RATE_REMIND = "rate_remindlater";
    private final static String RATE_CLICKED_RATE = "rate_clickedrated";
    private final static String COUNT_APP_LAUNCH = "app_launch_count";
    private final static String COUNT_REMIND_LAUNCH = "remind_launch_count";
    private final static String COUNT_RATED_LAUNCH = "rated_launch_count";
    private final static String DATE_FIRST_LAUNCH = "app_first_launch";
    private final static String DATE_REMIND_START = "remind_start_date";
    private final static String DATE_RATED_START = "rated_start_date";
   
    private final static int DAYS_FIRST_PROMPT = 7;
    private final static int DAYS_REMIND_PROMPT = 15;
    private final static int DAYS_RATED_PROMPT = 30;

	private final static int LAUNCHES_FIRST_PROMPT = 10;
    private final static int LAUNCHES_REMIND = 15;
    private final static int LAUNCHES_RATED = 25;
    
    private final static String INSTALL_SHORTCUT = "pref_install_shortcut";

	public static String getWaterCuts() {
		return water_cuts;
	}

	public static String getEuropeElectricityCuts() {
		return europe_electricity_cuts;
	}

	public static String getAnatoliaElectrictyCuts() {
		return anatolia_electricty_cuts;
	}
	
    public static String[] getCutsLinkList() {
		return CUTS_LINK_LIST;
	}
	
	public static Uri getContentUri() {
		return CONTENT_URI;
	}
	
	public static String getDatabaseName() {
		return DATABASE_NAME;
	}

	public static int getDatabaseVersion() {
		return DATABASE_VERSION;
	}

	public static String getCutsTable() {
		return CUTS_TABLE;
	}

	public static String getKeyId() {
		return KEY_ID;
	}

	public static String getKeyOperatorName() {
		return KEY_OPERATOR_NAME;
	}

	public static String getKeyStartDate() {
		return KEY_START_DATE;
	}

	public static String getKeyEndDate() {
		return KEY_END_DATE;
	}

	public static String getKeyLocation() {
		return KEY_LOCATION;
	}

	public static String getKeyReason() {
		return KEY_REASON;
	}

	public static String getKeyDetail() {
		return KEY_DETAIL;
	}
	
	public static String getKeyIsCurrent() {
		return KEY_IS_CURRENT;
	}

	public static String getKeyType() {
		return KEY_TYPE;
	}
	
	public static String getPrefRange() {
		return PREF_RANGE;
	}

	public static String getPrefFreq() {
		return PREF_FREQ;
	}
	
	public static String getPrefOrder() {
		return PREF_ORDER;
	}
	
	public static String getPrefOrderCriteria() {
		return PREF_ORDER_CRITERIA;
	}
	
	public static String getPrefSearchStrOption() {
		return PREF_SEARCH_STR_OPTION;
	}
	
	public static String getPrefLang() {
		return PREF_LANG;
	}
	
	public static String getSettingRange() {
		return SETTING_RANGE;
	}
	
	public static String getSettingFreq() {
		return SETTING_FREQ;
	}

	public static String getSettingOrder() {
		return SETTING_ORDER;
	}

	public static String getSettingOrderCriteria() {
		return SETTING_ORDER_CRITERIA;
	}

	public static String getSettingSearchStrOption() {
		return SETTING_SEARCH_STR_OPTION;
	}
	
	public static String getSettingLang() {
		return SETTING_LANG;
	}
	
	public static String getIntentCutsNotification() {
		return INTENT_CUTS_NOTIFICATION;
	}

	public static String getIntentCutsOrganizeDb() {
		return INTENT_CUTS_ORGANIZE_DB;
	}
	
	public static String getIntentCutsBootCompleted() {
		return INTENT_CUTS_BOOT_COMPLETED;
	}
	
	public static String getIntentCutsNotificationFlag() {
		return INTENT_CUTS_NOTIFICATION_FLAG;
	}
	
	public static String getIntentCutsBootFlag() {
		return INTENT_CUTS_BOOT_FLAG;
	}
	
	public static String getIntentCutsFreq() {
		return INTENT_CUTS_FREQ;
	}
	
	public static String getKeySearchText() {
		return KEY_SEARCH_TEXT;
	}
	
	public static String getKeyOrderStartDate() {
		return KEY_ORDER_START_DATE;
	}

	public static String getKeyOrderEndDate() {
		return KEY_ORDER_END_DATE;
	}
	
	public static String getKeyInsertDate() {
		return KEY_INSERT_DATE;
	}
	
	public static String getDefaultTargetUri() {
		return DEFAULT_TARGET_URI;
	}
	
	public static String getYyyymmddthhmmss() {
		return yyyyMMddTHHmmss;
	}

	public static String getDdmmyyyyhhmm() {
		return ddMMyyyyHHmm;
	}
	
	public static String getDdmmyyyyhhmmss() {
		return ddMMyyyyHHmmss;
	}
	
	public static String getYyyymmddHhmmss() {
		return yyyyMMdd_HHmmss;
	}
	
	public static String getYyyymmddhhmmss() {
		return yyyyMMddHHmmss;
	}
	
    public static String getRateDontShow() {
		return RATE_DONT_SHOW;
	}

	public static String getRateRemind() {
		return RATE_REMIND;
	}

	public static String getRateClickedRate() {
		return RATE_CLICKED_RATE;
	}

	public static String getCountAppLaunch() {
		return COUNT_APP_LAUNCH;
	}

	public static String getCountRemindLaunch() {
		return COUNT_REMIND_LAUNCH;
	}

	public static String getCountRatedLaunch() {
		return COUNT_RATED_LAUNCH;
	}

	public static String getDateFirstLaunch() {
		return DATE_FIRST_LAUNCH;
	}

	public static String getDateRemindStart() {
		return DATE_REMIND_START;
	}

	public static String getDateRatedStart() {
		return DATE_RATED_START;
	}

	public static int getDaysFirstPrompt() {
		return DAYS_FIRST_PROMPT;
	}

	public static int getDaysRemindPrompt() {
		return DAYS_REMIND_PROMPT;
	}

	public static int getDaysRatedPrompt() {
		return DAYS_RATED_PROMPT;
	}

	public static int getLaunchesFirstPrompt() {
		return LAUNCHES_FIRST_PROMPT;
	}

	public static int getLaunchesRemind() {
		return LAUNCHES_REMIND;
	}

	public static int getLaunchesRated() {
		return LAUNCHES_RATED;
	}
	
    public static String getInstallShortcut() {
		return INSTALL_SHORTCUT;
	}
	
	public static String convertToTurkishChars(String str) {
		String ret = str;
		for (int i = 0; i < turkishChars.length; i++) {
			ret = ret.replaceAll(new String(new char[]{englishChars[i]}), new String(new char[]{turkishChars[i]}));
		}
		return ret;
	}
	
	public static String convertContentToTurkish(String context) {
        context = context.replaceAll("&#304;", "İ");
        context = context.replaceAll("&#305;", "ı");
        context = context.replaceAll("&#214;", "Ö");
        context = context.replaceAll("&#246;", "ö");
        context = context.replaceAll("&#220;", "Ü");
        context = context.replaceAll("&#252;", "ü");
        context = context.replaceAll("&#199;", "Ç");
        context = context.replaceAll("&#231;", "ç");
        context = context.replaceAll("&#286;", "Ğ");
        context = context.replaceAll("&#287;", "ğ");
        context = context.replaceAll("&#350;", "Ş");
        context = context.replaceAll("&#351;", "ş");
	    return context;
	}
	
	public static boolean compareCutsStr(String str1, String str2) {
		String lowerCaseStr1 = str1.toLowerCase(new Locale("tr-TR"));
		String lowerCaseStr2 = str2.toLowerCase(new Locale("tr-TR"));
		if (lowerCaseStr1.contains(lowerCaseStr2))
			return true;
		
		String trStr2 = convertToTurkishChars(lowerCaseStr2);
		if (lowerCaseStr1.contains(trStr2))
			return true;
		return false;
	}
	
	public static String formatDate(String dateStr, String inputFormat, String outputFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(inputFormat, new Locale("tr-TR"));
		SimpleDateFormat output = new SimpleDateFormat(outputFormat, new Locale("tr-TR"));
		String formattedTime;
		try {
			Date d = sdf.parse(dateStr);
			formattedTime = output.format(d);
		} catch (ParseException e) {
			formattedTime = dateStr;
		}

		return formattedTime;
	}
	
}
