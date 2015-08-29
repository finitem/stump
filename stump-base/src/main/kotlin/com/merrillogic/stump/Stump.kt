package com.merrillogic.stump

import java.util.LinkedList
import java.util.Stack
import java.util.concurrent.CopyOnWriteArrayList

/*
	TODO:
		Add annotations from support library - assuming that doesn't  make the minSdk something bad
		Add rx capabilities, again assuming we don't muck up the minSdk
		Use timestamps for events? Or at least, relative times.
 */

public interface StumpObserver {
	public fun onEvent(event: String,
	                   vararg args: Any)
}

//CopyOnWriteArray is probably more efficient than alternatives given the rarity of adding new
// event listeners (pretty much just on startup), and the frequency of traversing (lots)
private val sObservers = CopyOnWriteArrayList<StumpObserver>()

fun addObserver(observer: StumpObserver) {
	sObservers.add(observer)
}

fun removeObserver(observer: StumpObserver) :Boolean{
	return sObservers.remove(observer)
}

fun event(event: String, extras: Any) {
	//Make a local pointer to that object so that it can't be swapped out from under us
	val currentObservers: List<StumpObserver> = sObservers
	for (observer in currentObservers) {
		observer.onEvent(event, extras)
	}
}
