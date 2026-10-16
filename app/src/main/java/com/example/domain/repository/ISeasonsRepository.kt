package com.example.domain.repository

import com.example.data.model.Season

interface ISeasonsRepository {
    suspend fun getSeasons(seriesId: Int): List<Season>
}
