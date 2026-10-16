package com.example.domain.repository

import com.example.data.model.FilterOrder
import com.example.data.model.Series

interface ISeriesRepository {
    suspend fun getSeries(page: Int = 0, genreId: Int = 0, order: FilterOrder = FilterOrder.CREATED): List<Series>
}
