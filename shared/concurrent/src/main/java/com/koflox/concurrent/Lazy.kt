package com.koflox.concurrent

fun <T> lazyUnsafe(initializer: () -> T) = lazy(mode = LazyThreadSafetyMode.NONE) { initializer.invoke() }
