package com.jeremyhahn.cropdroid.ui.events

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.EventLog
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EventListPaginationRecyclerAdapter(private val context: Context) :  RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private val ITEM = 0
    private val LOADING = 1

    private var eventLogs: MutableList<EventLog> = ArrayList<EventLog>()
    private var isLoadingAdded = false

    fun setEventLogs(eventLogs: MutableList<EventLog>) {
        this.eventLogs = eventLogs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM -> viewHolder = this.getViewHolder(parent, inflater)
            LOADING -> {
                val v2: View = inflater.inflate(R.layout.fragment_progressbar, parent, false)
                viewHolder = this.LoadingVH(v2)
            }
        }
        return viewHolder!!
    }

    @NonNull
    private fun getViewHolder(parent: ViewGroup, inflater: LayoutInflater): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val v1: View = inflater.inflate(R.layout.eventlist_cardview, parent, false)
        viewHolder = this.EventLogVH(v1)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val eventLog = eventLogs!![position]
        when (this.getItemViewType(position)) {
            ITEM -> {
                val eventLogVH = holder as EventLogVH
                eventLogVH.header.setText(createHeader(eventLog))
                eventLogVH.message.setText(eventLog.message)
            }
            LOADING -> {
            }
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return if(eventLogs == null) 0 else eventLogs!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == eventLogs!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */
    fun add(mc: EventLog) {
        eventLogs!!.add(mc)
        //this.notifyItemInserted(eventLogs!!.size - 1)
        //Handler().post(Runnable { this.notifyItemInserted(eventLogs!!.size - 1) })
        Handler().post(Runnable { notifyDataSetChanged() })
    }

    fun addAll(mcList: List<EventLog>) {
        for (mc in mcList) {
            add(mc)
        }
    }

    fun remove(event: EventLog?) {
        val position = eventLogs!!.indexOf(event)
        //if (position > -1) {
            eventLogs!!.removeAt(position)
            //this.notifyItemRemoved(position)
            //Handler().post(Runnable { this.notifyItemRemoved(position) })
            Handler().post(Runnable { notifyDataSetChanged() })
        //}
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
        notifyDataSetChanged()
    }

    val isEmpty: Boolean
        get() = itemCount == 0

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(EventLog("", "", "", ""))
    }

    fun removeLoadingFooter() {
        if(isLoadingAdded) {
            isLoadingAdded = false
            //if(eventLogs!!.size > 0) {
            var position = eventLogs!!.size - 1
            //if (position < 0) position = 0
            val item = this.getItem(position)
            if (item != null) {
                eventLogs!!.removeAt(position)
                //this.notifyItemRemoved(position)
                //Handler().post(Runnable { this.notifyItemRemoved(position) })
                Handler().post(Runnable { notifyDataSetChanged() })
            }
            //}
        }
    }

    fun getItem(position: Int): EventLog {
        return eventLogs!![position]
    }

    fun createHeader(item: EventLog) : String {
        if(!item.timestamp.isEmpty()) {
            var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy h:mm:ss a")
            var zdt = ZonedDateTime.parse(item.timestamp)
            return zdt.format(formatter).toString()
        }
        return ""
    }

    /*
   View Holders
   _________________________________________________________________________________________________
    */
    /**
     * Main list's content ViewHolder
     */
    protected inner class EventLogVH(itemView: View) :  RecyclerView.ViewHolder(itemView) {

        var header: TextView
        var message: TextView

        init {
            header = itemView.findViewById(R.id.eventHeader) as TextView
            message = itemView.findViewById(R.id.eventMessage) as TextView
        }

        fun bindItems(item: EventLog) {
            header.text = createHeader(item)
            message.text = item.message
        }
    }

    protected inner class LoadingVH(itemView: View?) : RecyclerView.ViewHolder(itemView!!)

}