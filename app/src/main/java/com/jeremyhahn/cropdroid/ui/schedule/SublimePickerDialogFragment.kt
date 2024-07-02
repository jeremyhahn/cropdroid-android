package com.jeremyhahn.cropdroid.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.appeaser.sublimepickerlibrary.SublimePicker
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import com.jeremyhahn.cropdroid.Constants.Companion.SCHEDULE_FREQUENCY_ID_MAP
import com.jeremyhahn.cropdroid.model.RecurrenceRule
import com.jeremyhahn.cropdroid.model.Schedule
import java.util.*
import kotlin.collections.ArrayList

class SublimePickerDialogFragment(listener: ScheduleSelectionListener, schedule: Schedule, options: SublimeOptions? = null) : DialogFragment() {

    private val listener: ScheduleSelectionListener
    private val schedule: Schedule
    private var options: SublimeOptions?

    init {
        this.listener = listener
        this.schedule = schedule
        this.options = options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var mListener = object : SublimeListenerAdapter() {

            override fun onCancelled() {
                dismiss()
            }

            override fun onDateTimeRecurrenceSet(sublimeMaterialPicker: SublimePicker?,
                        selectedDate: SelectedDate?, hourOfDay: Int, minute: Int,
                        recurrenceOption: SublimeRecurrencePicker.RecurrenceOption?, recurrenceRule: String?) {

                if(selectedDate != null) {

                    schedule.endDate = null

                    if(selectedDate.secondDate != null) {
                        schedule.endDate = selectedDate.secondDate
                        schedule.endDate!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        schedule.endDate!!.set(Calendar.MINUTE, minute)
                        schedule.endDate!!.set(Calendar.SECOND, 0)
                    }

                    schedule.startDate = selectedDate.firstDate
                    schedule.startDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    schedule.startDate.set(Calendar.MINUTE, minute)
                    schedule.startDate.set(Calendar.SECOND, 0)
                }

                println("hourOfDay: " + hourOfDay.toString())
                println("minute: " + minute)

                recurrenceRule?.let {

                    println("recurrenceRule")
                    println(recurrenceRule)

                    val rule = parseRule(recurrenceRule)
                    schedule.frequency = SCHEDULE_FREQUENCY_ID_MAP[rule.frequency]!!
                    schedule.interval = rule.interval
                    schedule.count = rule.count
                    schedule.days = ArrayList(rule.days)
                }

                recurrenceOption?.let {
                    println("recurrenceOption")
                    println(recurrenceOption.toString())

                    if(!recurrenceOption.equals(SublimeRecurrencePicker.RecurrenceOption.CUSTOM)) {
                        schedule.interval = 0
                        schedule.count = 0
                        schedule.days = ArrayList()

                        val selected = SCHEDULE_FREQUENCY_ID_MAP[recurrenceOption.name]!!
                        if(schedule.frequency != selected) {
                            schedule.frequency = selected
                        }
                    }
                }

                listener.onScheduleSelected(schedule)

                dismiss()
            }
        }

        var sublimePicker = SublimePicker(context)

        if(options == null) {
            options = SublimeOptions()
            options!!.setCanPickDateRange(true)
            options!!.setPickerToShow(SublimeOptions.Picker.REPEAT_OPTION_PICKER)
            options!!.setAnimateLayoutChanges(true)
        }

        sublimePicker.initializePicker(options, mListener)
        return sublimePicker
    }

    fun parseRule(recurrenceRule: String) : RecurrenceRule {
        val rule = RecurrenceRule()
        if(recurrenceRule.isEmpty()) {
            return rule
        }
        val pieces = recurrenceRule.split(";")
        for(piece in pieces) {
            val rulePieces = piece.split("=")
            when(rulePieces[0]) {
                "FREQ" ->
                    rule.frequency = rulePieces[1]
                "INTERVAL" ->
                    rule.interval = rulePieces[1].toInt()
                "COUNT" ->
                    rule.count = rulePieces[1].toInt()
                "BYDAY" ->
                    rule.days = rulePieces[1].split(",")
            }
        }
        return rule
    }
}
