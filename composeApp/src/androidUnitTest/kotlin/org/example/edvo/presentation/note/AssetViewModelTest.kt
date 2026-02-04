package org.example.edvo.presentation.note

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.edvo.domain.model.AssetDetail
import org.example.edvo.domain.model.AssetSummary
import org.example.edvo.domain.repository.AssetRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class AssetViewModelTest {

    private lateinit var viewModel: AssetViewModel
    private lateinit var fakeRepository: FakeAssetRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAssetRepository()
        viewModel = AssetViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test load assets and filter by search query`() = runTest(testDispatcher) {
        // Given
        val assets = listOf(
            AssetSummary("1", "Passport", 0L, 0L),
            AssetSummary("2", "Wifi Password", 0L, 0L),
            AssetSummary("3", "Grocery List", 0L, 0L)
        )
        fakeRepository.emitAssets(assets)

        // When
        viewModel.onSearchQueryChange("Pass")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.listState.value
        assertTrue(state is AssetListState.Success)
        val filtered = (state as AssetListState.Success).assets
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.title == "Passport" })
        assertTrue(filtered.any { it.title == "Wifi Password" })
    }

    @Test
    fun `test sort toggle logic`() = runTest(testDispatcher) {
        // Initial state
        assertEquals(SortOption.DATE_UPDATED, viewModel.sortOption.value)
        assertEquals(SortOrder.DESCENDING, viewModel.sortOrder.value)

        // Toggle same option -> flip order
        viewModel.onSortChange(SortOption.DATE_UPDATED)
        assertEquals(SortOrder.ASCENDING, viewModel.sortOrder.value)

        // Change option -> defaults
        viewModel.onSortChange(SortOption.NAME)
        assertEquals(SortOption.NAME, viewModel.sortOption.value)
        assertEquals(SortOrder.ASCENDING, viewModel.sortOrder.value) // Default for Name
    }

    @Test
    fun `test load detail success`() = runTest(testDispatcher) {
        val detail = AssetDetail("123", "Secret Title", "Secret Content", 0L)
        fakeRepository.prepareAssetDetail(detail)

        viewModel.loadAssetDetail("123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(detail, viewModel.detailState.value)
    }
}

// Fake Repository Implementation
class FakeAssetRepository : AssetRepository {
    private val assetsFlow = MutableStateFlow<List<AssetSummary>>(emptyList())
    private var preparedDetail: AssetDetail? = null

    fun emitAssets(assets: List<AssetSummary>) {
        assetsFlow.value = assets
    }

    fun prepareAssetDetail(detail: AssetDetail) {
        preparedDetail = detail
    }

    override fun getAssets(): Flow<List<AssetSummary>> = assetsFlow

    override suspend fun getAssetById(id: String): AssetDetail? = preparedDetail

    override suspend fun saveAsset(id: String?, title: String, content: String) {
        // No-op for test
    }

    override suspend fun deleteAsset(id: String) {
        // No-op for test
    }
}
