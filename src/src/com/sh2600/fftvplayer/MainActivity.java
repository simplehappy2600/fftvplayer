package com.sh2600.fftvplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final SharedPreferences sharedPreferences = getSharedPreferences(CVal.CONFIG, 0) ;
		final EditText etPortTv = (EditText)findViewById(R.id.etPortTv);
		final EditText etIPTv = (EditText)findViewById(R.id.etIPTv);
		final TextView tvInfo = (TextView)findViewById(R.id.tvInfo);
		final CheckBox cbUseSysPlayer = (CheckBox)findViewById(R.id.cbUseSysPlayer);
		final CheckBox cbUseMulticast = (CheckBox)findViewById(R.id.cbUseMulticast);
		
		etPortTv.setText(String.valueOf(sharedPreferences.getInt(CVal.PREF.PORT, CVal.DEFAULT_PORT)));
		etIPTv.setText(sharedPreferences.getString(CVal.PREF.IP, ""));
		cbUseSysPlayer.setChecked(sharedPreferences.getBoolean(CVal.PREF.USE_SYS_PLAYER, false));
		cbUseMulticast.setChecked(sharedPreferences.getBoolean(CVal.PREF.USE_MULTICAST, false));
		
		findViewById(R.id.btnSaveTv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sharedPreferences.edit()
					.putInt(CVal.PREF.PORT, Integer.parseInt(etPortTv.getText().toString()))
					.putBoolean(CVal.PREF.USE_SYS_PLAYER, cbUseSysPlayer.isChecked())
					.putBoolean(CVal.PREF.USE_MULTICAST, cbUseMulticast.isChecked())
					.commit();
				Toast.makeText(MainActivity.this, "保存完成", Toast.LENGTH_SHORT).show();
			}
		});
		
		findViewById(R.id.btnSavePhone).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sharedPreferences.edit()
					.putInt(CVal.PREF.PORT, Integer.parseInt(etPortTv.getText().toString()))
					.putString(CVal.PREF.IP, etIPTv.getText().toString())
					.putBoolean(CVal.PREF.USE_MULTICAST, cbUseMulticast.isChecked())
					.commit();
				Toast.makeText(MainActivity.this, "保存完成", Toast.LENGTH_SHORT).show();
			}
		});
		
		int port = sharedPreferences.getInt(CVal.PREF.PORT, 0);
		if (port > 0){
			Intent service = new Intent(this, HttpService.class);
			startService(service);
			tvInfo.setText("TV service started.");
		} else {
			tvInfo.setText("function phone.");
		}
		
		findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				android.content.Intent intent = new android.content.Intent("com.sh2600.fftvplayer.action.DS_RECV");
//				intent.addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//				intent.putExtra("path", "path2334");
//				intent.putExtra("keys", "");
//				intent.putExtra("values", "");
//				sendBroadcast(intent);
			}
		});		
		
	}
}
