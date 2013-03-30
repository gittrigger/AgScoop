package com.fightind.agscoop;
// Don't Panic


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

import com.fightind.agscoop.DataProvider;
import com.fightind.agscoop.R;
import com.fightind.agscoop.SqliteWrapper;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Motion extends ListActivity {
    
	public static String title = "Seattle.gov Weekend Events";
    public static String dest = "http://www.trumba.com/calendars/seattle-events-weekend-events.rss";
    public static int listMost = 100;
	public static String recoveryintent = "com.fightind.agscoop.SERVICE_RECOVER3";
	public static int naturalLimit = 10;//New records inserted per download/processing event.
	public static String handleCleanSQL = "published is not null AND strftime('%J',published) < strftime('%J','now')-30";
	public static String todayCountSQL = "strftime('%m-%d-%Y',published) = strftime('%m-%d-%Y','now')";
	public static int notifyimage = R.drawable.docdot;
	public static String loadlistSQL = "julianday(published) > julianday(datetime('NOW'))-32 AND status > 0";
	
	
	
	Handler getlist = new Handler(){
    	private long hover = 0;
    	public void handleMessage(Message msg){
    		if(hover > SystemClock.uptimeMillis()){
    			easyStatus("Started 10>" + (hover - SystemClock.uptimeMillis())/1000 + " seconds ago.");return;
    		}
    		hover = SystemClock.uptimeMillis() + 10000;
    		final Bundle bdl = msg.getData();
    		Log.i(G,"getlist");
    		
    		{handleNotification.sendEmptyMessage(2);}
    		
    		
    		Thread tx = new Thread(){
    			public void run(){
    				SharedPreferences mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
    	            Editor mEdt = mReg.edit();
    				//long lvhi = getListView().getItemIdAtPosition(0);
    				//LinearLayout lvh = (LinearLayout) findViewById((int)lvhi);//.getItemAtPosition(1);
    				if(headerText > 0){
    				//TextView tvh = (TextView) findViewById(headerText);
    				//if(tvh != null){
    					long st = System.currentTimeMillis();
    					//long lt = mReg.getLong("bucket_saved", 0);
    					//tvh.setText("Running");
    					
    					{Message ml = new Message(); Bundle bl = new Bundle(bdl); ml.setData(bl); mGet.sendMessage(ml);}
    					{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Running"); ml.setData(bl); setText.sendMessage(ml);}
    				
    					
    					try {
    						long freememory = 0;
    						for(int i = 0; i < 300 && getListView().isShown(); i++){	
							
								if(getListView().isShown()){}else{Log.e(G,"List isn't shown, process watch close");wayGo.sendEmptyMessage(2);break;}
								freememory = Runtime.getRuntime().freeMemory();
								Log.i(G,"processing " + i + " freememory("+freememory/1024+" Kb) has(" + getListView().hasFocus() +") shown("+getListView().isShown()+") enabled("+getListView().isEnabled()+")");
								
								if(st > mReg.getLong("bucket_saved", 0) && st > mReg.getLong("error", 0)){
									{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Running " + ((System.currentTimeMillis() - st)/1000) + " seconds."); ml.setData(bl); setText.sendMessageDelayed(ml,pRate);}
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
							
							{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Downloaded " + (ct.length()>1024?ct.length()/1024+" Kb":ct.length()+"b")+" at " + (d.getHours() > 12?d.getHours()-12:d.getHours()) + ":" + (d.getMinutes()<10?"0":"") + d.getMinutes() + "\n" + ctl.length + " Moments"); ml.setData(bl); setText.sendMessage(ml);}
							//ct = ct.replace("\r", "\n");
							if(ctl.length > 0){
							
								//mProgressDialog = ProgressDialog.show(mCtx, "Gratuitous Notification", "Loading", true);
								{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("max",ctl.length); bl.putString("title", "Colliding da'Base"); bl.putBoolean("indeter", true); bl.putString("text", "Processing\n"+(ctl.length-1)+"\nDownloaded\n"+Motion.naturalLimit+" New Record Limit"); ml.setData(bl); mProgress.sendMessage(ml);}
								
							Cursor cx=null;
							boolean found = true;
							int foundnew = 0;
							Uri contentpath = Uri.withAppendedPath(DataProvider.CONTENT_URI,"moment");
							ContentValues cv = new ContentValues();
							String title, published, content, url;
							for(int b = 0; b < ctl.length; b++){if(ctl[b].length() == 0){continue;}//Log.i(G,ctl[b]+"<------");
								
							
								//
								// CUSTOMIZED
								//
								title = ctl[b].substring(ctl[b].indexOf("<title>")+7, ctl[b].indexOf("</title>"));
								if(ctl[b].indexOf("<pubDate>") > 0){published = fixDate(ctl[b].substring(ctl[b].indexOf("<pubDate>")+9, ctl[b].indexOf("</pubDate>")));}else{published = "unavail";}
								url = ctl[b].substring(ctl[b].indexOf("<link>")+6, ctl[b].indexOf("</link>"));
								if(ctl[b].indexOf("<description>") > 0){content = ctl[b].substring(ctl[b].indexOf("<description>")+13, ctl[b].indexOf("</description>"));}else{content="unavail";}
								title = title.replaceAll("\"", "\\\"");
								// EC
								
								//DEBUG
								//Log.w(G,"thread("+this.getId()+") title("+title+") published("+published+") author(unavil) url("+url+") content("+content+")");
								
								found = false;
								//
								// CUSTOMIZED
								//
								cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"count(*)"}, "published = \""+published+"\" AND url = \""+url+"\" AND status > 0", null, null);
				     			// EC
								
								if( cx != null){if( cx.moveToFirst() ){ if( cx.getInt(0) > 0){found=true;}; } cx.close();}
								if(!found){
									foundnew ++;
									cv.put("title", title);
									cv.put("url", url);
									cv.put("published", published);
									cv.put("content", content);
									//cv.put("author", author);
									SqliteWrapper.insert(mCtx, mCtx.getContentResolver(), contentpath, cv);//+"\n"+published+"\n\n"+b+" of "+(ctl.length-1)+""
									{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text",title); ml.setData(bl); mProgressMessage.sendMessage(ml);}
									
									//
									// CUSTOMIZED
									// 
									if(foundnew >= Motion.naturalLimit){easyStatus("Casual Natural Limit\n" + foundnew + " New per refresh");break;}
									// EC
								}
								//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("progress",b+1); ml.setData(bl); mProgressPlus.sendMessage(ml);}
							}
							mProgressOut.sendEmptyMessage(2);
							cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"count(*)"}, todayCountSQL, null, null);
			     			int today = 0;
							if( cx != null){if( cx.moveToFirst() ){ if( cx.getInt(0) > 0){today=cx.getInt(0);}; } cx.close();}
							
							cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"count(*)"}, "", null, null);
			     			int onfile = 0;
							if( cx != null){if( cx.moveToFirst() ){ if( cx.getInt(0) > 0){onfile=cx.getInt(0);}; } cx.close();}
							
							int hand = SqliteWrapper.delete(mCtx, mCtx.getContentResolver(), contentpath, Motion.handleCleanSQL, null);//new String[] {"count(*)"});
			     			if(hand > 0){easyStatus("Handled removal of " + hand + " past records.");}
							
							{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", "Downloaded " + (ct.length()>1024?ct.length()/1024+" Kb":ct.length()+"b")+" at " + (d.getHours() > 12?d.getHours()-12:d.getHours()) + ":" + (d.getMinutes()<10?"0":"") + d.getMinutes() + "\nSource Mass " + (ctl.length-1) + "\n" + (foundnew > 0?foundnew + " New\n":"")+today+" Dated Today\n"+onfile+" Stored"); ml.setData(bl); setText.sendMessage(ml);}
							}
						}
						if( st < mReg.getLong("error", 0) ){
							{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", headerText); bl.putString("text", mReg.getString("errortype", "")); ml.setData(bl); setText.sendMessage(ml);}
						}
						
						
						
						//}else{
    				//	easyStatus("Wild error, unable to grab tvh");
    				//}	
    				}else{
    					easyStatus("Wild error, unable to see self, restart");
    				}
    			}
    		};
    		tx.start();
    	}	
    };
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.w(G,"onP");
    }


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.w(G,"Resume");lastposition = 0;
	}

	Handler wayGo = new Handler(){
		public void handleMessage(Message msg){
			finish();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	SharedPreferences mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
    	{
    		int groupNum = 20;
			SubMenu sync = menu.addSubMenu(Menu.NONE, groupNum, 20, "Interval"); //getItem().
			//sync.setIcon(android.R.drawable.stat_notify_sync);
			sync.add(groupNum, 0, 0, "Not Automatic");//value == 0
			sync.add(groupNum, 30, 2, "30 Minutes");
			sync.add(groupNum, 60, 2, "Hourly");
			sync.add(groupNum, 1, 2, "5 Hours");//value == 1
			sync.add(groupNum, 2, 3, "Daily");// value == 2
			
			int interval = mReg.contains("interval") ? mReg.getInt("interval",1) : 1;
			sync.setGroupCheckable(groupNum, true, true);
			sync.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = sync.findItem(interval);
			if( activeitem == null ){
				if( interval >= 10 ){
					sync.add(groupNum, interval, 1, "Every " + interval + " minutes");
				}else{
					interval = 1; // Must exist.
				}
				activeitem = sync.findItem(interval);
			}
			activeitem.setChecked(true);
		}
    	
    	return super.onCreateOptionsMenu(menu);
	}

    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int itemid = item.getItemId();
		final int groupid = item.getGroupId();
		item.setChecked(true);
		
		//moomo = menu;
		//moomoMenu.sendEmptyMessageDelayed(50,10);
		Thread tx = new Thread(){public void run(){
		switch(groupid){
		
		case 20: // Interval
			
			{Message mxm = new Message(); Bundle bxb = new Bundle();bxb.putString("int", "interval");bxb.putInt("interval",itemid);mxm.setData(bxb);setrefHandler.sendMessage(mxm);}
			if(itemid == 0){
				easyStatus("Not Automatic");
				Intent service = new Intent();service.setClass(mCtx, AutomaticService.class); stopService(service);
			}else{easyStatus("Interval Setting Saved");}
			break;
		default:
			break;
		}
		}};tx.start();
		return super.onOptionsItemSelected(item);
	}
    
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putLong("id", id); bl.putInt("position", position); ml.setData(bl); click.sendMessage(ml);}
    
    }
	Handler click = new Handler(){
		public void handleMessage(Message msg){
			Bundle bdl = msg.getData();
			long id = bdl.getLong("id",0);
			int position = bdl.getInt("position",0);
			
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","List Item Clicked position("+position+") id("+id+") count("+getListView().getCount()+")");bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			
			if(position == 0){
				//header
				{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("title", Motion.title); bl.putString("dest", Motion.dest); bl.putString("storloc", "bucket"); ml.setData(bl); getlist.sendMessage(ml);}
			}else if(position == getListView().getCount()-1){
				//footer
			
			}else{
				//moment
				//Cursor cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"url"}, "_id = " + id, null, null);
	 			//if( cx != null){if( cx.moveToFirst() ){ if( cx.getInt(0) > 0){found=true;}; } cx.close();}
				if(position > 0){	
					id = getListView().getItemIdAtPosition(position);
				}
				Intent lookup = new Intent(mCtx,Lookup.class);
				lookup.putExtra("id", id);
				startActivity(lookup);
				
			}

		}
	};
	/** Called when the activity is first created. */
    private static String G = "Motion";
    //private SharedPreferences mReg;
	//private Editor mEdt;
	private Context mCtx;
	int pRate = 72;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion);
        
        mCtx = this;
        SensorService.sendEmptyMessage(2);
        {Message ml = new Message(); Bundle bl = new Bundle(); ml.setData(bl); loadlist.sendMessageDelayed(ml,pRate);}
        
        
	}

	int lastposition = 0;
	Handler SensorService = new Handler(){
    	public void handleMessage(Message msg){
    		final Bundle bdl = msg.getData();
    		Thread tx = new Thread(){
    			boolean mStable = true;
    			int position = 0;float[] lastvalues;
				long smooth = 34;//long smoothtext = 32;//String cn = "";
				
    			public void run(){
    				
    				SensorEventListener or = new SensorEventListener(){



    					public void onAccuracyChanged(Sensor arg0, int arg1) {
    						// TODO Auto-generated method stub
    						
    					}

    					//float mStable0 = 1;
    					public void onSensorChanged(SensorEvent event) {
    						// TODO Auto-generated method stub
    						
    						if(smooth > SystemClock.uptimeMillis() || !getListView().hasFocus() ){return;}
    						smooth = SystemClock.uptimeMillis() + 250;//bdl.getInt("sensorspeed",250);
    						float[] values = event.values;
    						float valence = 0;
							
    						if(lastvalues == null){
    							Log.w(G,"Loading Initial Sensor Values");
    							lastvalues = values;
    							for(int b = 0; b < values.length; b++){lastvalues[b] = 0;}
    						}
    						
    						if( lastvalues != null && values.length == lastvalues.length ){
    							//if(){
    								
    								position = getListView().getSelectedItemPosition();
    								if(position == -1 || position == 0 || position == getListView().getCount()-1){return;}
    								for(int b = 0; b < values.length; b++){
    									
    									valence = (lastvalues[b]>values[b]?lastvalues[b]-values[b]:values[b]-lastvalues[b]);
    									lastvalues[b] = values[b];
    									if(valence <= 3.5 || !getListView().isShown() ){continue;}
    									
    									//Log.i(G,"Sensor Orientation ["+b+"] "+lastvalues[b]+" to "+values[b]+" position("+position+") last("+lastposition+") valence " + valence);
    									
    										Log.w(G,"Wave Select has(" + getListView().hasFocus() +") shown("+getListView().isShown()+") position("+position+") last("+lastposition+")");
    										
    										if(position != lastposition){
    											//smooth = SystemClock.uptimeMillis() + bdl.getInt("sensorspeed",1750);
        										
    											//getListView().clearFocus();
    											
    											{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("position", position); ml.setData(bl); click.sendMessage(ml);}
    											lastposition = position;
    										}
    										break;
    									
    								}
    								//if(position != lastposition){
    									//lastvalues = values;
    								//}
    							//}	
    						}
    						//int sensorid = event.sensor.getType();
    						//if(sensorid == SensorManager.SENSOR_ORIENTATION){if(mStable){mStable0 = values[0];mStable = false;}					
    							
    						
    					    
    						//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", R.id.apps); bl.putInt("x", (int)((mStable0>values[0]?mStable0-values[0]:values[0]-mStable0)*71)); bl.putInt("y", (int)(values[1]*33)); ml.setData(bl); move.sendMessage(ml);}
    						
    						//	{Message ml = new Message();Bundle bl = new Bundle(); bl.putFloat("u1", values[0]); bl.putFloat("u2", values[1]); bl.putFloat("u3", values[2]); ml.setData(bl);setOrientation.sendMessageDelayed(ml,pRate);}
    							//{Message ml = new Message();Bundle bl = new Bundle(); bl.putFloat("u", values[0]); ml.setData(bl);setLabelOrientation.sendMessageDelayed(ml,pRate);}
    					}
    						
    						//if(smoothtext > SystemClock.uptimeMillis()){return;}
    						//smoothtext = SystemClock.uptimeMillis() + 1750;
    						//String cn = "("+event.sensor.getName()+"+"+values.length+")"+(int)values[0]+(values.length>0?"\n"+(int)values[1]:"")+""+(values.length>1?"\n"+(int)values[2]:"");//+""+(values.length>2?"\n"+(int)values[3]:"")+"";
    						//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", bdl.getInt("morsv"));bl.putString("text", cn);bl.putInt("color", Color.argb(200, 250, 250, 255));ml.setData(bl);setText.sendMessageDelayed(ml,pRate);}
    						//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", bdl.getInt("morsv"));bl.putString("text", cn);bl.putInt("color", Color.argb(240, 250, 250, 255));ml.setData(bl);setText.sendMessageDelayed(ml,pRate<100?pRate*4:(int)(pRate+(pRate/4)));}
    						
    						
    						
    					};
    				
    		        	SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
    		        	
    		        	sm.registerListener(or, sm.getDefaultSensor(SensorManager.SENSOR_ORIENTATION) , SensorManager.SENSOR_DELAY_GAME );
    					
    		        	easyStatus("Wave Ready");
    					try {
    						for(;;Thread.sleep(750)){if(getListView().isShown() || getListView().hasFocus()){}else{Log.e(G,"List isn't shown and nofocus, sensor watch close");wayGo.sendEmptyMessage(2);break;}}
    					}catch(InterruptedException e){Thread.interrupted();}
    						
    				
    				
    			}
    		};
    		tx.start();
    	}	
    };

    
	Handler h1 = new Handler(){
    	public void handleMessage(Message msg){
    		Bundle bdl = msg.getData();
    		Thread tx = new Thread(){
    			public void run(){
    				
    			}
    		};
    		tx.start();
    	}	
    };
    
    Handler handleNotification = new Handler(){
    	public void handleMessage(Message msg){
    		Bundle bdl = msg.getData();
    		Thread tx = new Thread(){
    			public void run(){
    				NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    				mNM.cancelAll();
    			}
    		};
    		tx.start();
    	}	
    };
    
    private Cursor lCursor;
	private int headerText = 0;
	private int footerText = 0;
	Handler loadlist = new Handler(){
		public void handleMessage(Message msg){
			Bundle bdl = msg.getData();
			
			String[] columns = new String[] {"_id","title","strftime('%m/%d %H:%M',published) as published","author"};
			String[] from = new String[]{ "title","published","author"};
			int[] to = new int[]{R.id.listrow_title, R.id.listrow_published};
	        
			lCursor = SqliteWrapper.query(mCtx, getContentResolver(), Uri.withAppendedPath(DataProvider.CONTENT_URI,"moment"), 
	        		columns,
	        		Motion.loadlistSQL, // Future configurable time to expire seen and unread
	        		null, 
	        		"published asc limit " + listMost );// + startrow + "," + numrows
			
			startManagingCursor(lCursor);
	        SimpleCursorAdapter entries = new SimpleCursorAdapter(mCtx, R.layout.listrow, lCursor, from, to);
			
	        LinearLayout l1 = new LinearLayout(mCtx); 
	        l1.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, 65));
	        l1.setId((int)SystemClock.uptimeMillis());
	        l1.setBackgroundColor(Color.argb(200, 0, 80, 50));
	        TextView t1 = new TextView(mCtx);
	        t1.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
	        t1.setId((int)SystemClock.uptimeMillis());
	        t1.setTextSize((float)12);
	        l1.setGravity(Gravity.CENTER_VERTICAL);
	        t1.setTextColor(Color.BLACK);
	        t1.setGravity(Gravity.CENTER);
	        t1.setText("badass technology");
	        
	        l1.addView(t1);
	        footerText = t1.getId();
	        getListView().addFooterView(l1, null, true);
	        
	        LinearLayout l2 = new LinearLayout(mCtx); 
	        l2.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, -2));
	        l2.setId((int)SystemClock.uptimeMillis());
	        l2.setBackgroundColor(Color.argb(200, 0, 80, 50));
	        //l2.setBackgroundColor(Color.argb(200, 80, 80, 80));
	        l2.setPadding(3, 1, 3, 1);
	        TextView t2 = new TextView(mCtx);
	        t2.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
	        t2.setId((int)SystemClock.uptimeMillis());
	        
	        Uri contentpath = Uri.withAppendedPath(DataProvider.CONTENT_URI, "moment");
	        //"strftime('%m-%d-%Y',published) = strftime('%m-%d-%Y','now')"
	        Cursor cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"count(*)"}, Motion.todayCountSQL, null, null);
 			int today = 0;
			if( cx != null){if( cx.moveToFirst() ){ if( cx.getInt(0) > 0){today=cx.getInt(0);}; } cx.close();}
			//cx = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), contentpath, new String[] {"strftime('%m/%d %H:%M',created) as created"}, "status > 0", null, "created desc limit 1");
 			String recent = "";//
			//if( cx != null){if( cx.moveToFirst() ){ recent=cx.getString(0); } cx.close();}
			
			SharedPreferences mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
			Date d = new Date(mReg.getLong("bucket_saved", 0));
	        recent = d.getMonth()+"/"+d.getDate()+" "+(d.getHours() > 12?d.getHours()-12:d.getHours()) + ":" + (d.getMinutes()<10?"0":"") + d.getMinutes();
			if( mReg.getLong("bucket_saved", 0) != 0 ){	
		        easyStatus(today + " Dated Today"+"\nRefreshed " + recent );
			}
			t2.setTextSize((float)22);
	        //t2.setFocusable(true);
	        /*t2.setOnFocusChangeListener(new OnFocusChangeListener(){ public void onFocusChange(View v, boolean has){
	        	if(has){
	        		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", v.getId()); bl.putInt("color", Color.argb(200, 200, 50, 200)); ml.setData(bl); setColor.sendMessage(ml);}
	        	}else{
	        		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("id", v.getId()); bl.putInt("color", Color.argb(200, 20, 250, 200)); ml.setData(bl); setColor.sendMessage(ml);}
	        	}
	        	//tv = (TextView) v;
	        	//if(has){tv.setTextColor(Color.argb(255, 200, 50, 200));}else{tv.setTextColor(Color.argb(255, 50, 200, 50));}
	        }});//*/
	        //t2.setOnClickListener(new OnClickListener(){public void onClick(View v){    }});
	        l2.addView(t2);
	        headerText = t2.getId();
	        getListView().addHeaderView(l2, null, true);
	        
	        
	        setListAdapter(entries);
	        getListView().setSelectionAfterHeaderView();
	        /*
	        //getListView().setOnFocusChangeListener(new OnFocusChangeListener(){public void onFocusChange(View v, boolean has){lastposition = 0;}});
	        getListView().setOnItemSelectedListener(new OnItemSelectedListener(){

				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					// TODO Auto-generated method stub
					lastposition = 0;
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					lastposition = 0;
				}});//*/
			
			
			{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("title", Motion.title); bl.putString("dest", Motion.dest); bl.putString("storloc", "bucket"); ml.setData(bl); getlist.sendMessage(ml);}
			serviceStart.sendEmptyMessageDelayed(2,10000);
		}
	};
	
	Handler serviceStart = new Handler(){
		public void handleMessage(Message msg){
			Thread tx = new Thread(){
				public void run(){
					SharedPreferences mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
					int interval = mReg.getInt("interval", 1);
					if(interval != 0){
						String intervalText = "";
						if(interval == 1){ intervalText = "5 Hour Interval"; }
						else if(interval == 2){ intervalText = "Daily Interval"; }
						else if(interval >= 10){ intervalText = interval + " Minute Interval"; }
						easyStatus("Automatic\n" + intervalText );
						Intent service = new Intent(); 
						service.setClass(mCtx, AutomaticService.class);
				    	stopService(service);
				    	startService(service);   	
					}else{ Intent service = new Intent();service.setClass(mCtx, AutomaticService.class); stopService(service); }
				}
			};
			tx.start();
		
		}
	};	

	
    
    private ProgressDialog mProgressDialog;
    private Handler mProgress = new Handler(){
		public void handleMessage(Message mx){Bundle bx = mx.getData();
		String n = bx.getString("text");
		String nx = bx.getString("title");	
		boolean indeter = bx.getBoolean("indeter");
		mProgressDialog = ProgressDialog.show(mCtx, nx, n, indeter);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		Log.w(G,"progress max " + mProgressDialog.getMax());
		//mProgressDialog.setMax(100);
		//if( bx.containsKey("max") ){ mProgressDialog.setMax((bx.getInt("max")!=null?bx.getInt("max"):100)); }
		
		mProgressDialog.setCanceledOnTouchOutside(true);	
		mProgressDialog.setOnCancelListener(new OnCancelListener(){

				public void onCancel(DialogInterface dv) {
					// TODO Auto-generated method stub
					easyStatus("Loading in Background");
					
					//Log.w(G,"Cancelled Progress Dialog");
				}});
			
			}
    };

    private Handler mProgressMessage = new Handler(){long smooth = 0;public void handleMessage(Message msg){Bundle bdl = msg.getData(); if(mProgressDialog == null){ easyStatus(bdl.getString("text")); return; }else{ if(smooth > SystemClock.uptimeMillis()){ {Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", bdl.getString("text")); ml.setData(bl); mProgressMessage.sendMessageDelayed(ml,750);} return;} smooth = SystemClock.uptimeMillis() + 1750; mProgressDialog.setMessage(bdl.getString("text"));}}};
    private Handler mProgressMax = new Handler(){public void handleMessage(Message msg){if(mProgressDialog != null){Bundle bl = msg.getData();int max = bl.getInt("max"); Log.i(G,"setting max to "+max); mProgressDialog.setMax(max);}}};
    private Handler mProgressPlus = new Handler(){public void handleMessage(Message msg){if(mProgressDialog != null){Bundle bl = msg.getData();mProgressDialog.setProgress(bl.getInt("progress"));}}};
	private Handler mProgressOut = new Handler(){public void handleMessage(Message msg){if(mProgressDialog != null && mProgressDialog.isShowing() ){mProgressDialog.dismiss();}}};

    
    
    private DefaultHttpClient mHC;
	public Handler mGet = new Handler(){
		public void handleMessage(Message msg){Bundle bx = msg.getData();mget2(bx);}
		private void mget2(final Bundle bx){if(mHC == null){mHC = new DefaultHttpClient();}
		final String dest = bx.getString("dest");
		final String loc = bx.getString("storloc");
		final String titlr = bx.getString("title");final String procg = bx.getString("procg");
	
		if( dest == null || dest.length() == 0 ){
			Log.e(G,"Blocked empty get request: Destination titled " + titlr + " intended to " + loc);
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
	
			
			easyStatus("Acquiring " + titlr +"\n"+dest);
			//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("title",procg);bxb.putString("subtitle", ); mxm.setData(bxb);easyViewerHandler.sendMessageDelayed(mxm,10);}
			
			
		final long sh = SystemClock.uptimeMillis();
		HttpGet httpget = new HttpGet(dest);
		String mUrl = httpget.getURI().toString();
		
		//Log.w(G,"safeHttpGet() 1033 getURI("+httpget.getURI()+") for " + who);
		if( httpget.getURI().toString() == "" ){
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("subtitle","Blocked empty destination get.");bx.putString("title",procg+" "+titlr);mx.setData(bx);easyViewerHandler.sendMessageDelayed(mx,pRate);}
			return;
		}
		
		String responseCode = ""; //String mHP = "";
		//CookieStore c = mHC.getCookieStore();
		//mHC = new DefaultHttpClient();mHC.setCookieStore(c);
		CookieStore cs = (mHC != null) ? mHC.getCookieStore(): new DefaultHttpClient().getCookieStore();
		DefaultHttpClient mHC = new DefaultHttpClient();
		SharedPreferences mReg = mCtx.getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
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
			long freememory = Runtime.getRuntime().freeMemory();
			{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("text","Downloading into RAM\n"+(freememory/1024)+" Kb free"); mxm.setData(bxb);easyStatusHandler.sendMessageDelayed(mxm,10);}
			HttpResponse mHR = mHC.execute(httpget);
			//reply[2] = mReg.getString(loc+"url", mUrl);mUrl = reply[2];
	
			if( mHR != null ){
		        Log.w(G,"safeHttpGet() 436 " + mHR.getStatusLine() + " " + " for " + who);
				//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("text","Server says "+mHR.getStatusLine().getStatusCode() + " "+mHR.getStatusLine().getReasonPhrase()); mxm.setData(bxb);easyStatusHandler.sendMessageDelayed(mxm,10);}
				easyStatus(mHR.getStatusLine().getStatusCode() + " " + mHR.getStatusLine().getReasonPhrase());
				
		        Log.w(G,"safeHttpGet() 440 response.getEntity() for " + who);
		        HttpEntity mHE = mHR.getEntity();
	
		        if (mHE != null) {
			        //byte[] bytes = ;
		        	Log.w(G,"safeHttpGet() 445 byte[] to EntityUtils.toByteArray(mHE) expect 448");
		        	freememory = Runtime.getRuntime().freeMemory();
		        	easyStatus("Downloaded into RAM " + (mHE.getContentLength()>1024?(mHE.getContentLength()/1024)+" Kb":mHE.getContentLength()+"b" + "\n"+(freememory/1024)+" Kb free" ));
		        	String mhpb = EntityUtils.toString(mHE);
		        	Log.w(G,"safeHttpGet() 448 mhpb("+mhpb.length()+") to String for " + who);
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
			      		
			      		Editor mEdt = mReg.edit();
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
			responseCode = " " + e.getLocalizedMessage() + " HTTP ERROR";easyStatus(responseCode);
		} catch (NullPointerException e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1126 NullPointer Exception for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1127 IO Exception Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			e.printStackTrace();//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",e.printStackTrace());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
		} catch (IOException e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1130 IO Exception for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			//if( e.getLocalizedMessage().contains("Host is unresolved") ){ SystemClock.sleep(1880); }
			responseCode = e.getLocalizedMessage();
			
			Editor mEdt = mReg.edit();
			mEdt.putLong("error", System.currentTimeMillis());mEdt.putString("errortype", responseCode);mEdt.commit();
			
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1132 IO Exception Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			//StackTraceElement[] err = e.getStackTrace();
			//for(int i = 0; i < err.length; i++){
				//Log.w(G,"safeHttpGet() 1135 IO Exception Message " + i + " class(" + err[i].getClassName() + ") file(" + err[i].getFileName() + ") line(" + err[i].getLineNumber() + ") method(" + err[i].getMethodName() + ")");
			//}
			easyStatus(responseCode);
		} catch (OutOfMemoryError e) {
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1121 OutOfMemoryError for " + who);bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","safeHttpGet() 1122 IO Memory Message " + e.getLocalizedMessage());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
			e.printStackTrace();//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",e.printStackTrace());bx.putInt("l",2);mx.setData(bx);logoly.sendMessageDelayed(mx,100);}
			Editor mEdt = mReg.edit();
			long freememory = Runtime.getRuntime().freeMemory();
			responseCode = "OS Crunch, Out of RAM at " + (freememory/1024) + " Kb";
			mEdt.putLong("error", System.currentTimeMillis());mEdt.putString("errortype", responseCode);mEdt.commit();
			easyStatus(responseCode);
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
					
					Log.w(G,"storePage 86 storing content");
					//mx = null;
					mEdt.putString(loc, mhpb);
					mEdt.putLong(loc+"_saved", System.currentTimeMillis());
					mEdt.commit();
					Log.w(G,"safeHttpGet() 368 saved");
				
					String titlr = bdl.getString("title");
			        String murl = bdl.getString("murl");	
					long sh = bdl.getLong("startdl");
			        String dest = bdl.getString("dest");
					String statusline = bdl.getString("statusline");
			        String pageconnectknow = bdl.getString("pageconnectknow");
					
					//final String murl = mUrl;final String mhp = mHP;
			        	//Thread eb = new Thread(){public void run(){
			        	if( pageconnectknow == null || mhpb.contains(pageconnectknow) ){
			        	
			        		easyStatus("Acquired "+titlr+" in "+(SystemClock.uptimeMillis()-sh)/1000+" secs.\n"+dest);
			        	//{Message mxm = new Message(); Bundle bxb = new Bundle(); bxb.putString("subtitle","Acquired "+titlr+" in "+(SystemClock.uptimeMillis()-sh)/1000+" secs.\n"+dest ); mxm.setData(bxb);easyViewerHandler.sendMessageDelayed(mxm,10);}
						//}};eb.start();
			      		//mReg = getSharedPreferences("Preferences",MODE_WORLD_WRITEABLE);mEdt = mReg.edit();
			      		//mEdt.putString(loc, mHP);mEdt.commit();
			        	//{Message mx = new Message();Bundle bxb = new Bundle();// bxb.putString("string", loc+",");bxb.putString(loc, mHP);
			      		//bxb.putString("long", "lasthttp");bxb.putLong("lasthttp", System.currentTimeMillis());
			      		// mx.setData(bxb);setrefHandler.sendMessageDelayed(mx,50);}//*/
			        	}else if(pageconnectknow != null){easyStatus("Invalid Download");
			        	{Message ml = new Message(); Bundle bl = new Bundle(); bl.putInt("l",2);bl.putString("text",titlr+" downloaded didn't pass acknowledgement("+bdl.getString("pageconnectknow")+"). Lov'n cookies and the rest.");ml.setData(bl);logoly.sendMessageDelayed(ml,75); }
			        	String ct = "";
			        	{Message ml = new Message(); Bundle bxb = new Bundle(); bxb.putString("remove", "connect");  ml.setData(bxb);setrefHandler.sendMessageDelayed(ml, 50);}
						//String[] h = mHP.split("\n");for(int b = 0; b < h.length; b++){{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text","pageknow("+bdl.getString("pageconnectknow")+") "+h[b]);mx.setData(bx);logoly.sendMessageDelayed(mx,pRate);}
						//if(h[b].contains("content1=") ){ct += h[b].substring(h[b].indexOf("content1=")+9, h[b].indexOf(";HS;", h[b].indexOf("content1=")));}}
						//ct = ct.replaceAll("%0a", "\n").trim();
						Log.e(G,"know("+bdl.getString("pageconnectknow")+")");//Log.w(G,"know("+bdl.getString("pageconnectknow")+")"+h[b]);
			        	}
			        {Message ml = new Message(); Bundle bx = new Bundle();bx.putString("text","Downloaded status(" + statusline + ") loc("+loc+") mUrl("+murl+") " + mhpb.length() + " bytes.");ml.setData(bx);logoly.sendMessageDelayed(ml,pRate);}
			    
					
				}
			};
			tx.start();
		//}	
		}
	};
	private Handler logoly = new Handler(){public void handleMessage(Message msg){Bundle bx = msg.getData();int l = bx.getInt("l");String text = bx.getString("text");switch(l){case 2:Log.e(G,":"+text);break;case 3:Log.w(G,":"+text);break;default:Log.i(G,":"+text);break;}}};
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

	private void easyStatus(final String m){
		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text",m);ml.setData(bl);easyStatusHandler.sendMessage(ml);}
	}

	private long essmooth = 0;
	private Handler easyStatusHandler = new Handler(){
		public void handleMessage(Message msg){
		int es = 10;
		if(essmooth > System.currentTimeMillis() - 1750){es = (int)((essmooth + 1750) - System.currentTimeMillis());}
		essmooth = System.currentTimeMillis()+es;
		int esx = es;Bundle bdl = msg.getData();
		
		long freememory = Runtime.getRuntime().freeMemory();
		Log.i(G,"easyStatus("+bdl.getString("text").replaceAll("\n", "   ")+") freememory("+freememory/1024+" Kb) has(" + getListView().hasFocus() +") shown("+getListView().isShown()+") enabled("+getListView().isEnabled()+")");
		if(!getListView().isShown()){
			return;
			//bdl.putString("text", "Interruption Handled");
			//wayGo.sendEmptyMessageDelayed(2,1000);
		}
		Toast.makeText(mCtx, bdl.getString("text"), Toast.LENGTH_LONG).show();
		//{Message ml = new Message();/**/bdl.putString("status", bdl.getString("text"));bdl.putInt("esx", esx);ml.setData(bdl);updateStatus.sendMessageDelayed(ml,esx);}//easyStatus(msg.getData().getString("text"));
	}
	};

	private void easyViewer(final String m, final String s){
		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("title", m);bl.putString("sub", s);ml.setData(bl);easyViewerHandler.sendMessageDelayed(ml,75);}
		
	}

	private long evsmooth = 0;
	private Handler easyViewerHandler = new Handler(){
		public void handleMessage(final Message msg){//runnable,runable,run code here
			//int ev1 = 50;
			if(evsmooth > System.currentTimeMillis()){Bundle bl = msg.getData();Message ml = new Message(); ml.setData(bl);easyViewerHandler.sendMessageDelayed(ml,750);return;} 
			evsmooth = System.currentTimeMillis()+1750;
			//final int ev = ev1;
			
		//	Thread evt = new Thread(){public void run(){
		
		final Bundle bdl = msg.getData();//easyViewer(bx.getString("title"),bx.getString("subtitle"));
		
		
		String m = bdl.getString("title");String s = bdl.containsKey("subtitle")?bdl.getString("subtitle"):bdl.getString("sub");
		
		Toast.makeText(mCtx, m+"\n"+s, Toast.LENGTH_SHORT).show();
		
		//{Message mx = new Message();Bundle bx = new Bundle();bx.putString("title", m);bx.putString("sub", s);mx.setData(bx);updateViewer.sendMessageDelayed(mx,175);}
		//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",m);mx.setData(bx);mTitleHandler.sendMessageDelayed(mx, 75);}
		//{Message mx = new Message(); Bundle bx = new Bundle();bx.putString("text",s);mx.setData(bx);mSubtitleHandler.sendMessageDelayed(mx, 75);}
		
		//}};evt.start();
		}
	};
	private Handler rlColorBg = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id",0);if(id == 0){return;}RelativeLayout v = (RelativeLayout) findViewById(id);if(v != null){int col = bl.getInt("color",Color.CYAN);v.setBackgroundColor(col);}}};
	//private Handler rlColorBg = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id",0);if(id == 0){return;}try{RelativeLayout v = (RelativeLayout) findViewById(id);if(v != null){int col = bl.getInt("color",Color.CYAN);v.setBackgroundColor(col);}}catch(ClassCastException e){e.printStackTrace();return;}}};
	private Handler colorFilterIN = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id",0);if(id == 0){return;}try{ImageView v = (ImageView) findViewById(id);if(v != null){int col = bl.getInt("color",Color.CYAN);int alpha = bl.getInt("alpha",200);v.setColorFilter(col, PorterDuff.Mode.SRC_IN);v.setAlpha(alpha);}}catch(ClassCastException e){e.printStackTrace();return;}}};
	// mPearl.setColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_IN);mPearl.setAlpha(200);
	private Handler pointAt = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id");View v = findViewById(id);int x = bl.getInt("x",10); int y = bl.getInt("y",10); v.setPadding(x-bl.getInt("w")/2, y-bl.getInt("h")/2, 0, 0);}};
	private Handler setColor = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id");try{TextView v = (TextView) findViewById(id);if(bl.containsKey("color")){int co = bl.getInt("color",Color.BLACK);v.setTextColor(co);}}catch(ClassCastException e){Log.e(G,"Wrong target for text color");}}};
	private Handler setText = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id");String t = bl.getString("text");try{TextView v = (TextView) findViewById(id);if(t!=null){if(bl.containsKey("color")){int co = bl.getInt("color",Color.BLACK);v.setTextColor(co);}v.setText(t);}}catch(ClassCastException e){Log.e(G,"Wrong target for text " + t);}/*int x = bl.getInt("x",10); int y = bl.getInt("y",10); int size = bl.getInt("size",10);v.setPadding(x-size/2, y-size/2, 0, 0);/**/}};
	private Handler textUpdate = new Handler(){
	public void handleMessage(Message msg){
		TextView t = (TextView) findViewById(msg.getData().getInt("id"));t.setText(msg.getData().getString("text"));}};
	private Handler setFocusOn = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id");if(bl.containsKey("parentpos")){LinearLayout lv = (LinearLayout) findViewById(bl.getInt("parentpos"));if(lv != null){int pc = lv.getChildCount();int pos = bl.getInt("pos");while(pos > pc){pos-=pc;} View v = lv.getChildAt(pos>0?pos-1:0);if(v!=null){v.requestFocusFromTouch();}}}else{View v = findViewById(id);v.requestFocusFromTouch();}}};
	private Handler setFocusOff = new Handler(){public void handleMessage(Message msg){int id = msg.getData().getInt("id");View v = findViewById(id);v.clearFocus();}};
	private Handler setHidden = new Handler(){public void handleMessage(Message msg){int id = msg.getData().getInt("id");View v = findViewById(id);v.setVisibility(View.INVISIBLE);}};
	private Handler setGone = new Handler(){
	public void handleMessage(Message msg){	int id = msg.getData().getInt("id");
	View v = findViewById(id);if(v == null){return;}v.setVisibility(View.GONE);}
	};
	private Handler setVisible = new Handler(){public void handleMessage(Message msg){int id = msg.getData().getInt("id");View v = findViewById(id);v.setVisibility(View.VISIBLE);}};
    
	
	
    
    
}