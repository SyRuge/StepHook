package com.xcx.stephook

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * Create By Ruge at 2019-10-10
 */
class AntForest {

    companion object {
        val TAG = "xcx"
        private var hasMore = true
        private var collectedEnergy = 0

        fun saveUserIdAndName(args0: String, resp: String?) {
            if (args0 != "alipay.antmember.forest.h5.queryNextAction") {
                return
            }
            try {
                var jo = JSONObject(resp)
                if (jo.has("userEnergy")) {
                    jo = jo.getJSONObject("userEnergy")
                    val userName: String? = jo.getString("displayName")
                    var loginId = userName
                    if (jo.has("loginId")) {
                        loginId += "(" + jo.getString("loginId") + ")"
                    }
                    if (loginId == null || loginId.isEmpty()) {
                        loginId = "*null*"
                    }
//                    Config.putIdMap(jo.getString("userId"), loginId)
//                    Log.recordLog("进入【$loginId】的蚂蚁森林", "")
//                    Config.saveIdMap()
                }
            } catch (t: Throwable) {
                Log.i(TAG, "saveUserIdAndName err:")
//                Log.printStackTrace(TAG, t)
            }
        }


        fun start(loader: ClassLoader, args0: String, args1: String, resp: String) {

            if (args0 != "alipay.antmember.forest.h5.queryNextAction" || args1.contains("\"userId\"")) {
                return
            }

            Log.i(TAG, "===========start===========")
            queryEnergyRanking(loader, "1")
        }

        private fun queryEnergyRanking(loader: ClassLoader, startPoint: String) {

            thread {
                try {
                    var friend = JSONArray()
                    var needQuery = true
                    var point = startPoint

                    while (needQuery) {
                        Log.i(TAG, "===========start rpcCall_queryEnergyRanking===========")
                        val s: String? = rpcCall_queryEnergyRanking(loader, point)
                        Log.i(TAG, "===========end rpcCall_queryEnergyRanking===========")
                        val jo = JSONObject(s)
                        Log.d(TAG, "response: $s")
                        Log.i(TAG, "================")
                        val resultCode = jo.getString("resultCode")
                        if (resultCode != "SUCCESS") {
                            Log.e(TAG, jo.getString("resultDesc"))
                            Log.e(TAG, s)
                            break
                        }
                        val ja: JSONArray = jo.getJSONArray("friendRanking")
                        friend = joinJSONArray(friend, ja)
                        Log.e(TAG, "========================")
                        Log.d(TAG, "friend length: ${friend.length()}")
                        Log.e(TAG, "========================")
                        needQuery = jo.getBoolean("hasMore")
                        if (needQuery) {
                            point = jo.getString("nextStartPoint")
                        }
                    }

                    Log.i(TAG, "===========finish rpcCall_queryEnergyRanking===========")
                    Log.d(TAG, "friend length: ${friend.length()}")

                    for (i in 0 until friend.length()) {
                        val jo = friend.getJSONObject(i)
                        Log.i(TAG, "======friend=======")
                        Log.d(TAG, "friend: $jo")
                        Log.i(TAG, "======friend=======")
                        val optBoolean =
                            (jo.getBoolean("canCollectEnergy") || jo.getBoolean("canHelpCollect"))
                        val userId = jo.getString("userId")
                        if (optBoolean && userId != "2088022033800183") {
                            Log.d(TAG, "===========canHelpCollect================")
                            Log.d(TAG, "canHelpCollect: $jo")
                            Log.d(TAG, "===========canHelpCollect================")
                            canCollectEnergy(loader, userId)
                        }
                    }

                } catch (t: Throwable) {
                    Log.i(TAG, "queryEnergyRanking err:")
                    Log.e(TAG, Log.getStackTraceString(t))
                    hasMore = false
                }
            }

        }

        private fun canCollectEnergy(loader: ClassLoader, userId: String) {
            try {
                val s: String? = rpcCall_queryNextAction(loader, userId)
                Log.d(TAG, "===========rpcCall_queryNextAction================")
                Log.d(TAG, "canCollectEnergy: $s")
                Log.d(TAG, "===========rpcCall_queryNextAction================")
                var jo = JSONObject(s)
                if (jo.getString("resultCode") == "SUCCESS") {
                    val jaBubbles: JSONArray = jo.getJSONArray("bubbles")
                    jo = jo.getJSONObject("userEnergy")
                    val userName: String = jo.getString("displayName")
                    for (i in 0 until jaBubbles.length()) {
                        jo = jaBubbles.getJSONObject(i)
                        val bubbleId = jo.getLong("id")
                        if ("AVAILABLE" == jo.getString("collectStatus")) {
                            collectEnergy(loader, userId, bubbleId, userName)
                        }
                    }
                } else {
                    Log.d(jo.getString("resultDesc"), s)
                }
            } catch (t: Throwable) {
                Log.d(TAG, "canCollectEnergy err:")
                Log.e(TAG, Log.getStackTraceString(t))
            }
        }

        private fun collectEnergy(
            loader: ClassLoader,
            userId: String,
            bubbleId: Long,
            userName: String
        ) {
            try {
                var s: String? = rpcCall_collectEnergy(loader, userId, bubbleId)
                var jo = JSONObject(s)
                Log.d(TAG, "=============collectEnergy=================")
                Log.d(TAG, "collectEnergy: $s")
                Log.d(TAG, "=============collectEnergy=================")
                if (jo.getString("resultCode") == "SUCCESS") {
                    val jaBubbles: JSONArray = jo.getJSONArray("bubbles")
                    var collected = 0
                    for (i in 0 until jaBubbles.length()) {
                        jo = jaBubbles.getJSONObject(i)
                        collected += jo.getInt("collectedEnergy")
                    }
                    if (collected > 0) {
                        Log.d(
                            TAG,
                            "偷取【$userName】的能量【${collected}克】，UserID：$userId，BubbleId：$bubbleId"
                        )
                        collectedEnergy += collected
                    } else {
                        Log.d(TAG, "偷取【$userName】的能量失败，UserID：$userId，BubbleId：$bubbleId")
                    }
                } else {
                    s = jo.getString("resultDesc")
                    if (s.contains("TA")) {
                        s = s.replace("TA", "【$userName】")
                    }
                    Log.d(s, jo.toString())
                }
            } catch (t: Throwable) {
                Log.i(TAG, "collectEnergy err:")
                Log.e(TAG, Log.getStackTraceString(t))
            }
        }


        private fun rpcCall_queryEnergyRanking(loader: ClassLoader, startPoint: String): String? {
            try {
                val args1 =
                    ("[{\"av\":\"5\",\"ct\":\"android\",\"pageSize\":20,\"startPoint\":\""
                            + startPoint + "\"}]")
                val o: Any? =
                    RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryEnergyRanking", args1)
                return RpcCall.getResponse(o!!)
            } catch (t: Throwable) {
                Log.d(TAG, "rpcCall_queryEnergyRanking err:")
                Log.e(TAG, Log.getStackTraceString(t))
            }
            return null
        }

        private fun rpcCall_queryNextAction(
            loader: ClassLoader,
            userId: String
        ): String? {
            try {
                val args1 = "[{\"userId\":\"$userId\"}]"
                val o =
                    RpcCall.invoke(loader, "alipay.antmember.forest.h5.queryNextAction", args1)

                //   args1 = "[{\"av\":\"5\",\"ct\":\"android\",\"pageSize\":3,\"startIndex\":0,\"userId\":\""
                //    +userId+"\"}]";
                //   RpcCall.invoke(loader, "alipay.antmember.forest.h5.pageQueryDynamics", args1);


                return RpcCall.getResponse(o!!)
            } catch (t: Throwable) {
                Log.d(TAG, "rpcCall_queryNextAction err:")
                Log.e(TAG, Log.getStackTraceString(t))
            }
            return null
        }

        private fun rpcCall_collectEnergy(
            loader: ClassLoader,
            userId: String,
            bubbleId: Long
        ): String? {
            try {
                val args1 =
                    "[{\"bubbleIds\":[$bubbleId],\"userId\":\"$userId\"}]"
                val o =
                    RpcCall.invoke(loader, "alipay.antmember.forest.h5.collectEnergy", args1)
                return RpcCall.getResponse(o!!)
            } catch (t: Throwable) {
                Log.d(TAG, "rpcCall_collectEnergy err:")
                Log.e(TAG, Log.getStackTraceString(t))
            }
            return null
        }

        fun joinJSONArray(arr1: JSONArray, arr2: JSONArray): JSONArray {
            val sb = StringBuilder()
            try {
                val len = arr1.length()
                Log.d(TAG, "len1: $len")
                var obj1: JSONObject
                for (i in 0 until len) {
                    try {
                        obj1 = arr1.getJSONObject(i)
                    } catch (e: Exception) {
                        continue
                    }
                    if (i == len - 1) {
                        sb.append(obj1.toString())
                    } else {
                        sb.append(obj1.toString()).append(",")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "arr1 Exception: ", e)
                return arr2
            }

            try {
                val len = arr2.length()
                if (len > 0) {
                    sb.append(",")
                }
                var obj1: JSONObject
                for (i in 0 until len) {
                    try {
                        obj1 = arr2.getJSONObject(i)
                    } catch (e: Exception) {
                        continue
                    }
                    if (i == len - 1) {
                        sb.append(obj1.toString())
                    } else {
                        sb.append(obj1.toString()).append(",")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "arr2 Exception: ", e)
                return arr1
            }
            if (sb.endsWith(",")) {
                sb.delete(sb.length - 1, sb.length)
            }
            sb.insert(0, "[").append("]")
            return JSONArray(sb.toString())
        }


    }

}