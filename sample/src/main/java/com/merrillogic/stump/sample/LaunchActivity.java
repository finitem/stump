package com.merrillogic.stump.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.merrillogic.stump.StumpPackage;
import com.merrillogic.stump.logging.LoggingPackage;
import com.merrillogic.stump.logging.LoggingRoot;

public class LaunchActivity extends AppCompatActivity{

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_launch);
		StumpPackage.event("LaunchActivity", 0);
		if (icicle != null) {
			StumpPackage.event("Recreating LaunchActivity from icicle", 0);
		}

		findViewById(R.id.cool_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StumpPackage.event("User obeyed.", 0);
				LoggingPackage.broken(new RuntimeException("arg"));
			}
		});

		//If we really just want to make sure we know it if something weird happens, odd is for us
		LoggingPackage.odd("LaunchActivity.onCreate() made it through setup.");
	}

	@Override
	public void onPause() {
		super.onPause();
		//Nothing is actually broken, but this is the sort of thing I might have while developing
		// something or having a specific problem.
		LoggingPackage.dev(
				"Oh shoot we're pausing, does that mean we're broken the way I think we are?");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LoggingRoot.INSTANCE$.uiPopTo("LaunchActivity");
	}
}
