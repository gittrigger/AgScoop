package com.fightind.agscoop;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class AutomaticService extends Service implements Runnable {

	private static String TAG = "Service";
	
	private Handler mHandler;
	private NotificationManager mNM;
	private Context mCtx;
	private int pRate = 72;
	@Override
	public void onCreate() {
		super.onCreate();
		mCtx = this.getApplicationContext();
		//mLog.w(TAG,"onCreate() ++++++++++++++++++++++++++++++++++");
		
        mHandler = new Handler();
        
        Thread thr = new Thread(null, this, TAG + "_service_thread");
        thr.start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		//mLog.w(TAG,"onBind() ++++++++++++++++++++++++++++++++++");
		return null;
	}

	public void run() {
		//mLog.w(TAG,"run() ++++++++++++++++++++++++++++++++++");
		getlatest();
	}

	@Override
	public void onDestroy() {
		Log.w(TAG,"onDestroy() ++++++++++++++++++++++++++++++++++");
		mNM.cancelAll();
		super.onDestroy();
	}


	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.w(TAG,"onStart() ++++++++++++++++++++++++++++++++++");
	}

	
	
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	
	private void getlatest() {
		//mLog = new Custom(this);
		Log.w(TAG,"getlatest() ++++++++++++++++++++++++++++++++++");
		
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		//mLog.setNotificationManager(mNM);
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		mPreferencesEditor = mSharedPreferences.edit();
		//mLog.setSharedPreferences(mSharedPreferences,mPreferencesEditor);
  	  	
		mNM.cancelAll();
		
        //String mUuid = UUID.randomUUID().toString();
        //content://settings/system/notification_sound
        //for (Account account1 : accountsWithNewMail.keySet()) { if (account1.isVibrate()) vibrate = true; ringtone = account1.getRingtone(); }

        //BASEURL = "http://www.seashepherd.org/news-and-media/sea-shepherd-news/feed/rss.html";
        //BASEURL = "http://www.whitehouse.gov/blog/";
        // This will block until load is low or time limit exceeded
		
	
		

		Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("title", Motion.title); bl.putString("dest", Motion.dest); bl.putString("storloc", "bucket"); ml.setData(bl); 
		
		int interval = mSharedPreferences.getInt("interval",1);
		long ls = mSharedPreferences.getLong("bucket_saved",(long)0);
        //mLog.loadLimit(TAG + " getlatest() 107", syncLoad, 5 * 1000, 30 * 1000);
		Date d = new Date();
		if( interval == 0 ){
			Log.w(TAG,"Not Automatic");return;
		}else if( interval == 1 ){
			
			if( d.getHours() == 0 || d.getHours() == 5 || d.getHours() == 10 || d.getHours() == 15 || d.getHours() == 20 ){
				if( ls < System.currentTimeMillis() - 5 * 60 * 60 * 1000 ){
					getlist.sendMessage(ml);
				}else{Log.w(TAG,"recent " + (System.currentTimeMillis() - ls)/60000 + " minutes");}
			}else{Log.w(TAG,"wrong hour " + d.getHours());}
		}else if( interval == 2 ){
			
			if( d.getHours() == 0 || ls < System.currentTimeMillis() - 24 * 60 * 60 * 1000 ){
				getlist.sendMessage(ml);
			}else{Log.w(TAG,"recent " + (System.currentTimeMillis() - ls)/60000 + " minutes or hour("+d.getHours()+") != 0");}
		}else if( interval >= 10 ){
		
			if( d.getHours() == 0 || ls < System.currentTimeMillis() - interval * 60000 ){
				getlist.sendMessage(ml);
			}else{Log.w(TAG,"recent interval("+interval+") > " + (System.currentTimeMillis() - ls)/60000 + " minutes or hour("+d.getHours()+") != 0");}
		}
		
		
		
		
		mHandler.postDelayed(this, 600000);
        
		AlarmManager mAlM = (AlarmManager) mCtx.getSystemService(mCtx.ALARM_SERVICE);
		Intent resetservice = new Intent();
		resetservice.setAction(Motion.recoveryintent);
		PendingIntent service4 = PendingIntent.getBroadcast(mCtx, 80, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
		Date d4 = new Date();
		d4.setSeconds(0);d4.setMinutes(0);d4.setHours(d4.getHours()+1);Log.w(TAG,"Scheduling recovery at the top of the hour("+(d4.getHours())+") with("+d4.getTime()+") valence("+(d4.getTime()-System.currentTimeMillis())/1000/60+" m)");
		mAlM.set(AlarmManager.RTC_WAKEUP, d4.getTime(), service4);
		
		
	}
	
	

	Handler getlist = new Handler(){
    	private long hover = 0;
    	public void handleMessage(Message msg){
    		if(hover > SystemClock.uptimeMillis()){
    			Log.i(TAG,"Started 10>" + (hover - SystemClock.uptimeMillis())/1000 + " seconds ago.");return;
    		}
    		hover = SystemClock.uptimeMillis() + 10000;
    		final Bundle bdl = msg.getData();
    		Log.i(TAG,"getlist");
    		
    		
    		Thread tx = new Thread(){
    			public void run(){
    				long st = System.currentTimeMillis();
    					SharedPreferences mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
            Editor mEdt = mReg.edit();
    					{Message ml = new Message(); Bundle bl = new Bundle(bdl); ml.setData(bl); mGet.sendMessage(ml);}
    					//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Running"); ml.setData(bl); setText.sendMessage(ml);}
    				
    					
    					try {
							long freememory = 0;
    						for(int i = 0; i < 300; i++){	
    							freememory = Runtime.getRuntime().freeMemory();
								Log.i(TAG,"processing [" + i + "] freememory("+freememory+")");
								
								if(st > mReg.getLong("bucket_saved", 0) && st > mReg.getLong("error", 0)){
							//		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Running " + ((System.currentTimeMillis() - st)/1000) + " seconds."); ml.setData(bl); setText.sendMessageDelayed(ml,pRate);}
									Thread.sleep(750);
									continue;
								}else{
									
									
								}
								break;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				
						if( st < mReg.getLong("bucket_saved", 0) ){
							Date d = new Date(mReg.getLong("bucket_saved", 0));
							String ct = mReg.getString("bucket", "");
							
							//
							// CUSTOMIZED
							//
							String[] ctl = ct.split("<item>");
							// EC
							
							//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Downloaded " + (ct.length()>1024?ct.length()/1024+"Kb":ct.length()+"b")+" at " + (d.getHours() > 12?d.getHours()-12:d.getHours()) + ":" + (d.getMinutes()<10?"0":"") + d.getMinutes() + "\nContaining " + ctl.length); ml.setData(bl); setText.sendMessage(ml);}
							//ct = ct.replace("\r", "\n");
							if(ctl.length > 0){
							
								//mProgressDialog = ProgressDialog.show(mCtx, "Gratuitous Notification", "Loading", true);
								//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("max",ctl.length); bl.putString("titleOFF", "Notification"); bl.putBoolean("indeter", true); bl.putString("text", "Loading"); ml.setData(bl); mProgress.sendMessage(ml);}
								//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("max",ctl.length); ml.setData(bl); mProgressMax.sendMessage(ml);}
							Cursor cx=null;
							int found = 0;
							int foundnew = 0;
							Uri contentpath = Uri.withAppendedPath(DataProvider.CONTENT_URI,"moment");
							ContentValues cv = new ContentValues();
							String title, published, content, url;Uri geturi;
							for(int b = 0; b < ctl.length; b++){if(ctl[b].length() == 0){continue;}//Log.i(G,ctl[b]+"<------");
								
							
								//
								// CUSTOMIZED
								//
								title = ctl[b].substring(ctl[b].indexOf("<title>")+7, ctl[b].indexOf("</title>"));
								if(ctl[b].indexOf("<pubDate>") > 0){published = fixDate(ctl[b].substring(ctl[b].indexOf("<pubDate>")+9, ctl[b].indexOf("</pubDate>")));}else{published = "unavail";}
								url = ctl[b].substring(ctl[b].indexOf("<link>")+6, ctl[b].indexOf("</link>"));
								if(ctl[b].indexOf("<description>") > 0){content = ctl[b].substring(ctl[b].indexOf("<description>")+13, ctl[b].indexOf("</description>"));}else{content="unavail";}
								title = title.replaceAll("\"", "&quote;");
								// EC
								
								//DEBUG
								//Log.w(G,"thread("+this.getId()+") title("+title+") published("+published+") author(unavil) url("+url+") content("+content+")");
								
								found = 0;
								
								//
								// CUSOMIZED
								//
								cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"_id"}, "title = \""+title+"\" AND published = \""+published+"\" AND url = \""+url+"\" AND status > 0", null, null);
				     			// EC
								
								if( cx != null){if( cx.moveToFirst() ){ if( cx.getInt(0) > 0){ found=cx.getInt(0);}; } cx.close();}
								if(found == 0){
									foundnew ++;
									cv.put("title", title);
									cv.put("url", url);
									cv.put("published", published);
									cv.put("content", content);
									//cv.put("author", author);
									geturi = SqliteWrapper.insert(mCtx, mCtx.getContentResolver(), contentpath, cv);
									Log.w(TAG,"Inserted New " + geturi.toString());
									
									//
									// CUSOMIZED
									//
									content = content.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
									content = content.replaceAll("<br>", "");
									// EC
									if(foundnew == 1){setEntryNotification(TAG + " getlist", geturi, title, content);}
									if(foundnew >= Motion.naturalLimit){Log.w(TAG,"Natural Limit of import reached at " + foundnew);}
								}else{geturi = Uri.withAppendedPath(contentpath, "#"+found);}
								//if( geturi != null && ((found==0) || (foundnew > 39 && b == ctl.length-1) )){
									
								//}
								
								//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("progress",b+1); ml.setData(bl); mProgressPlus.sendMessage(ml);}
							}
							//mProgressOut.sendEmptyMessage(2);
							//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Downloaded " + (ct.length()>1024?ct.length()/1024+"Kb":ct.length()+"b")+" at " + (d.getHours() > 12?d.getHours()-12:d.getHours()) + ":" + (d.getMinutes()<10?"0":"") + d.getMinutes() + "\nContaining " + ctl.length + ", " + foundnew + " New"); ml.setData(bl); setText.sendMessage(ml);}
							}
						}
						if( st < mReg.getLong("error", 0) ){
							//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", mReg.getString("errortype", "")); ml.setData(bl); setText.sendMessage(ml);}
						}
						
						
					
    			}
    		};
    		tx.start();
    	}	
    };

public void setEntryNotification(String who, Uri geturi, String title, String summary){
		
		int syncvib = mSharedPreferences.contains("syncvib") ? mSharedPreferences.getInt("syncvib",1) : 1;
		
		//
		// CUSOMIZED
		//
		Notification notif = new Notification(Motion.notifyimage, title + " -- " + summary, System.currentTimeMillis()); // This text scrolls across the top.
		Intent intentJump2 = new Intent(mCtx, com.fightind.agscoop.Lookup.class);
		// EC
		intentJump2.putExtra("uri", geturi);
        PendingIntent pi2 = PendingIntent.getActivity(this, 0, intentJump2, Intent.FLAG_ACTIVITY_NEW_TASK );
        //PendingIntent pi2 = PendingIntent.getActivity(mContext, 0, intentJump2, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
        
        //if( syncvib != 3 ){ // NOT OFF
        	//notif.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
        //}else{
        	notif.defaults = Notification.DEFAULT_LIGHTS;
        //}
        
		notif.setLatestEventInfo(mCtx, title, summary, pi2); // This Text appears after the slide is open
		
		switch (syncvib) {
		case 1: // ++_+
			notif.vibrate = new long[] { 100, 200, 100, 200, 500, 200 };
			break;
		case 3: // None _
			break;
		case 2: // ++
			notif.vibrate = new long[] { 100, 200, 100, 200 };
			break;
		case 4: // +_++
			notif.vibrate = new long[] { 100, 200, 500, 200, 100, 200 };
			break;
		}
        mNM.notify(1, notif);
	}

    
    private DefaultHttpClient mHC;
	public Handler mGet = new Handler(){
		public void handleMessage(Message msg){Bundle bx = msg.getData();mget2(bx);}
		private void mget2(final Bundle bx){if(mHC == null){mHC = new DefaultHttpClient();}
		final String dest = bx.getString("dest");
		final String loc = bx.getString("storloc");
		final String titlr = bx.getString("title");final String procg = bx.getString("procg");
	
		if( dest == null || dest.length() == 0 ){
			Log.e(TAG,"Blocked empty get request: Destination titled " + titlr + " intended to " + loc);
			return;
		}
		
		Thread mt = new Thread(){
			
			public void run(){SharedPreferences mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
			/*if(mReg.contains(loc) && mReg.contains(loc+U_SAVED)){
				if( mReg.getLong(loc+U_SAVED, 10) > (System.currentTimeMillis()-bx.getLong("age",18000))){
					{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("title",procg + " " + titlr);bxb.putString("subtitle",(int)(System.currentTimeMillis() - mReg.getLong(loc+U_SAVED, 33))/1000+" Second Cache " + titlr +" for "+loc+".\n"+dest ); mxm.setData(bxb);easyViewerHandler.sendMessageDelayed(mxm,10);}
					{Message mxx = new Message();mxx.setData(bx);taskDone.sendMessageDelayed(mxx, 30);}
					return;
				}
			}//*/
			
			safeHttpGet("mGet2",bx);
			
			//{Message mxx = new Message();mxx.setData(bx);taskDone.sendMessageDelayed(mxx, 30);}
	
			}};mt.start();		
	}
	public void safeHttpGet(String who, final Bundle bdl) {
	
		Thread tx = new Thread(){public void run(){
			final String dest = bdl.getString("dest");String who = bdl.getString("who");
			final String loc = bdl.getString("storloc");
			final String titlr = bdl.getString("title");String procg = bdl.getString("procg");
	
			
			Log.i(TAG,"Acquiring " + titlr +"\n"+dest);
			//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("title",procg);bxb.putString("subtitle", ); mxm.setData(bxb);easyViewerHandler.sendMessageDelayed(mxm,10);}
			
			
		final long sh = SystemClock.uptimeMillis();
		HttpGet httpget = new HttpGet(dest);
		String mUrl = httpget.getURI().toString();
		
		//Log.w(G,"safeHttpGet() 1033 getURI("+httpget.getURI()+") for " + who);
		if( httpget.getURI().toString() == "" ){
			Log.i(TAG,"Blocked empty destination get.");
			return;
		}
		
		String responseCode = ""; //String mHP = "";
		//CookieStore c = mHC.getCookieStore();
		//mHC = new DefaultHttpClient();mHC.setCookieStore(c);
		CookieStore cs = (mHC != null) ? mHC.getCookieStore(): new DefaultHttpClient().getCookieStore();
		DefaultHttpClient mHC = new DefaultHttpClient();
		SharedPreferences mReg = mCtx.getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);Editor mEdt = mReg.edit();
		String cshort = mReg.getString("lastcookies","");
		String[] clist = cshort.split("\n");ContentValues cg = new ContentValues();
		for(int h=0; h < clist.length; h++){
			String[] c = clist[h].split(" ",2);
			if(c.length == 2 && c[0].length() > 3){if(cg.containsKey(c[0]) == false){
				//cg.put(c[0], c[1]);
				Cookie logonCookie = new BasicClientCookie(c[0], c[1].replaceAll("; expires=null", ""));
				//Log.w(G,"Carry Cookie mGet2 " + c[0] + ":"+c[1] + " expires("+logonCookie.getExpiryDate()+")" + " path("+logonCookie.getPath()+") domain("+logonCookie.getDomain()+")");
				cs.addCookie(logonCookie);//TODO
			}}
		}
		
		mHC.setCookieStore(cs);
		
		try {
			
			
			
			mHC.setRedirectHandler(new RedirectHandler(){
	
				public URI getLocationURI(HttpResponse arg0,
						HttpContext arg1) throws ProtocolException {
					
					if( arg0.containsHeader("Location")){
					String url = arg0.getFirstHeader("Location").getValue();
					//Log.w(G,"getLocationURI url("+url+")  " + arg0.getStatusLine().getReasonPhrase() + ": " + arg1.toString());
					//mUrl = url;mUrl("+mUrl+")
					URI uri = URI.create(url);
					
					{Message mxm = new Message(); Bundle bxb = new Bundle();bxb.putString("string", loc+"url");bxb.putString(loc+"url",url );mxm.setData(bxb);setrefHandler.sendMessageDelayed(mxm,10);}
					//mEdt.putString(loc+"url", url); mEdt.commit();
					return uri;
					}else{
						return null;
					}
				}
	
				public boolean isRedirectRequested(HttpResponse arg0,
						HttpContext arg1) {//Log.w(G,"isRedirectRequested " + arg0.getStatusLine().getReasonPhrase() + ": " + arg1.toString() + " ");
						if( arg0.containsHeader("Location") ){
							String url = arg0.getFirstHeader("Location").getValue();
							{Message mxm = new Message(); Bundle bxb = new Bundle();bxb.putString("string", loc+"url");bxb.putString(loc+"url",url );mxm.setData(bxb);setrefHandler.sendMessageDelayed(mxm,10);}
							//Log.w(G,"isRedirectRequested url(" + url+") ");
							//mEdt.putString(loc+"url", url); mEdt.commit();
							return true;
						}
					return false;
				}
				
			});
	
			//{Message mxm = new Message(); Bundle bxb = new Bundle();bxb.putString("string", loc+"url");bxb.putString(loc+"url",mUrl );mxm.setData(bxb);setrefHandler.sendMessageDelayed(mxm,1);}
			//Log.w(G,"safeHttpGet() 1044 httpclient.execute() mUrl("+mUrl+") for " + who);
			//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("text","Downloading"); mxm.setData(bxb);easyStatusHandler.sendMessageDelayed(mxm,10);}
			HttpResponse mHR = mHC.execute(httpget);
			//reply[2] = mReg.getString(loc+"url", mUrl);mUrl = reply[2];
	
			if( mHR != null ){
		        Log.w(TAG,"safeHttpGet() 436 " + mHR.getStatusLine() + " " + " for " + who);
				//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("text","Server says "+mHR.getStatusLine().getStatusCode() + " "+mHR.getStatusLine().getReasonPhrase()); mxm.setData(bxb);easyStatusHandler.sendMessageDelayed(mxm,10);}
				//easyStatus(mHR.getStatusLine().getStatusCode() + " " + mHR.getStatusLine().getReasonPhrase());
				
		        Log.w(TAG,"safeHttpGet() 440 response.getEntity() for " + who);
		        HttpEntity mHE = mHR.getEntity();
	
		        if (mHE != null) {
			        //byte[] bytes = ;
		        	Log.w(TAG,"Downloaded into RAM " + mHE.getContentLength());
		        	Log.w(TAG,"safeHttpGet() 445 byte[] to EntityUtils.toByteArray(mHE) expect 448");
		        	String mhpb = EntityUtils.toString(mHE);
		        	Log.w(TAG,"safeHttpGet() 448 mhpb("+mhpb.length()+") to String for " + who);
		        	{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("title", titlr); bl.putString("murl", mUrl); bl.putLong("startdl", sh); bl.putString("statusline", mHR.getStatusLine().getReasonPhrase());bl.putString("dest", dest); bl.putString("storloc", loc); bl.putString("mhpb", mhpb); bl.putString("pageconnectknow", bdl.getString("pageconnectknow")); ml.setData(bl); storePage.sendMessage(ml);}
		        	
		        	//mhpb = null;
		        	//mHP = new String(EntityUtils.toByteArray(mHE));
			            
		        //Log.w(G,"safeHttpGet() 1056 ");
			        final CookieStore cs2 = mHC.getCookieStore();
			        //Thread tc = new Thread(){public void run(){
			        //mHO = mHC.getCookieStore().getCookies();
			        
			      	List<Cookie> cl2 = cs2.getCookies();
			      	
			      	Bundle co = new Bundle();String cshort2 = "";
			      	for(int i = cl2.size()-1; i >= 0; i--){
			      		Cookie c3 = cl2.get(i);
			      		if(co.containsKey(c3.getName())){continue;}
			      		co.putInt(c3.getName(), 1);
			      	//ContentValues hh = new ContentValues();
			      	//for(int i = 0; i < cl2.size(); i++){//< cl2.size()
			      		//if(mReg.getInt("ask", 1)>3){easyStatus("Cookie "+(i+1));}
			      		//Cookie c3 = cl2.get(i);
			      		//if( cshort2.contains(c3.getName()+" ")){continue;}
			      		//Log.w(G, "mGet2 safeHttpGet() Cookie(): "+c3.getName() + " " + c3.getValue() + " (" + c3.getDomain()+" p" + c3.isPersistent()+" s" + c3.isSecure() +" " + c3.getPath()+" " +c3.getVersion() +")");
			      		//cshort2 += c3.getName() +" " + c3.getValue() +(!c3.getValue().contains("expires")?c3.getExpiryDate()!=null?"; expires="+c3.getExpiryDate():"":"")+"\n";
			      		cshort2 += c3.getName() +" " + c3.getValue()+(c3.getExpiryDate()!=null?"; expires="+c3.getExpiryDate():"")+(c3.getPath()!=null?"; path="+c3.getPath():"")+(c3.getDomain()!=null?"; domain="+c3.getDomain():"")+"\n";
			      	}
			      	if(cshort2.length() > 0 ){
			      		//final String s = cshort2; 
			      		
			      		mReg = getSharedPreferences("Preferences",MODE_WORLD_WRITEABLE);mEdt = mReg.edit();
			      		mEdt.putString("lastcookies", cshort2);
			      		mEdt.commit();
			      		//Log.w(G,"lastcookies: " + cshort2);
			      		//{Bundle bxb = new Bundle(); bxb.putString("string", "lastcookies");bxb.putString("lastcookies", cshort2);
			      		//bxb.putString("long", "lasthttp");bxb.putLong("lasthttp", System.currentTimeMillis());
			      		//Message mx = new Message(); mx.setData(bxb);setrefHandler.sendMessageDelayed(mx,50);}
			      		
			      		/*
			      		Thread eb = new Thread(){public void run(){mReg = getSharedPreferences("Preferences",MODE_WORLD_WRITEABLE);mEdt = mReg.edit();
			      	mEdt.putLong("lasthttp",System.currentTimeMillis());
			      	mEdt.putString("lastcookies", s);
			      	mEdt.commit();
			      		}};eb.start();//*/
			      	}//}};tc.start();
			      	mHE.consumeContent();
			        }
			        
			      	
			        //
			        // Print Cookies
			        //if ( !mHttpCookie.isEmpty() ) { for (int i = 0; i < mHttpCookie.size(); i++) { Log.w(TAG,"safeHttpGet() Cookie: " + mHttpCookie.get(i).toString()); } }
			        
			        //
			        // Print Headers
		        	//Header[] h = mHttpResponse.getAllHeaders(); for( int i = 0; i < h.length; i++){ Log.w(TAG,"safeHttpGet() Header: " + h[i].getName() + ": " + h[i].getValue()); }
			        //mUrl = httpget.getURI().toString();
			        //Log.w(G,"safeHttpGet() " + mUrl);
			        
				}
			
			
	        responseCode = mHR.getStatusLine().toString();
			
		} catch (ClientProtocolException e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1121 ClientProtocolException for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1122 IO Exception Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			e.printStackTrace();//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",e.printStackTrace());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
			responseCode = " " + e.getLocalizedMessage() + " HTTP ERROR";//easyStatus(responseCode);
		} catch (NullPointerException e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1126 NullPointer Exception for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1127 IO Exception Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			e.printStackTrace();//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",e.printStackTrace());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
		} catch (IOException e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1130 IO Exception for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			//if( e.getLocalizedMessage().contains("Host is unresolved") ){ SystemClock.sleep(1880); }
			
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1132 IO Exception Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			StackTraceElement[] err = e.getStackTrace();
			//for(int i = 0; i < err.length; i++){
				//Log.w(G,"safeHttpGet() 1135 IO Exception Message " + i + " class(" + err[i].getClassName() + ") file(" + err[i].getFileName() + ") line(" + err[i].getLineNumber() + ") method(" + err[i].getMethodName() + ")");
			//}
			responseCode = e.getLocalizedMessage();//easyStatus(responseCode);
		} catch (OutOfMemoryError e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1121 OutOfMemoryError for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1122 IO Memory Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			e.printStackTrace();//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",e.printStackTrace());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
			mEdt.putLong("error", System.currentTimeMillis());mEdt.putString("errortype", "Out of Memory");mEdt.commit();
			responseCode = " " + e.getLocalizedMessage() + " MEMORY ERROR";//easyStatus(responseCode);
		} catch (IllegalArgumentException e){
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","Argument Exception "+e.getLocalizedMessage()+" for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			responseCode = e.getLocalizedMessage();
		
		} catch (IllegalStateException e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1139 IllegalState Exception for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1140 IO Exception Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			e.printStackTrace();//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",e.printStackTrace());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
			//if( responseCode == "" ){
				//responseCode = "440"; //440 simulates a timeout condition and recreates the client.
			//}
		}//e.getLocalizedMessage()
		}};tx.start();
	}
	};
	
	private Handler logoly = new Handler(){public void handleMessage(Message msg){Bundle bx = msg.getData();int l = bx.getInt("l");String text = bx.getString("text");switch(l){case 2:Log.e(TAG,":"+text);break;case 3:Log.w(TAG,":"+text);break;default:Log.i(TAG,":"+text);break;}}};
	
	public String datetime(){
		String g = "";
		Date d = new Date();
		g = (d.getYear()+1900)+"-"+((d.getMonth() < 9)?"0":"")+((d.getMonth()+1))+"-"+((d.getDate() < 10)?"0":"")+d.getDate()+"T"+((d.getHours() < 10)?"0":"")+d.getHours()+":"+((d.getMinutes() < 10)?"0":"")+d.getMinutes()+":00";
		{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","generated date "+g);bx.putInt("l",1);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
		return g;
	}
	
	private String fixDate(String updated) {
		//day, month dd, yyyy hh:mm tt
		//m/d/year hh:mm tt
		String[] dateparts = updated.replaceFirst("T", " ").split(" ");
		//Log.i(G,"fixDate ("+updated+") parts("+dateparts.length+")");
		if(updated.length() > 25){return datetime();}
		
		if(dateparts[0].contains("/") && dateparts[0].contains(":")){
			
			int year = Integer.parseInt(dateparts[0].substring(dateparts[0].lastIndexOf("/")+1, dateparts[0].lastIndexOf("/")+5));
			int mon = Integer.parseInt(dateparts[0].substring(0, dateparts[0].indexOf("/")));
			int day = Integer.parseInt(dateparts[0].substring(dateparts[0].indexOf("/")+1, dateparts[0].lastIndexOf("/")));
			if( mon < 10 ){
				updated = year + "-0" + mon + "-";
			}else{
				updated = year + "-" + mon + "-";
			}
			if( day < 10 ){updated += "0"+ day + " ";}else{updated += day + " ";}
			int h = 0;int m = 0;
			h = Integer.parseInt(dateparts[0].substring(dateparts[0].indexOf(":")-2, dateparts[0].lastIndexOf(":")));
			m = Integer.parseInt(dateparts[0].substring(dateparts[0].indexOf(":")+1));
			if(dateparts[1].toLowerCase().contains("pm") && h < 12){
				h+=12;
			}if(dateparts[1].toLowerCase().contains("am") && h == 12){
				h-=12;
			}
			if( h < 10 ){updated += "0"+ h + ":";}else{updated += h + ":";}
			if( m < 10 ){updated += "0"+ m;}else{updated += m;}
			
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","Updated date to SQLite Format("+updated+") #3");bx.putInt("l",1);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			
		}
		if(dateparts[0].contains("/")
				&& dateparts[1].contains(":")){
			String[] dp = dateparts[0].split("/");
			int year = Integer.parseInt(dp[2]);
			int mon = Integer.parseInt(dp[0]);
			int day = Integer.parseInt(dp[1]);
			if( mon < 10 ){
				updated = year + "-0" + mon + "-";
			}else{
				updated = year + "-" + mon + "-";
			}
			if( day < 10 ){updated += "0"+ day + " ";}else{updated += day + " ";}
			int h = 0;int m = 0;
			String[] t = dateparts[1].split(":");
			h = Integer.parseInt(t[0]);
			m = Integer.parseInt(t[1]);
			if(dateparts[2].toLowerCase().contains("pm") && h < 12){
				h+=12;
			}if(dateparts[2].toLowerCase().contains("am") && h == 12){
				h-=12;
			}
			if( h < 10 ){updated += "0"+ h + ":";}else{updated += h + ":";}
			if( m < 10 ){updated += "0"+ m;}else{updated += m;}
			
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","Updated date to SQLite Format("+updated+") #2");bx.putInt("l",1);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
		}
		if( dateparts.length > 5 || (dateparts.length == 5 && dateparts[3].contains(":")) ){
			// Month
			String[] month = new String("xxx Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec xxx").split(" ");
			int mon = 0;
			for(;mon < month.length; mon++){
				if( month[mon].equalsIgnoreCase(dateparts[2]) ){ break; } 
				if(dateparts[1].startsWith(month[mon])){
					break;
				}
			}
			if( mon == 13 ){
				{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","Unable to determine month in fixDate("+updated+")");bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
				return updated;
			}
			
			// Year
			Date d = new Date();
			int year = d.getYear()+1900;
			if(dateparts[2].length() == 4){
				year = Integer.parseInt(dateparts[2]);
			}else if(dateparts[3].length() == 4){
				year = Integer.parseInt(dateparts[3]);
			}else{
				{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","Unable to determine year in fixDate("+updated+")");bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			}
			
			// Day
			int day = 1;
			
			if(dateparts[2].length() == 4 && !dateparts[0].contains(",")){
				day = Integer.parseInt(dateparts[0]);
			}else if(dateparts[2].length() > 0 && dateparts[2].contains(",")){
				dateparts[2] = dateparts[2].replaceAll(",", "");
				day = Integer.parseInt(dateparts[2]);
			}
			
			// Date == updated
			updated = year + "-";
			updated += (mon < 10?"0"+mon:mon) + "-";
			//if( mon < 10 ){
				//updated = year + "-0" + mon + "-";
			//}else{
				//updated = year + "-" + mon + "-";
			//}
			updated += (day < 10?"0"+day:day) + " ";
			//if( day < 10 ){updated += "0"+ day + " ";}else{updated += day + " ";}
			
			// Hour Minute
			
			int h = 0;int m = 0;
			
			if(dateparts[3].contains(":")){
				String[] t = dateparts[3].split(":");
				h = Integer.parseInt(t[0]);
				m = Integer.parseInt(t[1]);
			}else if(dateparts[4].contains(":")){
				String[] t = dateparts[4].split(":");
				h = Integer.parseInt(t[0]);
				m = Integer.parseInt(t[1]);
				if(dateparts[5].toLowerCase().contains("pm") && h < 12){
					h+=12;
				}if(dateparts[5].toLowerCase().contains("am") && h == 12){
					h-=12;
				}
			}
			
			
			
			//Time
			updated += (h < 10?"0"+h:h)+":";
			updated += (m < 10?"0"+m:m);
			//if( h < 10 ){updated += "0"+ h + ":";}else{updated += h + ":";}
			//if( m < 10 ){updated += "0"+ m;}else{updated += m;}
			
			//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","Updated date to SQLite Format("+updated+")");bx.putInt("l",1);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
		}
		
		return updated;
	}
	
	
	
	
	//private long storeTime = 0;
	Handler storePage = new Handler(){
		public void handleMessage(Message msg){
			//if(storeTime > System.currentTimeMillis()){ easyStatus("Refresh occured within 8 seconds, try later.");  return; }//Bundle bl = msg.getData();Message ml = new Message(); ml.setData(bl);storePage.sendMessageDelayed(ml,1750);return;} 
			//storeTime = System.currentTimeMillis()+7750;
			
			final Bundle bdl = msg.getData();
	//		s01(bdl);
	
		
	//public void s01(final Bundle bdl){
		
		Thread tx = new Thread(){
				public void run(){
					String mhpb = bdl.getString("mhpb");
					String loc = bdl.getString("storloc");
					//Log.w(G,"storePage 81 converting content " + mhpb.length() + " bytes");
					//byte[] mx = new byte[mhpb.length+1 * 2];
					//String mHP = "";
					//for(int i = 0; i+6 < mhpb.length; i++){mHP += mhpb[i]+mhpb[++i]+mhpb[++i]+mhpb[++i]+mhpb[++i]+mhpb[++i];}
					/*
					int errorcnt = 0;
					for(errorcnt = 0; errorcnt < 5; errorcnt++){
					try {
					mHP = new String(mhpb);
					break;
					} catch (OutOfMemoryError e){
						
						Log.e(G,"OutOfMemory Error Received while Storing Page");
						//{Message ml = new Message(); Bundle bl = new Bundle(bdl); ml.setData(bl); storePage.sendMessageDelayed(ml, 3500);}return;
					}
					}	
					if( errorcnt == 5 || mHP == null || mHP.length() == 0){
						easyStatus("Wild errors " + errorcnt);
						return;
					}//*/
						
					
					SharedPreferences mReg = getSharedPreferences("Preferences",MODE_WORLD_WRITEABLE);Editor mEdt = mReg.edit();
					//mEdt.putString(loc+"url", murl);
					
					Log.w(TAG,"storePage 86 storing content");
					//mx = null;
					mEdt.putString(loc, mhpb);
					mEdt.putLong(loc+"_saved", System.currentTimeMillis());
					mEdt.commit();
					Log.w(TAG,"safeHttpGet() 368 saved");
				
					String titlr = bdl.getString("title");
			        String murl = bdl.getString("murl");	
					long sh = bdl.getLong("startdl");
			        String dest = bdl.getString("dest");
					String statusline = bdl.getString("statusline");
			        String pageconnectknow = bdl.getString("pageconnectknow");
					
					//final String murl = mUrl;final String mhp = mHP;
			        	//Thread eb = new Thread(){public void run(){
			        	if( pageconnectknow == null || mhpb.contains(pageconnectknow) ){
			        	
			        		//easyStatus("Acquired "+titlr+" in "+(SystemClock.uptimeMillis()-sh)/1000+" secs.\n"+dest);
			        	//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("subtitle","Acquired "+titlr+" in "+(SystemClock.uptimeMillis()-sh)/1000+" secs.\n"+dest ); mxm.setData(bxb);easyViewerHandler.sendMessageDelayed(mxm,10);}
						//}};eb.start();
			      		//mReg = getSharedPreferences("Preferences",MODE_WORLD_WRITEABLE);mEdt = mReg.edit();
			      		//mEdt.putString(loc, mHP);mEdt.commit();
			        	//{Message mx = new Message();Bundle bxb = new Bundle();// bxb.putString("string", loc+",");bxb.putString(loc, mHP);
			      		//bxb.putString("long", "lasthttp");bxb.putLong("lasthttp", System.currentTimeMillis());
			      		// mx.setData(bxb);setrefHandler.sendMessageDelayed(mx,50);}//*/
			        	}else if(pageconnectknow != null){//easyStatus("Invalid Download");
			        	{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("l",2);bl.putString("text",titlr+" downloaded didn't pass acknowledgement("+bdl.getString("pageconnectknow")+"). Lov'n cookies and the rest.");ml.setData(bl);logoly.sendMessageDelayed(ml,75); }
			        	String ct = "";
			        	{Message ml = new Message(); Bundle bxb = new Bundle(); bxb.putString("remove", "connect");  ml.setData(bxb);setrefHandler.sendMessageDelayed(ml, 50);}
						//String[] h = mHP.split("\n");for(int b = 0; b < h.length; b++){{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","pageknow("+bdl.getString("pageconnectknow")+") "+h[b]);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
						//if(h[b].contains("content1=") ){ct += h[b].substring(h[b].indexOf("content1=")+9, h[b].indexOf(";HS;", h[b].indexOf("content1=")));}}
						//ct = ct.replaceAll("%0a", "\n").trim();
						Log.e(TAG,"know("+bdl.getString("pageconnectknow")+")");//Log.w(G,"know("+bdl.getString("pageconnectknow")+")"+h[b]);
			        	}
			        {Message ml = new Message(); Bundle bx = new Bundle();bx.putString("text","Downloaded status(" + statusline + ") loc("+loc+") mUrl("+murl+") " + mhpb.length() + " bytes.");ml.setData(bx);logoly.sendMessageDelayed(ml,pRate);}
			    
					
				}
			};
			tx.start();
		//}	
		}
	};
	
	
	private Handler setrefHandler = new Handler(){
		Bundle bx;public void handleMessage(Message mx){
		final Bundle bx = mx.getData();
			Thread tx = new Thread(){public void run(){
		
	SharedPreferences mReg = getSharedPreferences("Preferences",MODE_WORLD_WRITEABLE);
			Editor mEdt = mReg.edit();
	String[] ht = new String[]{"int","long","float","remove","string"};
	for(int b = 0; b < ht.length; b++){
		String t = ht[b];
		if(!bx.containsKey(t)){continue;}
	String[] nm = (bx.getString(t)+",0").split(",");
	for(int h = 0; h < nm.length; h++){
		String k = nm[h];if(k==null){continue;}if(k.length()<=1){continue;}else if(k.contentEquals("null")){continue;}
		if(!bx.containsKey(k) && !t.contentEquals("remove") ){
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","error reading incoming preference (doesn't exist in bundle) "+k);bx.putInt("l", 2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			continue;}String len = "";
		if(t.contentEquals("float")){mEdt.putFloat(k, bx.getFloat(k));len += bx.getFloat(k)+"";}
		else if(t.contentEquals("int")){mEdt.putInt(k, bx.getInt(k));len += bx.getInt(k)+"";}
		else if(t.contentEquals("long")){mEdt.putLong(k, bx.getLong(k));len += bx.getLong(k)+"";}
		else if(t.contentEquals("string")){mEdt.putString(k, bx.getString(k));len += bx.getString(k)+"";}
		else if(t.contentEquals("remove")){mEdt.remove(k);}
		//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","setpref "+k+" "+t + " "+(len.length() <100?len:len.length()+"b"));mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
	}mEdt.commit();	
	}
	
	}};tx.start();
	   	}
	};
	
}
