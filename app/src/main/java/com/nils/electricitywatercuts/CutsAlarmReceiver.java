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
	    
	       if (intent.getAction().equals(CutsConstants.getIntentCutsNotification())) {
		   	    Intent startIntent = new Intent(context, CutsUpdateService.class);
		   	    startIntent.putExtra(CutsConstants.getIntentCutsNotificationFlag(), true);
			    context.startService(startIntent);
	        } else if(intent.getAction().equals(CutsConstants.getIntentCutsBootCompleted())) {
		   	    Intent startIntent = new Intent(context, CutsUpdateService.class);
		   	    startIntent.putExtra(CutsConstants.getIntentCutsBootFlag(), true);
			    context.startService(startIntent);
	        } 
//	        else if(intent.getAction().equals(CutsConstants.getIntentCutsOrganizeDb())) {
//	    	    Intent startIntent = new Intent(context, OrganizeCutsDatabaseService.class);
//	    	    context.startService(startIntent);
//	        }
	  }

}
