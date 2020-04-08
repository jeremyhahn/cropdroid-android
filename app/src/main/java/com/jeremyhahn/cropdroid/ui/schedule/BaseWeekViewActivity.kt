package com.jeremyhahn.cropdroid.ui.schedule

import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.MonthLoader.MonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekView.*
import com.alamkanak.weekview.WeekViewEvent
import com.jeremyhahn.cropdroid.R
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseWeekViewActivity : AppCompatActivity(), EventClickListener,
    MonthChangeListener, EventLongPressListener, EmptyViewLongPressListener {

    private var mWeekViewType = TYPE_THREE_DAY_VIEW
    var weekView: WeekView? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekview)

        // Get a reference for the week view in the layout.
        weekView = findViewById<View>(R.id.weekView) as WeekView

        // Show a toast message about the touched event.
        weekView!!.setOnEventClickListener(this)

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        weekView!!.monthChangeListener = this

        // Set long press listener for events.
        weekView!!.eventLongPressListener = this

        // Set long press listener for empty view
        weekView!!.emptyViewLongPressListener = this

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(false)
    }
/*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.weekview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.getItemId()
        setupDateTimeInterpreter(id == R.id.action_week_view)
        when (id) {
            R.id.action_today -> {
                weekView!!.goToToday()
                return true
            }
            R.id.action_day_view -> {
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked())
                    mWeekViewType = TYPE_DAY_VIEW
                    weekView!!.numberOfVisibleDays = 1

                    // Lets change some dimensions to best fit the view.
                    weekView!!.columnGap = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8f,
                        resources.displayMetrics
                    ).toInt()
                    weekView!!.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        12f,
                        resources.displayMetrics
                    ).toInt()
                    weekView!!.eventTextSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        12f,
                        resources.displayMetrics
                    ).toInt()
                }
                return true
            }
            R.id.action_three_day_view -> {
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked())
                    mWeekViewType = TYPE_THREE_DAY_VIEW
                    weekView!!.numberOfVisibleDays = 3

                    // Lets change some dimensions to best fit the view.
                    weekView!!.columnGap = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8f,
                        resources.displayMetrics
                    ).toInt()
                    weekView!!.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        12f,
                        resources.displayMetrics
                    ).toInt()
                    weekView!!.eventTextSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        12f,
                        resources.displayMetrics
                    ).toInt()
                }
                return true
            }
            R.id.action_week_view -> {
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked())
                    mWeekViewType = TYPE_WEEK_VIEW
                    weekView!!.numberOfVisibleDays = 7

                    // Lets change some dimensions to best fit the view.
                    weekView!!.columnGap = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        2f,
                        resources.displayMetrics
                    ).toInt()
                    weekView!!.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        10f,
                        resources.displayMetrics
                    ).toInt()
                    weekView!!.eventTextSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        10f,
                        resources.displayMetrics
                    ).toInt()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
*/

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        weekView!!.dateTimeInterpreter = object : DateTimeInterpreter {
            override fun interpretDate(date: Calendar): String {
                val weekdayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
                var weekday: String = weekdayNameFormat.format(date.getTime())
                val format = SimpleDateFormat(" M/d", Locale.getDefault())

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate) weekday = weekday[0].toString()
                return weekday.toUpperCase() + format.format(date.getTime())
            }

            override fun interpretTime(hour: Int): String {
                return if (hour > 11) (hour - 12).toString() + " PM" else if (hour == 0) "12 AM" else "$hour AM"
            }
        }
    }

    protected fun getEventTitle(time: Calendar): String {
        return java.lang.String.format(
            "Event of %02d:%02d %s/%d",
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
            time.get(Calendar.MONTH) + 1,
            time.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onEventClick(event: WeekViewEvent, eventRect: RectF) {
        Toast.makeText(this, "Clicked " + event.name, Toast.LENGTH_SHORT).show()
    }

    override fun onEventLongPress(event: WeekViewEvent, eventRect: RectF) {
        Toast.makeText(this, "Long pressed event: " + event.name, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: Calendar) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
    }
}