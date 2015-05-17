package com.merrillogic.stump;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Stump {

	//TODO: Memory usage concerns, output, etc.

	private static final Object sUiStackLock = new Object();
	private static List<String> sEventList = new LinkedList<>();
	private static Stack<String> sUiStateStack = new Stack<>(); //Or instead I could just use a linkedlist, and then when I'm popping I don't change anything until I find what I need, then can convert it all.
	private static String sDefaultTag = "STUMP";
	private static String sEventTag = "StumpEvent";

	public static void event(String event) {
		sEventList.add(event);
		if (BuildConfig.DEBUG) {
			Log.d(sEventTag, event);
		}
		//TODO: crashlytics? Analytics? Callback?
	}

	public static void uiEvent(String uiEvent) {
		sUiStateStack.add(uiEvent);
	}

	/**
	 * Clear the ui stack up to and including the given string. You should be very certain that
	 * the string is in the stack, or else the entire thing will be cleared.
	 * @param uiEvent
	 */
	public static void uiPopTo(String uiEvent) {
		boolean done = false;

		StringBuilder poppedSummaryBuilder = new StringBuilder();
		//TODO: Make this a resource - not everyone develops in English!
		poppedSummaryBuilder.append("UiPop(").append(uiEvent).append(") [");

		String poppedItem;
		synchronized (sUiStackLock) {
			while (!done && !sUiStateStack.empty()) {
				poppedItem = sUiStateStack.pop();
				poppedSummaryBuilder.append(poppedItem);
				done = uiEvent.equals(poppedItem);
				if (!done) {
					poppedSummaryBuilder.append(" -> ");
				}
			}
		}
		event(poppedSummaryBuilder.toString());
	}

	/**
	 * A function to log things when you just want to see what the heck is going on
	 *
	 * @param logStatement
	 */
	public static void dev(String logStatement) {
		if (BuildConfig.DEBUG) {
			Log.d("DEVTEST!!!", logStatement);
		}
	}

	/**
	 * A function for logging things that are generally good to be aware of while debugging code.
	 *
	 * @param logStatement
	 */
	//ANALYZE: Do we need this? Event basically does the same thing.
	public static void debug(String logStatement) {
		if (BuildConfig.DEBUG) {
			Log.d("Useful", logStatement);
		}
	}

	/**
	 * A function for logging things that shouldn't be happening, but we want to know about them if
	 * they are
	 *
	 * @param logStatement The description of the odd thing happening
	 */
	public static void confusion(String logStatement) {
		if (BuildConfig.DEBUG) {
			Log.w("Oddity", logStatement);
		}
	}

	public static void broken(Throwable error) {
		Log.e("Error", error.toString());
		error.printStackTrace();
		//TODO: Send eventlist and current ui stack to 3rd party callback/observable?
		//At this point, adding event list and ui stack to any sort of 3rd party analytics would be cool.
	}

	// Copy functionality of Android Log class, in case anyone wants those

	public static void v(String logStatement) {
		v(sDefaultTag, logStatement);
	}

	public static void v(String tag, String logStatement) {
		Log.v(tag, logStatement);
	}

	public static void d(String logStatement) {
		d(sDefaultTag, logStatement);
	}

	public static void d(String tag, String logStatement) {
		Log.d(tag, logStatement);
	}

	public static void i(String logStatement) {
		i(sDefaultTag, logStatement);
	}

	public static void i(String tag, String logStatement) {
		Log.i(tag, logStatement);
	}

	public static void w(String logStatement) {
		w(sDefaultTag, logStatement);
	}

	public static void w(String tag, String logStatement) {
		Log.w(tag, logStatement);
	}

	public static void e(String logStatement) {
		e(sDefaultTag, logStatement);
	}

	public static void e(String tag, String logStatement) {
		Log.e(tag, logStatement);
	}

}
