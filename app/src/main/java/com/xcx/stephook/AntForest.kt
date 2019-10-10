package com.xcx.stephook

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Create By Ruge at 2019-10-10
 */
class AntForest {

    companion object {
        val TAG = "AntForest"
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

            if (args0 == "alipay.antmember.forest.h5.queryTopEnergyRanking"){
                var jo = JSONObject(resp)
                val jaFriendRanking: JSONArray = jo.getJSONArray("friendRanking")
                for (i in 0 until jaFriendRanking.length()) {
                    jo = jaFriendRanking.getJSONObject(i)
                    val optBoolean = (jo.getBoolean("canCollectEnergy")
                            || jo.getBoolean("canHelpCollect"))
                    val userId: String = jo.getString("userId")
                    if (optBoolean && userId != "2088022033800183") {
                        Log.d(TAG,"userId: $userId")
                        testcollect(loader,userId)
                    }
                }
            }
        }

        private fun testcollect(loader: ClassLoader, userId: String) {

        }

    }

}