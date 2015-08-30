package com.merrillogic.stump.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.merrillogic.stump.StumpPackage;

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
//				Stump.broken(new RuntimeException("arg"));
			}
		});

		//If we really just want to know that the onCreate() method succeeds, but don't care about
		// having it in our event stream, debug is for us!
//		Stump.debug("LaunchActivity.onCreate() made it through setup.");
	}

	@Override
	public void onPause() {
		super.onPause();
		//Nothing is actually broken, but this is the sort of thing I might have while developing
		// something or having a specific problem.
//		Stump.dev("Oh shoot we're pausing, does that mean we're broken the way I think we are?");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		Stump.uiPopTo("LaunchActivity");
	}
}
