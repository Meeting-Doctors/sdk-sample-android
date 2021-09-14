package com.meetingdoctors.chat.views.filepicker.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal abstract class GalleryAdapterPaginationScrollListener(layoutManager: GridLayoutManager): RecyclerView.OnScrollListener() {

    var paginationLayoutManager: GridLayoutManager = layoutManager

    // Index from which pagination should start (0 is 1st page in our case)
    internal val PAGE_START = 0

    // Indicates if footer ProgressBar is shown (i.e. next page is loading)
    internal val isLoading = false

    // If current page is the last page (Pagination will stop after this page load)
    internal val isLastPage = false

    // total no. of pages to load. Initial load is page 0, after which 2 more pages will load.
    internal val TOTAL_PAGES = 3

    internal val VISIBLE_ITEMS_THRESHOLD = 50 

    // indicates the current page which Pagination is fetching.
    private val currentPage = PAGE_START

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        val visibleItemCount = paginationLayoutManager.childCount
        val totalItemCount = paginationLayoutManager.itemCount
        val firstVisibleItemPosition = paginationLayoutManager.findFirstCompletelyVisibleItemPosition()

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems()
            }
        }
    }

    abstract fun loadMoreItems()

    abstract fun getTotalPageCount(): Int

    private fun isLastPage(): Boolean {
        return isLastPage
    }

    private fun isLoading(): Boolean {
        return isLoading
    }
}