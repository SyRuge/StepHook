package com.xcx.stephook

import android.app.Activity
import android.util.Log
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Create By Ruge at 2019-10-10
 */
class RpcCall {

    companion object {
        val TAG = "xcx"
        var rpcCallMethod: Method? = null
        var getResponseMethod: Method? = null
        var curH5Fragment: Any? = null
        var curH5PageImpl: Any? = null
        var h5Activity: Activity? = null

        operator fun invoke(
            loader: ClassLoader,
            args0: String?,
            args1: String?
        ): Any? {
            if (rpcCallMethod == null) {
                try {
                    val rpcClazz: Class<*> =
                        loader.loadClass(com_alipay_mobile_nebulabiz_rpc_H5RpcUtil)
                    val aF: Field = curH5Fragment!!.javaClass.getDeclaredField(
                        a
                    )
                    aF.isAccessible = true
                    val viewHolder: Any = aF.get(curH5Fragment)
                    val hF: Field = viewHolder.javaClass.getDeclaredField(h)
                    hF.isAccessible = true
                    curH5PageImpl = hF.get(viewHolder)
                    val h5PageClazz: Class<*>? =
                        loader.loadClass(com_alipay_mobile_h5container_api_H5Page)
                    val jsonClazz: Class<*>? =
                        loader.loadClass(com_alibaba_fastjson_JSONObject)
                    if (curH5PageImpl != null) {
                        rpcCallMethod = rpcClazz.getMethod(
                            rpcCall,
                            String::class.java,
                            String::class.java,
                            String::class.java,
                            Boolean::class.javaPrimitiveType, jsonClazz,
                            String::class.java,
                            Boolean::class.javaPrimitiveType, h5PageClazz,
                            Int::class.javaPrimitiveType,
                            String::class.java,
                            Boolean::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType
                        )
                        Log.d(TAG, "get Old RpcCallMethod successfully")
                    }
                } catch (t: Throwable) {
                    Log.d(TAG, "get Old RpcCallMethod err:")
                    //Log.printStackTrace(TAG, t);

                }
            }

            if (rpcCallMethod == null) {
                try {
                    val aF: Field = curH5Fragment!!.javaClass.getDeclaredField(a)
                    aF.isAccessible = true
                    val viewHolder: Any = aF.get(curH5Fragment)
                    val hF: Field = viewHolder.javaClass.getDeclaredField(h)
                    hF.isAccessible = true
                    curH5PageImpl = hF.get(viewHolder)
                    val h5PageClazz: Class<*>? =
                        loader.loadClass(com_alipay_mobile_h5container_api_H5Page)
                    val jsonClazz: Class<*>? =
                        loader.loadClass(com_alibaba_fastjson_JSONObject)
                    val rpcClazz: Class<*> =
                        loader.loadClass(com_alipay_mobile_nebulaappproxy_api_rpc_H5RpcUtil)
                    if (curH5PageImpl != null) {
                        rpcCallMethod = rpcClazz.getMethod(
                            rpcCall,
                            String::class.java,
                            String::class.java,
                            String::class.java,
                            Boolean::class.javaPrimitiveType, jsonClazz,
                            String::class.java,
                            Boolean::class.javaPrimitiveType, h5PageClazz,
                            Int::class.javaPrimitiveType,
                            String::class.java,
                            Boolean::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            String::class.java
                        )
                        Log.d(TAG, "get RpcCallMethod successfully")
                    }
                } catch (t: Throwable) {
                    Log.d(TAG, "get RpcCallMethod err:")
                    Log.e(TAG, Log.getStackTraceString(t))
                }
            }

            Log.i(TAG, "rpcCall params count: ${rpcCallMethod!!.parameterTypes.size}")

            if (rpcCallMethod!!.parameterTypes.size == 13){
                return rpcCallMethod!!.invoke(
                    null,
                    args0,
                    args1,
                    "",
                    true,
                    null,
                    null,
                    false,
                    curH5PageImpl,
                    0,
                    "",
                    false,
                    -1,
                    ""
                )
            }

            return rpcCallMethod!!.invoke(
                null,
                args0,
                args1,
                "",
                true,
                null,
                null,
                false,
                curH5PageImpl,
                0,
                "",
                false,
                -1
            )
        }

        fun getResponse(resp: Any): String {
            if (getResponseMethod == null) {
                getResponseMethod =
                    resp.javaClass.getMethod(getResponse)
            }
            return getResponseMethod!!.invoke(resp) as String
        }
    }
}