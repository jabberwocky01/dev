/**
 * 
 */
package com.nils.electricitywatercuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author NilS
 *
 */

public class CutsAlarmReceiver extends BroadcastReceiver {
	 
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    
	       if (intent.getAction().equals(CutsConstants.INTENT_CUTS_NOTIFICATION)) {
		   	    Intent startIntent = new Intent(context, CutsUpdateService.class);
		   	    startIntent.putExtra(CutsConstants.INTENT_CUTS_NOTIFICATION_FLAG, true);
			    context.startService(startIntent);
	        } else if(intent.getAction().equals(CutsConstants.INTENT_CUTS_BOOT_COMPLETED)) {
		   	    Intent startIntent = new Intent(context, CutsUpdateService.class);
		   	    startIntent.putExtra(CutsConstants.INTENT_CUTS_BOOT_FLAG, true);
			    context.startService(startIntent);
	        } 
//	        else if(intent.getAction().equals(CutsConstants.getIntentCutsOrganizeDb())) {
//	    	    Intent startIntent = new Intent(context, OrganizeCutsDatabaseService.class);
//	    	    context.startService(startIntent);
//	        }
	  }

}
