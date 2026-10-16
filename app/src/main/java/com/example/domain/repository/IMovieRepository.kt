package com.example.domain.repository

import com.example.data.model.FilterOrder
import com.example.data.model.Movie

interface IMovieRepository {
    suspend fun getMovies(page: Int = 0, genreId: Int = 0, order: FilterOrder = FilterOrder.CREATED): List<Movie>
}
