package com.wesender.win

import java.lang.ref.WeakReference
import kotlin.reflect.KClass

object Controllers {

    private val mControllers by lazy { mutableMapOf<KClass<*>, WeakReference<*>>() }

    fun put(controller: Any) {
        val key = getKey(controller)
        val ref = mControllers[key]
        if (ref?.get() == null) {
            mControllers[key] = WeakReference(controller)
        }
    }

    fun <T : Any> get(clazz: KClass<T>): T? {
        val ref = mControllers[clazz]
        val controller = ref?.get()
        if (controller != null && controller::class == clazz) {
            return controller as T
        }
        else {
            mControllers.remove(clazz)
        }
        return null
    }

    fun clear() {
        mControllers.clear()
    }

    private fun getKey(controller: Any): KClass<out Any> = controller::class
}