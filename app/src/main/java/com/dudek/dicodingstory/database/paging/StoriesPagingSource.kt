package com.dudek.dicodingstory.database.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dudek.dicodingstory.data.api.ApiService
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.database.response.StoriesResponseItem
import kotlinx.coroutines.flow.first

class StoriesPagingSource(
    private val apiService: ApiService,
    private val sessionPreference: SessionPreference
): PagingSource<Int, StoriesResponseItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoriesResponseItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val token = sessionPreference.token.first()
            if (token != null) {
                val responseData = apiService.getPageStories("Bearer $token", position, params.loadSize)
                LoadResult.Page(
                    data = responseData,
                    prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                    nextKey = if (responseData.isEmpty()) null else position + 1
                )
            } else {
                LoadResult.Error(Exception("Token is null"))
            }
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoriesResponseItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}