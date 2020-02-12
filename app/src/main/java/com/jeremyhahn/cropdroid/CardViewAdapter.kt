package com.jeremyhahn.cropdroid

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.model.CardViewItem


class CardViewAdapter(val cards: ArrayList<CardViewItem>) : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CardViewAdapter.ViewHolder, position: Int) {
        holder.bindItems(cards[position])
        holder.itemView.setOnClickListener(
            View.OnClickListener
            {
                Log.i("itemView click event!", "Click-$position")
                //context.startActivity(Intent(context, MainActivity::class.java))
            })
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return cards.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) /*, View.OnClickListener */ {

        fun bindItems(item: CardViewItem) {
            val title = itemView.findViewById(R.id.title) as TextView
            val value = itemView.findViewById(R.id.value) as TextView
            title.text = item.title
            value.text = item.value
        }
    }
}