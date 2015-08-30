package com.merrillogic.stump.sample;

import android.app.Application;
import android.util.Log;

import com.merrillogic.stump.StumpObserver;
import com.merrillogic.stump.StumpPackage;

import org.jetbrains.annotations.NotNull;


public class SampleApplication extends Application {

	StumpObserver a = new StumpObserver() {
		@Override
		public void onEvent(@NotNull String event, @NotNull Object... args) {
			Log.w("!!!", event);
		}
	};

	@Override
	public void onCreate(){
		super.onCreate();
		StumpPackage.addObserver(a);
		//If you wanted to pass on info to crash tracking, or analytics, or both!
		//Fabric.with(new Crashlytics());
		//Stump.setListener(this);
	}

	@Override
	public void onTrimMemory(int trimLevel) {
		super.onTrimMemory(trimLevel);
	}

//	@Override
	public void onEvent(String event) {
		//Could do this, for example.
		//Crashlytics.log(event);
	}

//	@Override
	public void onDump(String events, String uiStack) {
		//Crashlytics.log(events);
		//Crashlytics.log(uiStack);
	}
}
