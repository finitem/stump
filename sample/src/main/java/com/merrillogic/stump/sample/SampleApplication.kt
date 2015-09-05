package com.merrillogic.stump.sample;

import android.app.Application
import android.util.Log
import com.merrillogic.stump
import com.merrillogic.stump.logging.LoggingRoot
import timber.log.Timber

public class SampleApplication : Application() {

	var listener: LoggingRoot.Listener = object : LoggingRoot.Listener {
		override fun onDump(events: String,
		                    ui: String) {
			Log.w("DUUUUMP",
			      events)
			Log.w("UI",
			      ui)
		}
	}

	override fun onCreate() {
		super.onCreate();
		stump.addObserver(LoggingRoot)
		//If you wanted to pass on info to crash tracking, or analytics, or both!
		//Fabric.with(new Crashlytics());
		LoggingRoot.addListener(listener);
		Timber.plant(Timber.DebugTree())
	}

	override fun onTrimMemory(trimLevel: Int) {
		super.onTrimMemory(trimLevel);
	}

	//	@Override
	//	public void onEvent(String event) {
	//Could do this, for example.
	//Crashlytics.log(event);
	//	}

	//	@Override
	//	public void onDump(String events, String uiStack) {
	//Crashlytics.log(events);
	//Crashlytics.log(uiStack);
	//	}
}
