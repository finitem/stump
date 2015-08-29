package com.merrillogic.stump

/*
	TODO:
		Add annotations from support library - assuming that doesn't  make the minSdk something bad
		Add rx capabilities, again assuming we don't muck up the minSdk
		Use timestamps for events? Or at least, relative times.
 */

public object StoringRoot : StumpObserver {
	override fun onEvent(event: String,
	                     vararg args: Any) {
		throw UnsupportedOperationException()
	}

	//TODO: Memory usage concerns, output, threading conflicts, etc.

	private val sUiStackLock = Object()

	private val sEventList = LinkedList<String>()
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

	public fun event(event: String) {
		sEventList.add(event)
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
	private fun dump() {
		val events = getIndentedIteratedString("Event Stream",
		                                       sEventList)
		val uiStack = getIndentedIteratedString("Ui Stack",
		                                        sUiStateStack)
		Timber.d(events)
		Timber.d(uiStack)
		//Make a local pointer to that object so that it can't be swapped out from under us
		listener?.onDump(events,
		                 uiStack)
	}

	private fun getIndentedIteratedString(title: String,
	                                      items: Iterable<String>): String {
		val builder = StringBuilder()
		builder.append(title).append(":\n")
		for (event in items) {
			builder.append("\t").append(event).append("\n")
		}
		return builder.toString()
	}
}
