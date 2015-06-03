package com.merrillogic.stump.sample;

import android.app.Application;

import com.merrillogic.stump.Stump;

public class SampleApplication extends Application implements Stump.StumpListener {

	@Override
	public void onCreate(){
		super.onCreate();
		//If you wanted to pass on info to crash tracking, or analytics, or both!
		//Fabric.with(new Crashlytics());
		//Stump.setListener(this);
	}

	@Override
	public void onTrimMemory(int trimLevel) {
		super.onTrimMemory(trimLevel);
		Stump.trimMemory(trimLevel);
	}

	@Override
	public void onEvent(String event) {
		//Could do this, for example.
		//Crashlytics.log(event);
	}

	@Override
	public void onDump(String events, String uiStack) {
		//Crashlytics.log(events);
		//Crashlytics.log(uiStack);
	}
}
