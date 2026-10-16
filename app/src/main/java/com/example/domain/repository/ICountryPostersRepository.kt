package com.example.domain.repository

import com.example.data.model.FilterOrder
import com.example.data.model.Poster

interface ICountryPostersRepository {
    suspend fun getPostersByCountry(
        countryId: Int,
        page: Int = 0,
        order: FilterOrder = FilterOrder.CREATED
    ): List<Poster>
}
