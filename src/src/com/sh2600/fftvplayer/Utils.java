package com.sh2600.fftvplayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Utils {
	
	final static String TAG = Utils.class.getSimpleName();
	
	public static void sendMsg(Context ctx, String msg){
		Log.i(TAG, msg);
		Intent intent = new Intent(MainActivity.BROADCASTRECEIVER_LOG);
		intent.putExtra("msg", msg);
		ctx.sendBroadcast(intent);	
	}
}
