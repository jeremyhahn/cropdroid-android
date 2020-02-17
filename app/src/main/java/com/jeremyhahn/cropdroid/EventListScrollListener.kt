package com.jeremyhahn.cropdroid

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EventListScrollListener(var layoutManager: LinearLayoutManager) :  RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView!!, newState)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                && firstVisibleItemPosition >= 0
                //&& totalItemCount >= getPageCount()
                && totalItemCount < getPageCount()
            ) {
                nextPage()
            }
        }
    }

    protected abstract fun nextPage()
    abstract fun getPageCount(): Int
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
}