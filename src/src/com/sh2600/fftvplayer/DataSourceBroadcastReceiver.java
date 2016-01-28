package com.sh2600.fftvplayer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

public class DataSourceBroadcastReceiver extends BroadcastReceiver {
	
	final static String TAG = DataSourceBroadcastReceiver.class.getSimpleName();
	
	private int mPort;
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(CVal.CONFIG, 0) ;
		mPort = sharedPreferences.getInt(CVal.PREF.PORT, CVal.DEFAULT_PORT);
		if (sharedPreferences.getBoolean(CVal.PREF.USE_MULTICAST, false)){
			multicast(context, intent);
		} else {
			http(context, sharedPreferences, intent);
		}
	}
	
	private void http(Context context, SharedPreferences sharedPreferences, Intent intent){
		
		Utils.sendMsg(context, "recv play, send http");
		
		String ip = sharedPreferences.getString(CVal.PREF.TvIP, "");
		if (ip.length() == 0){
			Toast.makeText(context, "tv ip not set", Toast.LENGTH_SHORT).show();
			return;
		}
		
		AsyncHttpPost post = new AsyncHttpPost(String.format("http://%s:%d", ip, mPort));
		JSONObject json = new JSONObject();
		try {
			json.put(CVal.KEY_PATH, intent.getStringExtra(CVal.KEY_PATH));
			json.put(CVal.KEY_KEYS, intent.getStringArrayExtra(CVal.KEY_KEYS));
			json.put(CVal.KEY_VALUES, intent.getStringArrayExtra(CVal.KEY_VALUES));
			json.put(CVal.KEY_SEGMENTS, intent.getStringArrayExtra(CVal.KEY_SEGMENTS));
			json.put(CVal.KEY_CACHEDIR, intent.getStringExtra(CVal.KEY_CACHEDIR));
		} catch (JSONException e) {
			Log.e(TAG, "", e);
		}
		
		post.setBody(new JSONObjectBody(json));
		AsyncHttpClient.getDefaultInstance().execute(post, new HttpConnectCallback() {
			@Override
			public void onConnectCompleted(Exception ex, AsyncHttpResponse response) {			
				if (ex != null){
					Log.e(TAG, "", ex);
					//Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show(); //error
					return;
				}
				
				Log.e(TAG, response.toString());
			}
		});	
	}
	
	private void multicast(Context context, Intent intent){
		
		Utils.sendMsg(context, "recv play, send multicast");
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CVal.KEY_PATH, intent.getStringExtra(CVal.KEY_PATH));
		map.put(CVal.KEY_KEYS, intent.getStringArrayExtra(CVal.KEY_KEYS));
		map.put(CVal.KEY_VALUES, intent.getStringArrayExtra(CVal.KEY_VALUES));
		map.put(CVal.KEY_SEGMENTS, intent.getStringArrayExtra(CVal.KEY_SEGMENTS));
		map.put(CVal.KEY_CACHEDIR, intent.getStringExtra(CVal.KEY_CACHEDIR));
		
		new MulticastTask().execute(map);
	}
	
	class MulticastTask extends AsyncTask<HashMap<String, Object>, Void, Void> {

		@Override
		protected Void doInBackground(HashMap<String, Object>... params) {
			try{
				
				HashMap<String, Object> map = params[0];
				
				ByteArrayOutputStream bs = new ByteArrayOutputStream();
				bs.write(0);
				bs.write(0);
				bs.write(0);
				bs.write(0);
				ObjectOutputStream oos = new ObjectOutputStream(bs);
				oos.writeObject(map);
				oos.close();
				byte[] data = bs.toByteArray();
				ByteBuffer bb = ByteBuffer.wrap(data);
				bb.putInt(data.length);
				 
				InetAddress group = InetAddress.getByName("228.5.6.7");
				MulticastSocket socket = new MulticastSocket(mPort);
				socket.joinGroup(group);
				DatagramPacket p = new DatagramPacket(data, data.length, group, mPort);
				socket.send(p);
				socket.close();
			}
			catch(Exception e){
				Log.e(TAG, "", e);	
			}
			
			return null;
		}
		
	}
}
