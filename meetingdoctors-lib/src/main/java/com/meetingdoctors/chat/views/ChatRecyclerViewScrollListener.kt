package com.meetingdoctors.chat.views

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Héctor Manrique on 4/12/21.
 */

abstract class ChatRecyclerViewScrollListener(layoutManager: LinearLayoutManager, var pageSize: Int) : RecyclerView.OnScrollListener() {
    var mLayoutManager: LinearLayoutManager = layoutManager
    var olderThreshold: Int? = null

    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
        val lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
        val itemCount = mLayoutManager.itemCount
        Log.i("MessageMonitor", "ChatRecyclerViewScrollListener.onScrolled() itemCount[$itemCount] FirstVisibleItemPosition[$firstVisibleItem] LastVisibleItemPosition[$lastVisibleItem]")
        if (itemCount >= pageSize) {
            // reset older threshold
            if (olderThreshold != null && firstVisibleItem > olderThreshold!!) {
                Log.i("MessageMonitor", "ChatRecyclerViewScrollListener.onScrolled() olderThreshold reset")
                olderThreshold = null
            }
            // set older threshold
            if (olderThreshold == null && firstVisibleItem < pageSize / 4) {
                olderThreshold = pageSize / 4
                Log.i("MessageMonitor", "ChatRecyclerViewScrollListener.onScrolled() olderThreshold set to $olderThreshold")
                onLoadOlderMessages()
            }
        }
    }

    abstract fun onLoadOlderMessages()

}