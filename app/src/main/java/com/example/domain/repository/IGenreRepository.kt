package com.example.domain.repository

import com.example.data.model.Genre

interface IGenreRepository {
    suspend fun getGenres(): List<Genre>
}
