package com.xcx.stephook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.*

/**
 * Create By Ruge at 2019-07-27
 */
class MyStepHook : IXposedHookLoadPackage {

    private var isNeedRandom = true
    private val TAG = "MyStepHook"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "com.eg.android.AlipayGphone") {
            hookAlipayStep(lpparam)
        } else if (lpparam.packageName == "com.tencent.mm") {
            hookWechatStep(lpparam)
        }
        if (lpparam.packageName == "com.eg.android.AlipayGphone") {
            Log.d(TAG, lpparam.packageName)
            //hookSecurity(lpparam);

            hookRpcCall(lpparam)
        }

    }

    private fun hookRpcCall(lpparam: LoadPackageParam) {
        val loader: ClassLoader = lpparam.classLoader
        var clazz: Class<*>? = null
        try {
            clazz = loader.loadClass(com_alipay_mobile_nebulacore_ui_H5FragmentManager)
            val h5FragmentClazz: Class<*>? =
                loader.loadClass(com_alipay_mobile_nebulacore_ui_H5Fragment)
            XposedHelpers.findAndHookMethod(clazz, pushFragment, h5FragmentClazz,
                Boolean::class.java, Bundle::class.java, Boolean::class.java,
                Boolean::class.java, object : XC_MethodHook() {

                    override fun afterHookedMethod(param: MethodHookParam) {
                        Log.i(TAG, "cur fragment: " + param.args[0])
                        RpcCall.curH5Fragment = param.args[0]
                    }
                })
            Log.d(TAG, "hook $pushFragment successfully")
        } catch (t: Throwable) {
            Log.d(TAG, "hook $pushFragment err:")
//            Log.printStackTrace(TAG, t)
        }
        try {
            clazz = loader.loadClass(com_alipay_mobile_nebulacore_ui_H5Activity)
            XposedHelpers.findAndHookMethod(clazz, onResume, object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    Log.i(TAG, "cur activity: " + param.thisObject)
                    val act = param.thisObject as Activity
                    if (RpcCall.h5Activity !== act) {
//                            Config.shouldReloadConfig = true
                    }
                    RpcCall.h5Activity = act
                }
            })
            Log.d(TAG, "hook $onResume successfully")
        } catch (t: Throwable) {
            Log.d(TAG, "hook $onResume err:")
//            Log.printStackTrace(TAG, t)
        }
        var hookRpcCallSuccess = false
        try {
            clazz = loader.loadClass(com_alipay_mobile_nebulabiz_rpc_H5RpcUtil)
            val h5PageClazz: Class<*>? =
                loader.loadClass(com_alipay_mobile_h5container_api_H5Page)
            val jsonClazz: Class<*>? =
                loader.loadClass(com_alibaba_fastjson_JSONObject)


            XposedHelpers.findAndHookMethod(clazz,
                rpcCall,
                String::class.java,
                String::class.java,
                String::class.java,
                Boolean::class.javaPrimitiveType,
                jsonClazz,
                String::class.java,
                Boolean::class.javaPrimitiveType,
                h5PageClazz,
                Int::class.javaPrimitiveType,
                String::class.java,
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType, object : XC_MethodHook() {

                    override fun afterHookedMethod(param: MethodHookParam) {
                        afterHookRpcCall(param, loader)
                    }
                })
            hookRpcCallSuccess = true
            Log.i(
                TAG,
                "hook old $rpcCall successfully"
            )
        } catch (t: Throwable) {
            Log.e(TAG, "hook old $rpcCall err: $t")
            //Log.printStackTrace(TAG, e);

        }
        if (!hookRpcCallSuccess) {
            try {
                clazz = loader.loadClass(com_alipay_mobile_nebulaappproxy_api_rpc_H5RpcUtil)
                val h5PageClazz: Class<*>? =
                    loader.loadClass(com_alipay_mobile_h5container_api_H5Page)
                val jsonClazz: Class<*>? =
                    loader.loadClass(com_alibaba_fastjson_JSONObject)
                XposedHelpers.findAndHookMethod(clazz, rpcCall,
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
                    String::class.java, object : XC_MethodHook() {
                        @Throws(
                            Throwable::class
                        )
                        override fun afterHookedMethod(param: MethodHookParam) {
                            afterHookRpcCall(param, loader)
                        }
                    })
                Log.i(
                    TAG, "hook $rpcCall successfully"
                )
            } catch (t: Throwable) {
                Log.i(
                    TAG,
                    "hook $rpcCall err:"
                )
//            Log.printStackTrace(TAG, t)
            }
        }

    }

    private fun hookStartActivity(lpparam: XC_LoadPackage.LoadPackageParam) {

        val c1 = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader)
        XposedBridge.hookAllMethods(c1, "startActivity", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                XposedBridge.log("MyStepHook: $intent")
                super.beforeHookedMethod(param)
            }
        })

        val c2 = XposedHelpers.findClass("android.content.Intent", lpparam.classLoader)

        XposedBridge.hookAllMethods(c2, "putExtra", object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                val key = param.args[0] as String
                val value = param.args[1].toString()
                XposedBridge.log("MyStepHook: key = $key, value = $value")
                super.afterHookedMethod(param)
                XposedBridge.log("MyStepHook: key = $key, value = $value")
            }
        })

        XposedBridge.hookAllMethods(c2, "putExtras", object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                val b = param.args[0] as Bundle
                XposedBridge.log("MyStepHook: putExtras: $b")
                super.afterHookedMethod(param)
            }
        })


    }

    private fun hookAlipayStep(lpparam: XC_LoadPackage.LoadPackageParam) {
        val c1 = XposedHelpers.findClass(
            "android.hardware.SystemSensorManager\$SensorEventQueue",
            lpparam.classLoader
        )
        XposedBridge.hookAllMethods(c1, "dispatchSensorEvent", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val arg = param.args[1] as FloatArray

                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val min = cal.get(Calendar.MINUTE)
                val second = cal.get(Calendar.SECOND)

                if (hour < 6) {//凌晨不记步
                    (param.args[1] as FloatArray)[0] = 0f
                    isNeedRandom = true

                } else if (hour >= 23 && min >= 30) {//超过23:30点不记步

                    if (isNeedRandom) {
                        isNeedRandom = false
                        //增加一个随机步数 防止每天步数都一样
                        val random = Random()
                        val ranStep = random.nextInt(50) + 100
                        (param.args[1] as FloatArray)[0] = arg[0] + ranStep
                    }
                } else {//正常计步
                    isNeedRandom = true
                    val step = (hour - 6) * 60 * 60 + min * 60 + second
                    (param.args[1] as FloatArray)[0] = step.toFloat()
                }
                super.beforeHookedMethod(param)
            }

        })

    }

    private fun hookWechatStep(lpparam: XC_LoadPackage.LoadPackageParam) {
        val c1 = XposedHelpers.findClass(
            "android.hardware.SystemSensorManager\$SensorEventQueue",
            lpparam.classLoader
        )
        XposedBridge.hookAllMethods(c1, "dispatchSensorEvent", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val arg = param.args[1] as FloatArray

                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val min = cal.get(Calendar.MINUTE)
                val second = cal.get(Calendar.SECOND)


                if (hour < 6) {//凌晨不记步
                    (param.args[1] as FloatArray)[0] = 0f
                    isNeedRandom = true

                } else if (hour >= 22) {//超过22点不记步
                    if (isNeedRandom) {
                        isNeedRandom = false
                        //增加一个随机步数 防止每天步数都一样
                        val random = Random()
                        val ranStep = random.nextInt(50) + 100
                        (param.args[1] as FloatArray)[0] = arg[0] + ranStep
                    }
                } else {//正常计步
                    isNeedRandom = true
                    val step = (hour - 6) * 60 * 60 + min * 60 + second
                    if (step <= 60000) {
                        (param.args[1] as FloatArray)[0] = step.toFloat()
                    }
                }

                XposedBridge.log("hook wechat now step: " + (param.args[1] as FloatArray)[0])

                super.beforeHookedMethod(param)
            }
        })
    }

    private fun testHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod("com.xcx.myandroiddemo.MainActivity",
            lpparam.classLoader, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                    super.beforeHookedMethod(param)
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                    super.afterHookedMethod(param)

                    try {
                        val c = lpparam.classLoader.loadClass("com.xcx.myandroiddemo.MainActivity")
                        val field = c.getDeclaredField("bt_test_view")
                        field.isAccessible = true
                        //                                XposedBridge.log("zzz");
                        val bt = field.get(param!!.thisObject) as Button
                        bt.text = "xcx"
                    } catch (e: Exception) {
                        XposedBridge.log(e.toString())

                    }

                }
            })
    }

    private fun afterHookRpcCall(param: MethodHookParam, loader: ClassLoader) {
        val args0 = param.args[0] as String
        val args1 = param.args[1] as String
        if (args0 == null || !args0.contains("forest") && !args0.contains("antfarm") &&
            !args0.contains("antmember")
        ) {
            Log.d(TAG, "afterHookRpcCall null")
            return
        }
        Log.d(TAG,"===============args0===================")
        Log.d(TAG, "$args0")
        Log.d(TAG,"===============args0===================")
        Log.i(TAG,"===============args1===================")
        Log.i(TAG, "$args1")
        Log.i(TAG,"===============args1===================")
        val resp: Any? = param.result
        if (resp != null) {
            val response = RpcCall.getResponse(resp)
            Log.d(TAG,"===============response===================")
            Log.d(TAG, "response: $response")
            Log.d(TAG,"===============response===================")
            AntForest.saveUserIdAndName(args0, response)
            AntForest.start(loader, args0, args1, response)


        }
    }
}