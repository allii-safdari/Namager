package com.example.domain.repository

import com.example.data.model.SearchResult

interface ISearchRepository {
    suspend fun search(query: String): SearchResult
}
