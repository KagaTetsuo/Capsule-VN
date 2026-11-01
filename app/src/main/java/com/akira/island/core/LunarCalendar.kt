package com.akira.island.core

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

object LunarCalendar {

    private val CAN = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val CHI = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    private fun getNewMoonDay(k: Int, timeZone: Double = 7.0): Int {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180.0
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2.0 * m * dr)
        c1 -= 0.4068 * sin(mpr * dr) + 0.0161 * sin(2.0 * mpr * dr)
        c1 -= 0.0004 * sin(3.0 * mpr * dr)
        c1 += 0.0104 * sin(2.0 * f * dr) - 0.0051 * sin((m + mpr) * dr)
        c1 -= 0.0074 * sin((m - mpr) * dr) + 0.0004 * sin((2.0 * f + m) * dr)
        c1 -= 0.0004 * sin((2.0 * f - m) * dr) - 0.0006 * sin((2.0 * f + mpr) * dr)
        c1 += 0.0010 * sin((2.0 * f - mpr) * dr) + 0.0005 * sin((m + 2.0 * mpr) * dr)
        val deltat = if (t < -11.0) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.00000061 * t * t3
        } else {
            -0.000078 + 0.000085 * t + 0.0000297 * t2
        }
        val jdNew = jd1 + c1 - deltat
        return floor(jdNew + 0.5 + timeZone / 24.0).toInt()
    }

    private fun getSunLongitude(jdn: Int, timeZone: Double = 7.0): Int {
        val t = (jdn - 2451545.0 + 0.5 - timeZone / 24.0) / 36525.0
        val t2 = t * t
        val dr = PI / 180.0
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        val dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(m * dr) +
                (0.019993 - 0.000101 * t) * sin(2.0 * m * dr) + 0.000290 * sin(3.0 * m * dr)
        var l = (l0 + dl) * dr
        l -= 2.0 * PI * floor(l / (2.0 * PI))
        return floor(l / PI * 6.0).toInt()
    }

    private fun getLunarMonth11(yy: Int, timeZone: Double = 7.0): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm, timeZone)
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    private fun getLeapMonthOffset(a11: Int, timeZone: Double = 7.0): Int {
        val k = floor((a11 - 2415021.076998695) / 29.530588853).toInt()
        var last = 0
        var i = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        while (true) {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
            if (arc == last || i > 14) break
        }
        return i - 1
    }

    fun convertSolarToLunar(dd: Int, mm: Int, yy: Int, timeZone: Double = 7.0): IntArray {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        val lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = floor((monthStart - a11) / 29.0).toInt()
        var lunarLeap = 0
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = 1
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        var finalYear = lunarYear
        if (lunarMonth >= 11 && diff < 4) {
            finalYear -= 1
        }
        return intArrayOf(lunarDay, lunarMonth, finalYear, lunarLeap)
    }

    fun getCanChiYear(year: Int): String {
        val can = CAN[(year + 6) % 10]
        val chi = CHI[(year + 8) % 12]
        return "$can $chi"
    }

    fun getLunarDateString(timestamp: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).apply {
            timeInMillis = timestamp
        }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val lunar = convertSolarToLunar(day, month, year, 7.0)
        val lDay = lunar[0]
        val lMonth = lunar[1]
        val lYear = lunar[2]
        val lLeap = lunar[3] == 1

        val leapStr = if (lLeap) " (nhuận)" else ""
        val canChi = getCanChiYear(lYear)

        return if (Lang.en) {
            "Âm lịch: $lDay/$lMonth$leapStr ($canChi)"
        } else {
            "Âm lịch: $lDay tháng $lMonth$leapStr ($canChi)"
        }
    }
}
