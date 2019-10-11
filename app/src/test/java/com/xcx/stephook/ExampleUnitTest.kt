package com.xcx.stephook

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun zzz(){
       val sb = StringBuilder()
        sb.append("zzz,")

        if (sb.endsWith(",")){
            sb.delete(sb.length-1,sb.length)
        }

        println(sb.toString())

    }
}
