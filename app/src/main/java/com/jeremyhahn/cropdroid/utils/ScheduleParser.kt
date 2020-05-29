package com.jeremyhahn.cropdroid.utils

import android.content.res.Resources
import android.util.Log
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.DATE_FORMAT_RFC3339
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.Schedule
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScheduleParser {

    companion object {

        fun parse(json: String): ArrayList<Schedule> {
            Log.d("[ScheduleParser.parse] json: ", json)
            return parse(JSONArray(json))
        }

        fun parse(jsonSchedules : JSONArray) : ArrayList<Schedule> {

            Log.d("[ScheduleParser.parse] jsonArray: ", jsonSchedules.toString())

            val formatter = SimpleDateFormat(DATE_FORMAT_RFC3339)
            var schedules = ArrayList<Schedule>(jsonSchedules.length())

            for (i in 0..jsonSchedules.length() - 1) {
                val jsonSchedule = jsonSchedules.getJSONObject(i)

                Log.d("ScheduleParser.parse", jsonSchedule.toString())

                val id = jsonSchedule.getLong("id")
                val channelId = jsonSchedule.getInt("channelId")
                val startDate = jsonSchedule.getString("startDate")
                val endDate = jsonSchedule.getString("endDate")
                val frequency = jsonSchedule.getInt("frequency")
                val interval = jsonSchedule.getInt("interval")
                val count = jsonSchedule.getInt("count")

                var days = ArrayList<String>()
                val jsonArray = jsonSchedule.getJSONArray("days")
                for(i in 0..jsonArray.length()-1) {
                    days.add(jsonArray.getString(i))
                }

                var startCalendar = Calendar.getInstance()
                var endCalendar = Calendar.getInstance()

                try {
                    startCalendar.time = formatter.parse(startDate)
                }
                catch(e: ParseException) {
                    Log.e("ScheduleParser.parse", "startDate=" + startDate + ", error=" + e.message)
                    startCalendar = null
                }

                if(endDate != null) {
                    try {
                        endCalendar.time = formatter.parse(endDate)
                    } catch (e: ParseException) {
                        Log.e("ScheduleParser.parse", "endDate=" + endDate + ", error=" + e.message)
                        endCalendar = null
                    }
                }

                schedules.add(
                    Schedule(id, channelId, startCalendar, endCalendar, frequency, interval, count, days)
                )
            }
            return schedules
        }

        fun frequencyIntervalToText(resources: Resources, frequency: Int, interval : Int) : String {
            when(frequency) {
                1 -> {
                    return if(interval > 1) resources.getString(R.string.date_days) else resources.getString(R.string.date_day)
                }
                2 -> {
                    return if(interval > 1) resources.getString(R.string.date_weeks) else resources.getString(R.string.date_week)
                }
                3 -> {
                    return if(interval > 1) resources.getString(R.string.date_months) else resources.getString(R.string.date_month)
                }
                4 -> {
                    return if(interval > 1) resources.getString(R.string.date_years) else resources.getString(R.string.date_year)
                }
            }
            return "{unsupported frequency}"
        }

        fun frequencyToText(resources: Resources, schedule: Schedule) : String {
            var ret = "Every "
            if(schedule.interval > 1) {
                ret  = ret.plus(schedule.interval.toString()).plus(" ")
            }
            ret = ret.plus(frequencyIntervalToText(resources, schedule.frequency, schedule.interval))
            if(schedule.count > 0) {
                ret = ret.plus(" for " + schedule.count.toString() + " " +
                        frequencyIntervalToText(resources, schedule.frequency, schedule.count))
            }
            /*
            if(schedule.days.count() > 0) {
                ret = ret.plus(" (").plus(schedule.days.joinToString(",")).plus(")")
            }*/
            return ret
        }

        fun timerToText(resources: Resources, seconds: Int): String {
            var timer = seconds
            var text = resources.getString(R.string.time_second)
            text = resources.getString(R.string.time_second)
            if (seconds > Constants.SECONDS_IN_YEAR) {
                timer = seconds / Constants.SECONDS_IN_YEAR
                text = resources.getString(R.string.date_years)
            } else if (seconds == Constants.SECONDS_IN_YEAR) {
                timer = seconds / Constants.SECONDS_IN_YEAR
                text = resources.getString(R.string.date_year)
            } else if (seconds > Constants.SECONDS_IN_MONTH) {
                timer = seconds / Constants.SECONDS_IN_MONTH
                text = resources.getString(R.string.date_months)
            } else if (seconds == Constants.SECONDS_IN_MONTH) {
                timer = seconds / Constants.SECONDS_IN_MONTH
                text = resources.getString(R.string.date_month)
            } else if (seconds > Constants.SECONDS_IN_WEEK) {
                timer = seconds / Constants.SECONDS_IN_WEEK
                text = resources.getString(R.string.date_weeks)
            } else if (seconds == Constants.SECONDS_IN_WEEK) {
                timer = seconds / Constants.SECONDS_IN_WEEK
                text = resources.getString(R.string.date_week)
            } else if (seconds > Constants.SECONDS_IN_DAY) {
                timer = seconds / Constants.SECONDS_IN_DAY
                text = resources.getString(R.string.date_days)
            } else if (seconds == Constants.SECONDS_IN_DAY) {
                timer = seconds / Constants.SECONDS_IN_DAY
                text = resources.getString(R.string.date_day)
            } else if (seconds > Constants.SECONDS_IN_HOUR) {
                timer = seconds / Constants.SECONDS_IN_HOUR
                text = resources.getString(R.string.time_hours)
            } else if (seconds == Constants.SECONDS_IN_HOUR) {
                timer = seconds / Constants.SECONDS_IN_HOUR
                text = resources.getString(R.string.time_hour)
            } else if (seconds > Constants.SECONDS_IN_MINUTE) {
                timer = seconds / Constants.SECONDS_IN_MINUTE
                text = resources.getString(R.string.time_minutes)
            } else if (seconds == Constants.SECONDS_IN_MINUTE) {
                timer = seconds / Constants.SECONDS_IN_MINUTE
                text = resources.getString(R.string.time_minute)
            } else if (seconds > 1) {
                text = resources.getString(R.string.time_seconds)
            }
            return StringBuffer().append(timer).append(" ").append(text).toString()
        }
    }
}
