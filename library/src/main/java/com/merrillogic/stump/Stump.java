package com.merrillogic.stump;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/*
	TODO:
		Add annotations from support library - assuming that doesn't  make the minSdk something bad
		Add rx capabilities, again assuming we don't muck up the minSdk
		Use timestamps for events? Or at least, relative times.
 */


public class Stump {

	/**
	 * A StumpListener is notified when various logging happens in the stump class.
	 * <p/>
	 * <p/>
	 * If a tree has already fallen and someone is listening... Is there even a question?
	 */
	public interface StumpListener {
		void onEvent(String event);

		void onDump(String events, String uiStack);
	}

	//TODO: Memory usage concerns, output, threading conflicts, etc.

	private static final Object sUiStackLock = new Object();

	private static List<String> sEventList = new LinkedList<>();
	private static Stack<String> sUiStateStack = new Stack<>(); //Or instead I could just use a linkedlist, and then when I'm popping I don't change anything until I find what I need, then can convert it all.

	private static WeakReference<StumpListener> sListenerWeakReference = new WeakReference<>(null);

	private static String sDefaultTag = "STUMP";
	private static String sDevTag = "DEV!!!";
	private static String sDebugTag = "USEFUL";
	private static String sOddTag = "Odd";
	private static String sErrorTag = "Error";
	private static String sEventTag = "StumpEvent";
	private static String sUiString = "Ui";
	private static String sUiAddString = "Add";
	private static String sUiPopSting = "Pop";
	private static String sUiJoinerString = " -> ";
	private static String sMemoryTrimString = "Memory";

	/**
	 * Sets the key words to be output to the log at various points. Useful if you code in a
	 * language other than English or just prefer different phrasing.
	 *
	 * @param defaultTag
	 * @param devTag
	 * @param debugTag
	 * @param oddTag
	 * @param errorTag
	 * @param eventTag
	 * @param uiString
	 * @param uiAddSTring
	 * @param uiPopString
	 * @param uiJoinerStartString
	 * @param memoryTrimString
	 */
	public static void setWording(String defaultTag,
								  String devTag,
								  String debugTag,
								  String oddTag,
								  String errorTag,
								  String eventTag,
								  String uiString,
								  String uiAddSTring,
								  String uiPopString,
								  String uiJoinerStartString,
								  String memoryTrimString) {
		sDefaultTag = defaultTag;
		sDevTag = devTag;
		sDebugTag = debugTag;
		sOddTag = oddTag;
		sErrorTag = errorTag;
		sEventTag = eventTag;
		sUiString = uiString;
		sUiAddString = uiAddSTring;
		sUiPopSting = uiPopString;
		sUiJoinerString = uiJoinerStartString;
		sMemoryTrimString = memoryTrimString;
	}

	/**
	 * Sets the listener that responds to onDump() and onEvent() calls. A weak reference to the
	 * listener is used, so that it doesn't need to be manually cleared to avoid leaking attached
	 * contexts and the like (because it very well may retain one).
	 *
	 * @param listener The listener that wants to receive notification about events in the event
	 *                 stream.
	 */
	public static void setListener(StumpListener listener) {
		sListenerWeakReference = new WeakReference<>(listener);
	}

	public static void event(String event) {
		sEventList.add(event);
		if (BuildConfig.DEBUG) {
			Log.d(sEventTag, event);
		}
		//Make a local pointer to that object so that it can't be swapped out from under us
		WeakReference<StumpListener> reference = sListenerWeakReference;
		if (reference.get() != null) {
			reference.get().onEvent(event);
		}
	}

	/**
	 * In the hopes of preserving some small amount of memory, dumps the event and uiStack, then
	 * clears the event list. If you're app has been running a long time or you have surprisingly
	 * long event names, this could free up a bit of memory.
	 *
	 * @param memoryLevel
	 */
	public static void trimMemory(int memoryLevel) {
		event(sMemoryTrimString + "(" + memoryLevel + ")");
		dump();
		sEventList.clear();
	}

	public static void uiEvent(String uiEvent) {
		synchronized (sUiStackLock) {
			sUiStateStack.add(uiEvent);
		}
		//This constructs a stringbuilder for us, which is something to be aware of.
		event(sUiString + " " + sUiAddString + " " + uiEvent);
	}

	/**
	 * Clear the ui stack up to and including the given string. You should be very certain that
	 * the string is in the stack, or else the entire thing will be cleared.
	 * <p/>
	 * Logs the pop event to the event list.
	 *
	 * @param uiEvent The ui tag string to pop up to (inclusive).
	 */
	public static void uiPopTo(String uiEvent) {
		boolean done = false;

		StringBuilder poppedSummaryBuilder = new StringBuilder();
		poppedSummaryBuilder.append(sUiString)
				.append(" ")
				.append(sUiPopSting)
				.append("(")
				.append(uiEvent)
				.append(") [");

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
	 * Write entire event stream to log, also transmits it to the listener
	 */
	//ANALYZE: memory concerns for constructing potentially massive string
	private static void dump() {
		String events = getIndentedIteratedString("Event Stream", sEventList);
		String uiStack = getIndentedIteratedString("Ui Stack", sUiStateStack);
		debug(events);
		debug(uiStack);
		//Make a local pointer to that object so that it can't be swapped out from under us
		WeakReference<StumpListener> reference = sListenerWeakReference;
		StumpListener listener = reference.get();
		if (listener != null) {
			listener.onDump(events, uiStack);
		}
	}

	private static String getIndentedIteratedString(String title, Iterable<String> items) {
		StringBuilder builder = new StringBuilder();
		builder.append(title).append(":\n");
		for (String event : items) {
			builder.append("\t").append(event).append("\n");
		}
		return builder.toString();
	}

	/**
	 * A function to log things when you just want to see what the heck is going on. Logs at the
	 * warn level so that it still appears if you've set your filter to higher levels. Does not
	 * output unless it is a debug build, so it is technically safe to leave it in release code.
	 * Should still probably never make it into committed code - use event() instead.
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
		String errorString = error.toString();
		Log.e(sErrorTag, errorString);
		error.printStackTrace();
		//Is there a way to be more efficient with the stack trace print and stringing?
		event(errorString + "\n" + Arrays.toString(error.getStackTrace()));
		//ANALYZE: Right now this means we're printing this error to logs THREE times (but only in
		// debug mode, so maybe it's acceptable?)
		dump();
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
