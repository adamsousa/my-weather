package com.github.adamsousa.myweather.custom

class Event<T>(private val mContent: T) {
    private var mHasBeenHandled = false
    fun hasBeenHandled(): Boolean {
        return mHasBeenHandled
    }

    val contentIfNotHandled: T?
        get() = if (mHasBeenHandled) {
            null
        } else {
            mHasBeenHandled = true
            mContent
        }

    fun peekContent(): T {
        return mContent
    }
}