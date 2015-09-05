package com.merrillogic.stump

import java.util.concurrent.CopyOnWriteArrayList

public interface StumpObserver {
	fun onEvent(event: String,
	                   vararg args: Any)
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

fun event(event: String, extras: Any = Array(0, {0})) {
	//Yeah, we're creating an iterator here - but I like that more than copying and iterating that?
	for (observer in sObservers) {
		observer.onEvent(event, extras)
	}
}
