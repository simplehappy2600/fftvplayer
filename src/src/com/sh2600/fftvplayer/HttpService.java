package com.sh2600.fftvplayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

public class HttpService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	final static String TAG = HttpService.class.getSimpleName();
	
	interface MSG {
		int InitHttp		= 1;
		int InitMulticast	= 2;
		
	}

	private SharedPreferences mSharedPreferences;
	private AsyncHttpServer mServer;
	private MulticastSocket socket;
	private InetAddress group;
	private int mPort = 8899;
	private boolean use_sys_player;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case MSG.InitHttp:
				initHttp();
				break;
			case MSG.InitMulticast:
				initMulticast();
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onCreate() {
		
		mSharedPreferences = getSharedPreferences(CVal.CONFIG, 0);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		mPort = mSharedPreferences.getInt(CVal.PREF.PORT, CVal.DEFAULT_PORT);
		use_sys_player = mSharedPreferences.getBoolean(CVal.PREF.USE_SYS_PLAYER, false);
		
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		if (mSharedPreferences.getBoolean(CVal.PREF.USE_MULTICAST, false)){
			mServiceHandler.sendEmptyMessage(MSG.InitMulticast);
		} else {
			mServiceHandler.sendEmptyMessage(MSG.InitHttp);
		}
	}
	
	private void initHttp(){
		mServer = new AsyncHttpServer();
		mServer.post("/", new HttpServerRequestCallback() {
		    @Override
		    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
		    	Log.i(TAG, "recv play request data");
		    	JSONObjectBody body = (JSONObjectBody)request.getBody();
		    	JSONObject json = body.get();
		    	Bundle bundle = new Bundle();
		    	
    			bundle.putString(CVal.KEY_PATH, json.optString(CVal.KEY_PATH));
				bundle.putStringArray(CVal.KEY_KEYS, (String[])json.opt(CVal.KEY_KEYS));
		    	bundle.putStringArray(CVal.KEY_VALUES, (String[])json.opt(CVal.KEY_VALUES));
		    	bundle.putStringArray(CVal.KEY_SEGMENTS, (String[])json.opt(CVal.KEY_SEGMENTS));
		    	bundle.putString(CVal.KEY_CACHEDIR, json.optString(CVal.KEY_CACHEDIR));					
		    	
		        response.send("OK");
		        play(bundle);
		    }
		});

		mServer.listen(mPort);
	}
	
	private void play(Bundle bundle){
        if (use_sys_player){
	        String path = bundle.getString(CVal.KEY_PATH);
	        if (path != null && path.length() > 0){
	        	Log.i(TAG, "start player, use sys player");
		        Intent intent = new Intent(Intent.ACTION_VIEW);
		        String type = "video/* ";
		        Uri uri = Uri.parse(path);
		        intent.setDataAndType(uri, type);
		        startActivity(intent);   		       
	        } 	
        } else {
        	//String path = "http://vplay.aixifan.com/des/20151221/3005289_mp4/3005289_480p.mp4?k=e1fb720dd1d9742a0bc309f0114e7d98&t=1450920450";
        	String path = bundle.getString(CVal.KEY_PATH);
        	Log.i(TAG, "start local player");
	        Intent intent = new Intent(HttpService.this, PlayerActivity.class);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	        intent.putExtras(bundle);
	        intent.putExtra("path", path);
			startActivity(intent);			        
        }	
	}
	
	private void initMulticast(){
		try {
			group = InetAddress.getByName("228.5.6.7");
			socket = new MulticastSocket(mPort);
			socket.joinGroup(group);
			
			byte[] buf = new byte[64*1024];
			int pos = 0;
			ByteBuffer bb = ByteBuffer.wrap(buf);
			
			while(true){
				try{
					if (socket.isClosed()){
						return;
					}
					DatagramPacket pkg = new DatagramPacket(buf, pos, buf.length - pos);
					socket.receive(pkg);
					pos += pkg.getLength();
					
					bb.position(0);
					int len = bb.getInt();
					if (pos >= len){
						//
						ByteArrayInputStream bs = new ByteArrayInputStream(buf, 4, len - 4);
						ObjectInputStream ois = new ObjectInputStream(bs);
						Object o = ois.readObject();
						HashMap<String, Object> map = (HashMap<String, Object>)o;
						
				    	Bundle bundle = new Bundle();
				    	
		    			bundle.putString(CVal.KEY_PATH, (String)map.get(CVal.KEY_PATH));
						bundle.putStringArray(CVal.KEY_KEYS, (String[])map.get(CVal.KEY_KEYS));
				    	bundle.putStringArray(CVal.KEY_VALUES, (String[])map.get(CVal.KEY_VALUES));
				    	bundle.putStringArray(CVal.KEY_SEGMENTS, (String[])map.get(CVal.KEY_SEGMENTS));
				    	bundle.putString(CVal.KEY_CACHEDIR, (String)map.get(CVal.KEY_CACHEDIR));	
				    	
						play(bundle);
						
						if (pos > len){
							System.arraycopy(buf, len, buf, 0, pos - len);
						}
						
						pos -= len;
					}	
				}
				catch(Exception e){
					Log.e(TAG, "", e);
					pos = 0;
				}
			}
			
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
//		Message msg = mServiceHandler.obtainMessage();
//		msg.arg1 = startId;
//		mServiceHandler.sendMessage(msg);
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		try {
			if (mServer != null){
				mServer.stop();
				mServer = null;
			}
			if (socket != null){
				socket.leaveGroup(group);
				socket.close();
				socket = null;
			}			
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
		//mServiceHandler.sendEmptyMessage(MSG.QUIT);
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		mServiceLooper.quit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (CVal.PREF.PORT.equalsIgnoreCase(key)){
			mPort = sharedPreferences.getInt(CVal.PREF.PORT, 8899);
			//mServiceHandler.sendEmptyMessage(MSG.RESTART);
			try {
				if (mServer != null){
					mServer.stop();
					mServer.listen(mPort);
				}
				if (socket != null){
					socket.leaveGroup(group);
					socket.close();
					socket = null;
					mServiceHandler.sendEmptyMessageDelayed(MSG.InitMulticast, 600);
				}			
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}	
		}
		
		if (CVal.PREF.USE_SYS_PLAYER.equalsIgnoreCase(key)){
			use_sys_player = sharedPreferences.getBoolean(CVal.PREF.USE_SYS_PLAYER, true);
		}
	}
}
