package com.sh2600.fftvplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private EditText etLog;
	
	public final static String BROADCASTRECEIVER_LOG = "com.sh2600.fftvplayer.BROADCASTRECEIVER_LOG";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		registerReceiver(mBroadcastReceiver, new IntentFilter(BROADCASTRECEIVER_LOG));
		
		final SharedPreferences sharedPreferences = getSharedPreferences(CVal.CONFIG, 0) ;
		final EditText etPortTv = (EditText)findViewById(R.id.etPortTv);
		final EditText etIPTv = (EditText)findViewById(R.id.etIPTv);
		final CheckBox cbUseSysPlayer = (CheckBox)findViewById(R.id.cbUseSysPlayer);
		final RadioButton cbUseMulticast = (RadioButton)findViewById(R.id.cbUseMulticast);
		final RadioButton cbUseHttp = (RadioButton)findViewById(R.id.cbUseHttp);
		final RadioButton cbTv = (RadioButton)findViewById(R.id.cbTv);
		final RadioButton cbPhone = (RadioButton)findViewById(R.id.cbPhone);
		etLog = (EditText)findViewById(R.id.etLog);
		
		etPortTv.setText(String.valueOf(sharedPreferences.getInt(CVal.PREF.PORT, CVal.DEFAULT_PORT)));
		etIPTv.setText(sharedPreferences.getString(CVal.PREF.TvIP, ""));
		cbUseSysPlayer.setChecked(sharedPreferences.getBoolean(CVal.PREF.USE_SYS_PLAYER, false));
		
		boolean useMulticast = sharedPreferences.getBoolean(CVal.PREF.USE_MULTICAST, true);
		cbUseMulticast.setChecked(useMulticast);
		cbUseHttp.setChecked(!useMulticast);
		
		cbTv.setChecked(sharedPreferences.getBoolean(CVal.PREF.EnableTv, true));
		cbPhone.setChecked(sharedPreferences.getBoolean(CVal.PREF.EnablePhone, false));
		
		findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (cbPhone.isChecked() && cbUseHttp.isChecked() 
						&& etIPTv.getText().length() == 0){
					Toast.makeText(MainActivity.this, "电视的IP?", Toast.LENGTH_SHORT).show();
					return;
				}
				
				sharedPreferences.edit()
					.putString(CVal.PREF.TvIP, etIPTv.getText().toString())
					.putInt(CVal.PREF.PORT, Integer.parseInt(etPortTv.getText().toString()))
					.putBoolean(CVal.PREF.USE_SYS_PLAYER, cbUseSysPlayer.isChecked())
					.putBoolean(CVal.PREF.USE_MULTICAST, cbUseMulticast.isChecked())
					.putBoolean(CVal.PREF.EnableTv, cbTv.isChecked())
					.putBoolean(CVal.PREF.EnablePhone, cbPhone.isChecked())
					.commit();
				Toast.makeText(MainActivity.this, "保存完成", Toast.LENGTH_SHORT).show();
				
				restart();
			}
		});
		
		if (sharedPreferences.getBoolean(CVal.PREF.EnableTv, false)){
			Intent service = new Intent(this, HttpService.class);
			startService(service);
			etLog.append("function tv\n");
		} else {
			etLog.append("function phone\n");
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(mBroadcastReceiver);
	}
	
	private void restart(){
		try{
			Intent service = new Intent(this, HttpService.class);
			stopService(service);
		}
		catch(Exception e){}
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getStringExtra("msg");
			etLog.append(msg);
			etLog.append("\n");
		}
	};
}
