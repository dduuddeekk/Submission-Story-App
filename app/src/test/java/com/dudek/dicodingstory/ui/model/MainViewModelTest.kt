package com.dudek.dicodingstory.ui.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dudek.dicodingstory.DataDummy
import com.dudek.dicodingstory.MainDispatcherRule
import com.dudek.dicodingstory.database.repositories.StoriesRepository
import com.dudek.dicodingstory.database.response.StoriesResponseItem
import com.dudek.dicodingstory.getOrAwaitValue
import com.dudek.dicodingstory.ui.adapter.StoriesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storiesRepository: StoriesRepository

    @Test
    fun `when Get Stories Should Not Null and Return Data`() = runTest {
        val dummyStories = DataDummy.generateDummyStoriesResponse()
        val data: PagingData<StoriesResponseItem> = StoriesPagingSource.snapshot(dummyStories)
        val expectedStories = MutableLiveData<PagingData<StoriesResponseItem>>()
        expectedStories.value = data
        Mockito.`when`(storiesRepository.getStory()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(storiesRepository)
        val actualStories: PagingData<StoriesResponseItem> = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = Dispatchers.Main,
            workerDispatcher = Dispatchers.IO,
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<StoriesResponseItem> = PagingData.from(emptyList())
        val expectedStories = MutableLiveData<PagingData<StoriesResponseItem>>()
        expectedStories.value = data
        Mockito.`when`(storiesRepository.getStory()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(storiesRepository)
        val actualStories: PagingData<StoriesResponseItem> = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = Dispatchers.Main,
            workerDispatcher = Dispatchers.IO,
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
    }
}

class StoriesPagingSource : PagingSource<Int, StoriesResponseItem>() {
    companion object {
        fun snapshot(items: List<StoriesResponseItem>): PagingData<StoriesResponseItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoriesResponseItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoriesResponseItem> {
        return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}