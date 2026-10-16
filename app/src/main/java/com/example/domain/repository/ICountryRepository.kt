package com.example.domain.repository

import com.example.data.model.Country

interface ICountryRepository {
    suspend fun getAllCountries(): List<Country>
}
