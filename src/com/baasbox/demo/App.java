package com.baasbox.demo;


import com.baasbox.android.BaasBox;

import android.app.Application;

public class App extends Application {
	
	public static BaasBox bbox;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		BaasBox.Config config = new BaasBox.Config();
		config.apiDomain = "10.0.0.2";//TODO change to real IP for device or remote server test
		config.authenticationType = BaasBox.Config.AuthType.SESSION_TOKEN;
		
		bbox = BaasBox.initDefault(this, config);
	}
}
