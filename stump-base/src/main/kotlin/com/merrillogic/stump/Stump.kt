package com.merrillogic.stump

import java.util.concurrent.CopyOnWriteArrayList

/*
	TODO:
		Add annotations from support library
		Add rx capabilities?
 */

public interface StumpObserver {
	public fun onEvent(event: String,
	                   //Default to 0 length Array of ints if they haven't provided anything
	                   vararg args: Any = Array(0, {false}))
}

//CopyOnWriteArray is probably more efficient than alternatives given the rarity of adding new
// event listeners (pretty much just on startup), and the frequency of traversing (lots)
// Also it can't be changed from underneath our iterators, so that's nice
private val sObservers = CopyOnWriteArrayList<StumpObserver>()

fun addObserver(observer: StumpObserver) {
	sObservers.add(observer)
}

fun removeObserver(observer: StumpObserver) :Boolean{
	return sObservers.remove(observer)
}

fun event(event: String, extras: Any) {
	//Yeah, we're creating an iterator here - but I like that more than copying and iterating that?
	for (observer in sObservers) {
		observer.onEvent(event, extras)
	}
}
