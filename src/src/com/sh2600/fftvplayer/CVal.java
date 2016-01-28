package com.sh2600.fftvplayer;

public interface CVal {
	
	String KEY_PATH 	= "path";
	String KEY_KEYS 	= "keys";
	String KEY_VALUES 	= "values";
	String KEY_SEGMENTS	= "segments";
	String KEY_CACHEDIR = "cacheDir";
	
	int DEFAULT_PORT = 8899;
	String CONFIG = "config";
	interface PREF {
		String USE_SYS_PLAYER 	= "usesysplayer";
		String USE_MULTICAST 	= "use_multicast";
		String PORT 			= "port";
		String TvIP 			= "tvip";
		String EnableTv 		= "enabletv";
		String EnablePhone 		= "enablephone";
	}
}
