package com.xcx.stephook

import android.app.Activity
import java.lang.reflect.Method

/**
 * Create By Ruge at 2019-10-10
 */
class RpcCall {

    companion object{
        val TAG = "RpcCall"
        var rpcCallMethod: Method? = null
        var getResponseMethod: Method? = null
        var curH5Fragment: Any? = null
        var curH5PageImpl: Any? = null
        var h5Activity: Activity? = null


        fun getResponse(resp: Any): String {
            if (getResponseMethod == null) {
                getResponseMethod =
                    resp.javaClass.getMethod(getResponse)
            }
            return getResponseMethod!!.invoke(resp) as String
        }

    }

}