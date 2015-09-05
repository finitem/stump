package com.merrillogic.stump

import timber.log.Timber
import java.util.*

/**
 * A function to log things when you just want to see what the heck is going on. Logs at the
 * warn level so that it still appears if you've set your filter to higher levels. Does not
 * output unless it is a debug build, so it is technically safe to leave it in release code.
 * Should still probably never make it into committed code - use event() instead.

 * @param logStatement The statement to be output into the log.
 */
public fun Timber.dev(logStatement: String) {
	Timber.w("!!!",
	         logStatement)
}

/**
 * A function for logging things that shouldn't be happening, but we want to know about them if
 * they are.

 * @param logStatement The description of the odd thing happening, to be output into the log.
 */
public fun Timber.odd(logStatement: String) {
	Timber.w("???",
	         logStatement)
}

public fun Timber.broken(error: Throwable) {
	val errorString = error.toString()
	Timber.e("***",
	         error)
	event(errorString + Arrays.toString(error.getStackTrace()))
	LoggingRoot.dump()
}

/*
	TODO:
		Add annotations from support library
		Add rx capabilities, again assuming we don't muck up the minSdk
		Use timestamps for events? Or at least, relative times.
 */

data class Event(val eventName:String, val params: Array<out Any>)

public object LoggingRoot : StumpObserver {

	interface Listener {
		public fun onDump(events: String, ui: String);
	}

	override fun onEvent(event: String,
	                     vararg args: Any) {
		sEventList.add(Event(event, args))
	}

	//TODO: Memory usage concerns, output, threading conflicts, etc.

	private val sUiStackLock = Object()
	private val sListeners = ArrayList<Listener>()

	private val sEventList = LinkedList<Event>()
	private val sUiStateStack = Stack<String>() //Or instead I could just use a linkedlist, and then when I'm popping I don't change anything until I find what I need, then can convert it all.

	private var sEventTag = "^^^"
	private var sUiString = "[ ]"
	private var sUiAddString = "++"
	private var sUiPopSting = "--"
	private var sUiJoinerString = " -> "
	private var sMemoryTrimString = "||]/"

	/**
	 * Sets the key words to be output to the log at various points. Useful if you code in a
	 * language other than English or just prefer different phrasing.

	 * @param eventTag
	 * *
	 * @param uiString
	 * *
	 * @param uiAddSTring
	 * *
	 * @param uiPopString
	 * *
	 * @param uiJoinerStartString
	 * *
	 * @param memoryTrimString
	 */
	public fun setWording(
			eventTag: String,
			uiString: String,
			uiAddSTring: String,
			uiPopString: String,
			uiJoinerStartString: String,
			memoryTrimString: String) {
		sEventTag = eventTag
		sUiString = uiString
		sUiAddString = uiAddSTring
		sUiPopSting = uiPopString
		sUiJoinerString = uiJoinerStartString
		sMemoryTrimString = memoryTrimString
	}

	/**
	 * In the hopes of preserving some small amount of memory, dumps the event and uiStack, then
	 * clears the event list. If you're app has been running a long time or you have surprisingly
	 * long event names, this could free up a bit of memory.

	 * @param memoryLevel
	 */
	public fun trimMemory(memoryLevel: Int) {
		event(sMemoryTrimString + "(" + memoryLevel + ")")
		dump()
		sEventList.clear()
	}

	public fun uiEvent(uiEvent: String) {
		synchronized (sUiStackLock) {
			sUiStateStack.add(uiEvent)
		}
		//This constructs a stringbuilder for us, which is something to be aware of.
		event(sUiString + " " + sUiAddString + " " + uiEvent)
	}

	/**
	 * Clear the ui stack up to and including the given string. You should be very certain that
	 * the string is in the stack, or else the entire thing will be cleared.
	 *
	 *
	 * Logs the pop event to the event list.

	 * @param uiEvent The ui tag string to pop up to (inclusive).
	 */
	public fun uiPopTo(uiEvent: String) {
		var done = false

		val poppedSummaryBuilder = StringBuilder()
		poppedSummaryBuilder.append(sUiString).append(" ").append(sUiPopSting).append("(").append(uiEvent).append(") [")

		var poppedItem: String
		synchronized (sUiStackLock) {
			while (!done && !sUiStateStack.empty()) {
				poppedItem = sUiStateStack.pop()
				poppedSummaryBuilder.append(poppedItem)
				done = uiEvent == poppedItem
				if (!done) {
					poppedSummaryBuilder.append(sUiJoinerString)
				}
			}
		}
		event(poppedSummaryBuilder.toString())
	}

	/**
	 * Write entire event stream to log, also transmits it to the listener
	 */
	//ANALYZE: memory concerns for constructing potentially massive string
	public fun dump() {
		val events = getIndentedIteratedString("Event Stream",
		                                       sEventList)
		val uiStack = getIndentedIteratedString("Ui Stack",
		                                        sUiStateStack)
		Timber.d(events)
		Timber.d(uiStack)
		//Make a local pointer to that object so that it can't be swapped out from under us
		for (listener in sListeners) {
			listener?.onDump(events,
			                 uiStack)
		}
	}

	private fun getIndentedIteratedString(title: String,
	                                      items: Iterable<Any>): String {
		val builder = StringBuilder()
		builder.append(title).append(":\n")
		for (event in items) {
			builder.append("\t").append(event.toString()).append("\n")
		}
		return builder.toString()
	}
}
