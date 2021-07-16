package com.wesender.common.log

class ObjectUtil {

    companion object {

        fun hexHash(obj: Any?): String {
            return if (obj == null) "" else String.format("%s@0x%s", obj.javaClass.simpleName,
                    Integer.toHexString(obj.hashCode()))
        }
    }
}