package com.example.data.repository

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

open class BaseRepository {
    protected val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    protected val baseUrl: String get() = BuildConfig.BASE_URL
    protected val apiKey: String get() = BuildConfig.API_KEY

    // Mirror / helper servers defined in BuildConfig
    protected val helperServers: List<String> get() =
        BuildConfig.MIRROR_HOSTS.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    /**
     * Executes GET request with automatic mirror fallback if primary server fails
     */
    protected suspend fun executeRequest(
        primaryUrl: String,
        requestBuilder: (String) -> Request = { url -> Request.Builder().url(url).build() }
    ): String {
        return withContext(Dispatchers.IO) {
            // First, try primary server
            try {
                val primaryRequest = requestBuilder(primaryUrl)
                client.newCall(primaryRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext response.body?.string()
                            ?: throw Exception("Empty response body from primary server")
                    } else {
                        throw Exception("Primary server returned error: ${response.code}")
                    }
                }
            } catch (primaryException: Exception) {
                // If primary server fails, try helper mirror servers in order
                for (helperServer in helperServers) {
                    try {
                        val helperUrl = primaryUrl.replace(Regex("^https?://[^/]+"), helperServer)
                        val helperRequest = requestBuilder(helperUrl)
                        client.newCall(helperRequest).execute().use { response ->
                            if (response.isSuccessful) {
                                return@withContext response.body?.string()
                                    ?: throw Exception("Empty response body from mirror server")
                            }
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }

                throw primaryException
            }
        }
    }
}
