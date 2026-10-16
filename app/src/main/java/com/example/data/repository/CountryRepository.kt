package com.example.data.repository

import com.example.data.model.Country
import com.example.domain.repository.ICountryRepository
import javax.inject.Inject

class CountryRepository @Inject constructor() : BaseRepository(), ICountryRepository {

    override suspend fun getAllCountries(): List<Country> {
        val url = "$baseUrl/api/country/all/$apiKey/"
        val jsonData = executeRequest(url)
        return parseCountries(jsonData)
    }

    private fun parseCountries(jsonData: String): List<Country> {
        val jsonArray = org.json.JSONArray(jsonData)
        val countries = mutableListOf<Country>()

        for (i in 0 until jsonArray.length()) {
            try {
                val countryObj = jsonArray.getJSONObject(i)
                countries.add(
                    Country(
                        id = countryObj.optInt("id", 0),
                        title = countryObj.optString("title", ""),
                        image = countryObj.optString("image", "")
                    )
                )
            } catch (_: Exception) {
                continue
            }
        }

        return countries
    }
}
