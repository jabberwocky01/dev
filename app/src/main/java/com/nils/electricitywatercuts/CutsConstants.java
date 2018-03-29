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
public class CutsConstants {
	
    public static final String water_cuts = "iski";
	public static final String europe_electricity_cuts = "bedas";
	public static final String anatolia_electricty_cuts = "ayedas";
	public static final String[] CUTS_LINK_LIST = {"http://www.iski.gov.tr/web/tr-TR/ariza-kesinti",
    	"https://www.bedas.com.tr/kesinti.asp?ilce=%s&tip=%s&tarih=%s",
    	"https://www.ayedas.com.tr/Pages/Bilgilendirme/PlanliBakim/Planli-Kesinti-Listesi-ve-Haritasi.aspx"};

    public static final String BEDAS_CUT_TYPE_PLANNED = "0";
	public static final String BEDAS_CUT_TYPE_INSTANTANEOUS = "1";

	public static final String PREF_RANGE = "pref_cuts_range_option";
	public static final String PREF_FREQ = "pref_cuts_freq_option";
	public static final String PREF_ORDER = "pref_cuts_order_option";

	public static final String PREF_ORDER_CRITERIA = "pref_cuts_order_criteria_option";
	public static final String PREF_SEARCH_STR_OPTION = "pref_cuts_search_str";
	public static final String PREF_LANG = "pref_cuts_lang_option";

	public static final String SETTING_RANGE = "PREF_RANGE";
	public static final String SETTING_FREQ = "PREF_FREQ";
	public static final String SETTING_ORDER = "PREF_ORDER";
	public static final String SETTING_ORDER_CRITERIA = "PREF_ORDER_OPTION";
	public static final String SETTING_SEARCH_STR_OPTION = "PREF_SEARCH_STR_OPTION";
	public static final String SETTING_LANG = "PREF_LANG";

	public static final Uri CONTENT_URI = Uri.parse("content://com.nils.electricitywatercuts/cuts");

	public static final String DATABASE_NAME = "electricitywatercuts.db";
	public static final int DATABASE_VERSION = 1;
	public static final String CUTS_TABLE = "electricitywatercuts";

	// Column Names
	public static final String KEY_ID = "_id";
	public static final String KEY_OPERATOR_NAME = "operator_name";
	public static final String KEY_START_DATE = "start_date";
	public static final String KEY_END_DATE = "end_date";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_REASON = "reason";
	public static final String KEY_DETAIL = "detail";

	public static final String KEY_TYPE = "type";
	public static final String KEY_SEARCH_TEXT = "search_text";
	public static final String KEY_ORDER_START_DATE = "order_start_date";
	public static final String KEY_ORDER_END_DATE = "order_end_date";
	public static final String KEY_INSERT_DATE = "insert_date";
	public static final String KEY_IS_CURRENT = "is_current";

	public static final String INTENT_CUTS_NOTIFICATION = "com.nils.electricitywatercuts.ACTION_REFRESH_CUTS_ALARM";
	public static final String INTENT_CUTS_ORGANIZE_DB = "com.nils.electricitywatercuts.ACTION_ORGANIZE_CUTS_DB_ALARM";
	public static final String INTENT_CUTS_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	public static final String INTENT_CUTS_NOTIFICATION_FLAG = "cutsNotificationFlag";
	public static final String INTENT_CUTS_BOOT_FLAG = "cutsBootFlag";
	public static final String INTENT_CUTS_FREQ = "cutsFreq";

	public static final String DEFAULT_TARGET_URI = "http://goo.gl/jgqzvz";
	//"http://play.google.com/store/apps/details?id=com.nils.electricitywatercuts"

	public static final String yyyyMMddTHHmmss = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss";
	public static final String ddMMyyyyHHmm = "dd.MM.yyyy HH:mm";
	public static final String ddMMyyyyHHmmss = "dd.MM.yyyy HH:mm:ss";
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

	public static char[] turkishChars = new char[] {0x131, 0x130, 0xFC, 0xDC, 0xF6, 0xD6, 0x15F, 0x15E, 0xE7, 0xC7, 0x11F, 0x11E};
	public static char[] englishChars = new char[] {'i', 'I', 'u', 'U', 'o', 'O', 's', 'S', 'c', 'C', 'g', 'G'};
	
	// app rate
	public final static String RATE_DONT_SHOW = "rate_dontshowagain";
	public final static String RATE_REMIND = "rate_remindlater";
	public final static String RATE_CLICKED_RATE = "rate_clickedrated";
	public final static String COUNT_APP_LAUNCH = "app_launch_count";
	public final static String COUNT_REMIND_LAUNCH = "remind_launch_count";
	public final static String COUNT_RATED_LAUNCH = "rated_launch_count";
	public final static String DATE_FIRST_LAUNCH = "app_first_launch";
	public final static String DATE_REMIND_START = "remind_start_date";
	public final static String DATE_RATED_START = "rated_start_date";

	public final static int DAYS_FIRST_PROMPT = 7;
	public final static int DAYS_REMIND_PROMPT = 15;
	public final static int DAYS_RATED_PROMPT = 30;

	public final static int LAUNCHES_FIRST_PROMPT = 10;
	public final static int LAUNCHES_REMIND = 15;
	public final static int LAUNCHES_RATED = 25;

	public final static String INSTALL_SHORTCUT = "pref_install_shortcut";
	
}
