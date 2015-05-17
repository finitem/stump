package com.merrillogic.stump;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Stump {

	//TODO: Memory usage concerns, output, threading conflicts, etc.

	private static final Object sUiStackLock = new Object();

	private static List<String> sEventList = new LinkedList<>();
	private static Stack<String> sUiStateStack = new Stack<>(); //Or instead I could just use a linkedlist, and then when I'm popping I don't change anything until I find what I need, then can convert it all.

	private static String sDefaultTag = "STUMP";
	private static String sDevTag = "DEV!!!";
	private static String sDebugTag = "USEFUL";
	private static String sOddTag = "Odd";
	private static String sErrorTag = "Error";
	private static String sEventTag = "StumpEvent";
	private static String sUiStartString = "UiPop";
	private static String sUiJoinerString = " -> ";

	public static void setWording(String defaultTag,
	                                String devTag,
	                                String debugTag,
	                                String oddTag,
	                                String errorTag,
	                                String eventTag,
	                                String uiPopStartString,
	                                String uiJoinerStartString) {
		sDefaultTag = defaultTag;
		sDevTag = devTag;
		sDebugTag = debugTag;
		sOddTag = oddTag;
		sErrorTag = errorTag;
		sEventTag = eventTag;
		sUiStartString = uiPopStartString;
		sUiJoinerString = uiJoinerStartString;
	}

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
	 *
	 * @param uiEvent
	 */
	public static void uiPopTo(String uiEvent) {
		boolean done = false;

		StringBuilder poppedSummaryBuilder = new StringBuilder();
		//TODO: Make this a resource - not everyone develops in English!
		poppedSummaryBuilder.append(sUiStartString).append("(").append(uiEvent).append(") [");

		String poppedItem;
		synchronized (sUiStackLock) {
			while (!done && !sUiStateStack.empty()) {
				poppedItem = sUiStateStack.pop();
				poppedSummaryBuilder.append(poppedItem);
				done = uiEvent.equals(poppedItem);
				if (!done) {
					poppedSummaryBuilder.append(sUiJoinerString);
				}
			}
		}
		event(poppedSummaryBuilder.toString());
	}

	/**
	 * A function to log things when you just want to see what the heck is going on. Logs at the
	 * warn so that it still appears if you've set your filter to higher levels. Does not output
	 * unless it is a debug build, so it is technically safe to leave it in release code. Should
	 * still probably never make it into committed code - use event() instead.
	 *
	 * @param logStatement The statement to be output into the log.
	 */
	public static void dev(String logStatement) {
		if (BuildConfig.DEBUG) {
			Log.w(sDevTag, logStatement);
		}
	}

	/**
	 * A function for logging things that are generally good to be aware of while debugging code.
	 *
	 * @param logStatement The statement to be output into the log.
	 */
	//ANALYZE: Do we need this? Event basically does the same thing.
	public static void debug(String logStatement) {
		if (BuildConfig.DEBUG) {
			Log.d(sDebugTag, logStatement);
		}
	}

	/**
	 * A function for logging things that shouldn't be happening, but we want to know about them if
	 * they are. Does not output unless it's a debug build - can be safely left in release code.
	 *
	 * @param logStatement The description of the odd thing happening, to be output into the log.
	 */
	public static void odd(String logStatement) {
		if (BuildConfig.DEBUG) {
			Log.w(sOddTag, logStatement);
		}
	}

	public static void broken(Throwable error) {
		Log.e(sErrorTag, error.toString());
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
