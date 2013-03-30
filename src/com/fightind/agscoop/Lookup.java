package com.fightind.agscoop;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Lookup extends Activity {

	private static String TAG = "Lookup";
	
	private WebView mContent;
	//private TextView mTitle, mDate;
	//private LinearLayout mLinearLayout;
	private String mLink;
	//private String mLookup;
	//private SharedPreferences mReg;
	//private Editor mEdt;
	private Context mCtx;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mLog = new Custom(this);
		mCtx = getApplicationContext();
		setContentView(R.layout.browser);
		{s01.sendEmptyMessage(2);}
	}
	
	Handler s01 = new Handler(){
		public void handleMessage(Message msg){
		//Bundle bdl = msg.getData();
		
		mContent = (WebView) findViewById(R.id.browser_viewer);
		mContent.setBackgroundColor(Color.BLACK);
		
		ImageView ob = (ImageView) findViewById(R.id.browser_overback);
		ob.setOnClickListener(new OnClickListener(){public void onClick(View v){wayGo.sendEmptyMessage(2);}});
		TextView mTitle = (TextView) findViewById(R.id.browser_title);
		mTitle.setOnClickListener(new OnClickListener(){public void onClick(View v){ wayForward.sendEmptyMessage(2); }});
		
		//mContent.setVisibility(View.INVISIBLE);
		//mHideData.sendEmptyMessageDelayed(1, 10);
		
		//mReg = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
        //mEdt = mReg.edit();
        //String url = mReg.getString("url","");
        //mEdt.putLong("id", id); mEdt.commit();


		Bundle mIntentExtras = getIntent().getExtras();
		long id = mIntentExtras != null && mIntentExtras.containsKey("id") ? mIntentExtras.getLong("id") : 0;
		Uri geturi = mIntentExtras != null && mIntentExtras.containsKey("uri") ? Uri.parse(mIntentExtras.getString("uri")) : null;
		
		//mLinearLayout = (LinearLayout) findViewById(R.id.browser);
		
		
		//mDate = (TextView) findViewById(R.id.browser_date);
	
		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", "Loading"); bl.putInt("id", R.id.browser_title); ml.setData(bl); setText.sendMessage(ml);}
		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", ""); bl.putInt("id", R.id.browser_date); ml.setData(bl); setText.sendMessage(ml);}
		
		Uri contenturi = Uri.withAppendedPath(DataProvider.CONTENT_URI, "moment");
		
		if(geturi == null || id > 0){
			//mEdt.putLong("id", id);mEdt.commit();
			//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putLong("id", id); ml.setData(bl); loadRecord.sendMessage(ml); }
			
			geturi = Uri.withAppendedPath(contenturi, "#"+id);
		}else if(geturi != null){
			
		}
	
		Log.w(TAG,"loadRecord id("+id+") uri("+geturi+")");
		{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("uri", geturi.toString()); ml.setData(bl); loadRecord.sendMessage(ml); }
		
		}
	};

	private void easyLoadData(String html){
		Message msg = new Message();
		Bundle bdl = new Bundle();
		bdl.putString("html", html);
		msg.setData(bdl);
		mLoadData.sendMessage(msg);
	}
	private void easyLoadData(String url, String html){
		Message msg = new Message();
		Bundle bdl = new Bundle();
		bdl.putString("url", url);
		bdl.putString("html", html);
		msg.setData(bdl);
		mLoadData.sendMessage(msg);
	}
	private Handler mLoadData = new Handler(){
		public void handleMessage(Message msg){
			Bundle bdl = msg.getData();
			String html = bdl.containsKey("html") ? bdl.getString("html") : "";
			String url = bdl.containsKey("url") ? bdl.getString("url") : "";
			mContent.getSettings().setJavaScriptEnabled(true);
			if( url.length() > 0 ){
				Log.w(TAG,"Loaded page with baseurl " + url);
				mContent.loadDataWithBaseURL(url, html, "text/html", "UTF-8", url);
			}else{
				Log.w(TAG,"Loaded page");
				mContent.loadData(html, "text/html", "UTF-8");
			}
			mLookatData.sendEmptyMessageDelayed(2,75);
		}
	};
	
	private Handler mLookatData = new Handler(){
		public void handleMessage(Message msg){
			mContent.setVisibility(View.VISIBLE);
			mContent.requestFocus();
		}
	};
	
	/*
	private Handler mHideData = new Handler(){
		public void handleMessage(Message msg){
			Bundle bdl = msg.getData();
			//String html = bdl.containsKey("html") ? bdl.getString("html") : "";
			//mContent.loadData(html, "text/html", "UTF-8");
			mContent.setVisibility(View.INVISIBLE);
		}
	};//*/

   // private void loadRecord(long id) {
    	
    	private Handler loadRecord = new Handler(){
    		public void handleMessage(Message msg){
    			Bundle bdl = msg.getData();
    			Uri geturi = Uri.parse(bdl.getString("uri"));
    			//long id = bdl.getLong("id");
    	//if( id == mLookup ){ return; }
    	
    			{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", "Loading"); bl.putInt("id", R.id.browser_title); ml.setData(bl); setText.sendMessage(ml);}
				{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", ""); bl.putInt("id", R.id.browser_date); ml.setData(bl); setText.sendMessage(ml);}
    			
    	//mTitle.setText("");
		//mDate.setText("");
		//easyLoadData("<html>Loading</html>");
		//easyLoadData("<html><body bgcolor=#000000 text=#e0e0e0 link=#0066cc vlink=#cc6600><h3><center>Loading</center></h1></body></html>");
    	//mContent.loadData("<html><body bgcolor=#000000 text=#e0e0e0 link=#0066cc vlink=#cc6600><h3><center>Loading</center></h1></body></html>", "text/html", "UTF-8");
    	//mContent.setVisibility(View.INVISIBLE);
    	
				
		//
		// CUSOMIZED		
		//		
		Cursor lCursor = SqliteWrapper.query(mCtx, mCtx.getContentResolver(), geturi, //Uri.withAppendedPath(DataProvider.CONTENT_URI,"moment"), 
        		//new String[] { "_id", "address", "body", "strftime(\"%Y-%m-%d %H:%M:%S\", date, \"unixepoch\", \"localtime\") as date" },
        		//strftime("%Y-%m-%d %H:%M:%S"
        		new String[] {"_id", "title", "url", "strftime('%Y/%m/%d %H:%M',published) as published", "author", "content"  },
				//new String[] { "_id", "address", "body", "date" },
        		"status > 0",
        		null, 
        		null);
		// EC
		
		if( lCursor != null ){
			startManagingCursor(lCursor);
			if ( lCursor.moveToFirst() ){
				String title = null;
				String link = null;
				String published = null;
				String author = null;
				//String content = null;
				//mLookup = link;
				if( lCursor.getColumnCount() > 5 ){/// <<<<<<<<<<<<<<<<<  LOOK HERE
					title = lCursor.getString(1) != null ? lCursor.getString(1) : "";
					link = lCursor.getString(2) != null ? lCursor.getString(2) : "";
					published = lCursor.getString(3) != null ? lCursor.getString(3) : "";
					author = lCursor.getString(4) != null ? lCursor.getString(4) : "";
					String content = lCursor.getString(5) != null ? lCursor.getString(5) : "";
					content = content.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
					if(content.indexOf("![CDATA[") > 0){
					content = content.substring(content.indexOf("![CDATA[")+8, content.lastIndexOf("]]>", content.length() - 4));}
					if(content.contentEquals("unavail") ){content = "";}
					//content = content.replaceAll("br>", "BR>");
					//if(content.indexOf("&ndash;") >0 ){content = content.replaceAll("&ndash;", "-");}
					//if(content.indexOf("&nbsp;") >0 ){content = content.replaceAll("&nbsp;", " ");}
					
					if( author.length() == 0 ){
						author = "Author Not Stated";
					}
					Log.w(TAG,"Found uri("+geturi+") title("+title+") content("+content+")");

					{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", title); bl.putInt("id", R.id.browser_title); ml.setData(bl); setText.sendMessage(ml);}
					{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text", published); bl.putInt("id", R.id.browser_date); ml.setData(bl); setText.sendMessage(ml);}
					//mTitle.setText(title);
					//mDate.setText(published);

					//mContent.getSettings().supportMultipleWindows();
					
					//link + "#contenttopoff",<div style=\"font-size:24px;\"><b>"+title+"</b></div>
					
					//
					// CUSTOMIZED
					//
					String html = "<html><style>body {font-size: 24px;}</style><body bgcolor=#000000 text=#b0F0E0 link=#00cc66 vlink=#0066cc><div style=\"font-size:16px;\">"+published + "</div><hr noshade><a name=contenttop></a><div style=\"text-align:justify;\">"+content + "</div><hr noshade>\n<a href=\""+Motion.dest+"\" style=\"font-size:16px;\">"+ Motion.title + "</a><br>\n<a href=\""+link+"\" style=\"font-size:16px;\">"+ title + "</a><br>\n<br>\n<br>\n</body></html>";//, "text/html", "UTF-8", link);
					// EC
					//mContent.getSettings().setSupportZoom(true);
					
					mLink = link;

					easyLoadData(link,html);
					//mContent.loadData(html, "text/html", "utf-8");
					//mContent.reload();
					//ContentValues cv = new ContentValues();
					//cv.put("status", 2);
					//SqliteWrapper.update(this, getContentResolver(), DataProvider.CONTENT_URI, cv, "_id = " + id, null);
				}
			}
			//mBrowser.addJavascriptInterface(new AndroidBridge(), "android");
		}
    	}
    };

    
    private Handler setText = new Handler(){public void handleMessage(Message msg){Bundle bl = msg.getData();int id = bl.getInt("id");String t = bl.getString("text");try{TextView v = (TextView) findViewById(id);if(t!=null){if(bl.containsKey("color")){int co = bl.getInt("color",Color.BLACK);v.setTextColor(co);}v.setText(t);}}catch(ClassCastException e){Log.e(TAG,"Wrong target for text " + t);}/*int x = bl.getInt("x",10); int y = bl.getInt("y",10); int size = bl.getInt("size",10);v.setPadding(x-size/2, y-size/2, 0, 0);/**/}};
    
    

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		//menu.add(0, 401, 0, "View Article Link")
			//.setIcon(android.R.drawable.ic_menu_view);
        menu.add(0, 402, 0, "Forward")
			.setIcon(android.R.drawable.ic_dialog_email);
		return super.onCreatePanelMenu(featureId, menu);
	}


	@Override
	public View onCreatePanelView(int featureId) {
		// TODO Auto-generated method stub
		return super.onCreatePanelView(featureId);
	}

	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		mLog.w(TAG,"onOptionsItemSelected()");
		
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final String link = mLink; 
		TextView mTitle = (TextView) findViewById(R.id.browser_title);
		 

    	
		switch(item.getItemId()){
		case 401:
			Intent d = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			startActivity(d);
			break;
		case 402:
			{
				mTitle.performClick();
				//{Message ml = new Message(); Bundle bl = new Bundle(); bl.putString("text","\n\n\n"+date + "\n"+title+"\n" +link + "\n\n\nAndroid\n"); bl.putString("title", "FW: " + title); ml.setData(bl); wayForward.sendMessage(ml);}

			
				
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	Handler wayForward = new Handler(){
		public void handleMessage(Message msg){
			TextView mTitle = (TextView) findViewById(R.id.browser_title);
			TextView mDate = (TextView) findViewById(R.id.browser_date);
			final String title = mTitle.getText().toString(); 
			final String published = mDate.getText().toString();
			final String link = mLink;
			
			wayGo.sendEmptyMessageDelayed(2,1000);
			
			
			Intent jump = new Intent(Intent.ACTION_SEND);
			jump.putExtra(Intent.EXTRA_TEXT, "\n\n\n"+published + "\n"+title+"\n" +link + "\n\n\nAndroid\n" ); 
			jump.putExtra(Intent.EXTRA_SUBJECT, "FW: " + title );
			jump.setType("message/rfc822"); 
			startActivity(Intent.createChooser(jump, "Email"));

			
		}
	};
	

	Handler wayGo = new Handler(){
		public void handleMessage(Message msg){
			finish();
		}
	};

	
	@Override
	protected void onPause() {
//		mLog.w(TAG,"onPause() ++++++++++++++++++++++++++++++++");
		//easyLoadData("<html></html>");
		//mHideData.sendEmptyMessage(2);
		// TODO Auto-generated method stub
		super.onPause();
	}

	
    
}

