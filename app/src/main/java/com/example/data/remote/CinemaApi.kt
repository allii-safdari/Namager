package com.example.data.remote

import com.example.BuildConfig
import com.example.core.error.AppException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Thin HTTP gateway for the catalogue API. It owns mirror fallback and converts
 * transport failures into typed [AppException]s so callers do not depend on
 * OkHttp details.
 */
@Singleton
class CinemaApi @Inject constructor(
    private val client: OkHttpClient
) {
    private val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
    private val mirrors = BuildConfig.MIRROR_HOSTS
        .split(',')
        .map { it.trim().trimEnd('/') }
        .filter { it.isNotBlank() }

    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        var lastStatus: Int? = null
        var lastFailure: IOException? = null

        for (url in candidateUrls(path)) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "CinematicView/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body.isNullOrBlank()) throw AppException.Parse()
                        return@withContext body
                    }
                    lastStatus = response.code
                }
            } catch (timeout: SocketTimeoutException) {
                lastFailure = timeout
            } catch (io: IOException) {
                lastFailure = io
            } catch (error: AppException) {
                throw error
            }
        }

        when {
            lastStatus != null -> throw AppException.Server(lastStatus!!)
            lastFailure is SocketTimeoutException -> throw AppException.Timeout(lastFailure)
            else -> throw AppException.Network(lastFailure)
        }
    }

    private fun candidateUrls(path: String): List<String> {
        val normalizedPath = if (path.startsWith('/')) path else "/$path"
        return buildList {
            add("$baseUrl$normalizedPath")
            mirrors.forEach { mirror -> add("$mirror$normalizedPath") }
        }
    }

    companion object {
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
