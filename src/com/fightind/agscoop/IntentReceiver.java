package com.fightind.agscoop;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {

	private final static String TAG = "IntentReceiver";
	
	//private Custom mLog;

	//private static Object mStartingServiceSync = new Object();
	//private static WakeLock mWakeService;
	private static Context mCtx;

	public void onReceive(Context context, Intent intent) {
	//Log.i(TAG,"onReceive(Action Received:"+intent.getAction()+") ++++++++++++++++++++++++++++++++++++++++++++++++");
		onReceiveWithPrivilege(context, intent, false);
		return;
	}
		
	
	Handler serviceStart = new Handler(){
		public void handleMessage(Message msg){
			Thread tx = new Thread(){
				public void run(){
					SharedPreferences mReg = mCtx.getSharedPreferences("Preferences", mCtx.MODE_WORLD_WRITEABLE);
					int interval = mReg.getInt("interval", 1);
					if(interval != 0){
						String intervalText = "";
						if(interval == 1){ intervalText = "5 Hour Interval"; }
						else if(interval == 2){ intervalText = "Daily Interval"; }
						else if(interval >= 10){ intervalText = interval + " Minute Interval"; }
						Log.w(TAG,"Automatic Service " + intervalText );
						Intent service = new Intent(); 
						service.setClass(mCtx, AutomaticService.class);
				    	mCtx.stopService(service);
				    	mCtx.startService(service);   
					}else{ Intent service = new Intent();service.setClass(mCtx, AutomaticService.class); mCtx.stopService(service); }
				}
			};
			tx.start();
		
		}
	};	
	
	protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
		//mLog = new Custom(context, TAG + " onReceiveWithPrivilege() 27");
		String action = intent.getAction();
		Log.i(TAG,"onReceiveWithPrivilege(Action Received:"+action+") ++++++++++++++++++++++++++++++++++++++++++++++++");
		
		mCtx = context;

		serviceStart.sendEmptyMessage(2);
				
		Log.w(TAG,"onReceiveWithPrivilege(Action Received:"+action+")");
		
		
	}

	/*
	public static void beginHostingService(Context context, Intent intent) {
		//Custom mLog = new Custom(context, TAG + " beginHostingService() 42");
		//android.intent.action.BOOT_COMPLETED
		
		Log.i(TAG,"beginHostingService() ++++++++++++++++++++++++++++++++++++++++++++++++");
		
		mCtx = context;
		//synchronized (mStartingServiceSync){
			Log.i(TAG,"beginHostingService() synchronized() ++++++++++++++++++++++++++++++++++++++++++++++++");
			if(mWakeService == null){
				Log.i(TAG,"beginHostingService() PowerManager ++++++++++++++++++++++++++++++++++++++++++++++++");
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				mWakeService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StartingSyncService");
				mWakeService.setReferenceCounted(false);
			}
			Log.i(TAG,"beginHostingService() WakeService.acquire() ++++++++++++++++++++++++++++++++++++++++++++++++");
			mWakeService.acquire();

			Log.i(TAG,"beginHostingService() startService() ++++++++++++++++++++++++++++++++++++++++++++++++");
			mCtx.startService(intent);
		//}
	}
	/*
	public static void finishHostingService(AutomaticService service, int serviceId) {
		
		//Custom mLog = new Custom(service.getApplicationContext(), TAG + " finishHostingService() 66");
		Log.i(TAG,"finishHostingService() ++++++++++++++++++++++++++++++++++++++++++++++++");

		synchronized (mStartingServiceSync){
			if(mStartingServiceSync != null){
				Log.i(TAG,"finishHostingService() stop Self Result");
				if( service.stopSelfResult(serviceId) ){
					Log.i(TAG,"finishHostingService() release Wake Service");
					mWakeService.release();
				}
			}
		}
		
	}//*/


}
