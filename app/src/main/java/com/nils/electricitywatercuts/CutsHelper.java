/**
 * 
 */
package com.nils.electricitywatercuts;

import android.net.Uri;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author NilS
 *
 */
public class CutsHelper {

	public String convertToTurkishChars(String str) {
		String ret = str;
		for (int i = 0; i < CutsConstants.turkishChars.length; i++) {
			ret = ret.replaceAll(new String(new char[]{CutsConstants.englishChars[i]}),
					new String(new char[]{CutsConstants.turkishChars[i]}));
		}
		return ret;
	}
	
	public String convertContentToTurkish(String context) {
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
	
	public boolean compareCutsStr(String str1, String str2) {
		String lowerCaseStr1 = str1.toLowerCase(new Locale("tr-TR"));
        String lowerCaseStr2 = str2.toLowerCase(new Locale("tr-TR"));

		String normalizedStr1 = Normalizer.normalize(lowerCaseStr1, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		String normalizedStr2 = Normalizer.normalize(lowerCaseStr2, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

		if (normalizedStr1.toLowerCase().contains(normalizedStr2.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	public String formatDate(String dateStr, String inputFormat, String outputFormat) {
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
