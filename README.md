![](https://raw.githubusercontent.com/simplehappy2600/raw/master/fftvplayer/images/fftvplayer.PNG)

手机端接收如下广播:

    android.content.Intent intent = new android.content.Intent("com.sh2600.fftvplayer.action.DS_RECV");
	intent.addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
	intent.putExtra("path", path); //path是播放地址, 如http://xxxxx.cn/xx.mp4
	intent.putExtra("keys", keys);
	intent.putExtra("values", values);
	mContext.sendBroadcast(intent);

通过http或者广播方式发送给远端(如TV)，远端接收到后启动播发器播放.

使用的lib:

- [https://github.com/koush/AndroidAsync](https://github.com/koush/AndroidAsync "AndroidAsync")
- [https://github.com/yixia/VitamioBundle](https://github.com/yixia/VitamioBundle "Vitamio")
    
    
