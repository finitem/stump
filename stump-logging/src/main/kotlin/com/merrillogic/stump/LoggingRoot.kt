package com.merrillogic.stump


public object LoggingRoot: StumpObserver {
	override fun onEvent(event: String,
	                     vararg args: Any) {
		throw UnsupportedOperationException()
	}
}
