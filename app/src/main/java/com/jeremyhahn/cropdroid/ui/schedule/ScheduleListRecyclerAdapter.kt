package com.jeremyhahn.cropdroid.ui.schedule

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.SCHEDULE_DATE_TIME_LONG_FORMAT
import com.jeremyhahn.cropdroid.Constants.Companion.SCHEDULE_DAY_MAP
import com.jeremyhahn.cropdroid.Constants.Companion.SCHEDULE_FREQUENCY_MAP
import com.jeremyhahn.cropdroid.Constants.Companion.SCHEDULE_TIME_ONLY_FORMAT
import com.jeremyhahn.cropdroid.Constants.Companion.SCHEDULE_TYPE_ONCE
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Schedule
import com.jeremyhahn.cropdroid.utils.ScheduleParser
import kotlinx.android.synthetic.main.microcontroller_schedule_cardview.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScheduleListRecyclerAdapter(val activity: ScheduleListActivity, val cropDroidAPI: CropDroidAPI,
           var recyclerItems: ArrayList<Schedule>, val timerDuration: Int) : RecyclerView.Adapter<ScheduleListRecyclerAdapter.ViewHolder>() {

    class ViewHolder(adapter: ScheduleListRecyclerAdapter, activity: ScheduleListActivity, cropDroidAPI: CropDroidAPI,
                     itemView: View, timerDuration: Int) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        private val TAG = "ConditionListRecyclerAdapter"
        private val adapter: ScheduleListRecyclerAdapter
        private val activity: ScheduleListActivity
        private val cropDroidAPI: CropDroidAPI
        private val timerDuration: Int

        init {
            this.adapter = adapter
            this.activity = activity
            this.cropDroidAPI = cropDroidAPI
            this.timerDuration = timerDuration
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(schedule: Schedule) {

            itemView.setTag(schedule)

            Log.d(TAG, "binding schedule: " + schedule)

            //itemView.tableRowEndDate.id = schedule.id
            //itemView.tableRowTimer.id = schedule.id
            //itemView.tableRowDays.id = schedule.id

            val formatter = SimpleDateFormat(SCHEDULE_DATE_TIME_LONG_FORMAT)
            var frequencyText = ""

            if(schedule.interval > 0) { // CUSTOM frequency
                frequencyText = ScheduleParser.frequencyToText(activity.resources, schedule)
            }
            else {
                when (schedule.frequency) {
                    Constants.SCHEDULE_TYPE_ONCE -> {
                        frequencyText = "One Time Event"
                    }
                    Constants.SCHEDULE_TYPE_DAILY -> {
                        frequencyText = "Daily "
                    }
                    Constants.SCHEDULE_TYPE_WEEKLY -> {
                        frequencyText = "Weekly "
                    }
                    Constants.SCHEDULE_TYPE_MONTHLY -> {
                        frequencyText = "Monthly "
                    }
                    Constants.SCHEDULE_TYPE_YEARLY -> {
                        frequencyText = "Yearly "
                    }
                    else -> {
                        Log.e(TAG, "Unsupported frequency ID: " + schedule.frequency)
                    }
                }
            }

            itemView.frequency.text = frequencyText

            formatter.calendar = schedule.startDate
            itemView.startDateValue.text = formatter.format(schedule.startDate.time)

            if(schedule.endDate != null) {
                formatter.calendar = schedule.endDate
                itemView.endDateValue.text = formatter.format(schedule.endDate!!.time)

                itemView.tableRowEndDate.visibility = View.VISIBLE
                //itemView.endDateTitle.visibility = View.VISIBLE
                //itemView.endDateValue.visibility = View.VISIBLE
            }

            if(schedule.days.size > 0) {
                val days = ArrayList<String>(schedule.days.size)
                for(day in schedule.days) {
                    var _day = day
                    if(day[0].isDigit()) {  // nthDay is used by recurring month view
                        _day = day.substring(1, 3)
                    }
                    days.add(SCHEDULE_DAY_MAP[_day]!!)
                }
                itemView.tableRowDays.visibility = View.VISIBLE
                itemView.daysValue.text = days.joinToString(", ")
            }

            if (timerDuration > 0) {
                itemView.tableRowTimer.visibility = View.VISIBLE
                itemView.timerValue.text = ScheduleParser.timerToText(activity.resources, timerDuration)

                if(schedule.endDate == null) {
                    val endDate = Calendar.getInstance()

                    endDate.time = schedule.startDate.time
                    endDate.add(Calendar.SECOND, timerDuration)

                    var formatter = SimpleDateFormat(SCHEDULE_TIME_ONLY_FORMAT)
                    formatter.calendar = endDate

                    itemView.endDateValue.text = formatter.format(endDate.time)
                    itemView.tableRowEndDate.visibility = View.VISIBLE
                }
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var schedule = itemView.getTag() as Schedule

            Log.d("onCreateContextMenu", "schedule: " + schedule)

            menu!!.add(0, schedule.id, 0, "Edit")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                    val _adapter = this.adapter
                    val scheduleSelectedListener = object:
                        ScheduleSelectionListener {
                        override fun onScheduleSelected(schedule: Schedule) {
                            cropDroidAPI.updateSchedule(schedule, object: Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("ConditionListActivity.onFailure", "onFailure response: " + e!!.message)
                                    return
                                }
                                override fun onResponse(call: Call, response: okhttp3.Response) {
                                    val responseBody = response.body().string()
                                    Log.d("ConditionListActivity.onResponse", responseBody)
                                    activity.runOnUiThread(Runnable() {
                                        _adapter.notifyDataSetChanged()
                                    })
                                }
                            })
                        }
                    }

                    var recurrenceOption = SCHEDULE_FREQUENCY_MAP[schedule.frequency]
                    if(schedule.interval > 0) {
                        recurrenceOption = SublimeRecurrencePicker.RecurrenceOption.CUSTOM
                    }

                    val options = SublimeOptions()
                        .setCanPickDateRange(true)
                        .setPickerToShow(SublimeOptions.Picker.REPEAT_OPTION_PICKER)
                        .setAnimateLayoutChanges(true)
                        .setDateParams(schedule.startDate)
                        .setTimeParams(schedule.startDate.get(Calendar.HOUR_OF_DAY), schedule.startDate.get(Calendar.MINUTE), false)
                        .setRecurrenceParams(recurrenceOption, createRecurrenceRule(schedule))

                    /*
                    if(schedule.startDate != null) {
                        options.setDateRange(schedule.startDate.timeInMillis, Long.MAX_VALUE)
                    }*/

                    var bundle = Bundle()
                    var sublimePickerDialogFragment = SublimePickerDialogFragment(scheduleSelectedListener, schedule, options)
                    sublimePickerDialogFragment.arguments = bundle
                    sublimePickerDialogFragment.isCancelable = true
                    sublimePickerDialogFragment.show(activity.supportFragmentManager,null)

                    true
                })

            menu!!.add(0, schedule.id, 0, "Delete")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    activity.deleteSchedule(schedule)
                    true
                })
        }

        fun createRecurrenceRule(schedule: Schedule) : String {
            if(schedule.frequency == SCHEDULE_TYPE_ONCE) {
                return ""
            }
            if(schedule.interval <= 0) {
                //schedule.frequency = //ScheduleParser.frequencyToText(activity.resources, schedule)
                return "" // interval only specified for CUSTOM frequency
            }
            var rule = StringBuffer()
            rule.append("FREQ").append("=").append(SCHEDULE_FREQUENCY_MAP[schedule.frequency])
            if(schedule.interval > 0) {
                rule.append(";").append("INTERVAL").append("=").append(schedule.interval.toString())
            }
            if(schedule.count > 0) {
                rule.append(";").append("COUNT").append("=").append(schedule.count.toString())
            }
            if(schedule.days.size > 0) {
                rule.append(";").append("BYDAY").append("=").append(schedule.days.joinToString(","))
            }

            // UNTIL=20200505T203221Z

            return rule.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleListRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_schedule_cardview, parent, false)
        return ViewHolder(this, activity, cropDroidAPI, v, timerDuration)
    }

    override fun onBindViewHolder(holder: ScheduleListRecyclerAdapter.ViewHolder, position: Int) {
        if(recyclerItems.size < position) {
            return
        }
        holder.bind(recyclerItems.get(position))
    }

    override fun getItemCount(): Int {
        return recyclerItems.size
    }

    fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    fun setData(data: ArrayList<Schedule>) {
        recyclerItems = data
        notifyDataSetChanged()
    }
}