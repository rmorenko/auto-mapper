package com.morenko.automapper


inline fun <reified T, reified V> V.setPrivateField(name: String, value: T): V {
    val field = V::class.java.getDeclaredField(name)
    field.isAccessible = true
    field.set(this, value)
    return this
}
