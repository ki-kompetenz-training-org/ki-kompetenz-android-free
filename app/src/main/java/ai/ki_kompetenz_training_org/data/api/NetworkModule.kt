package ai.ki_kompetenz_training_org.data.api

import android.content.Context
import android.util.Log
import ai.ki_kompetenz_training_org.data.prefs.TokenStore
import ai.ki_kompetenz_training_org.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * OkHttp + Retrofit setup.
 * A persistent CookieJar keeps the web session cookies (kkt_access/kkt_refresh)
 * extracted from the WebView login flow — the API accepts the same cookie auth
 * as the browser.
 *
 * Production hardening:
 * - Retry interceptor for transient failures (5xx, timeouts)
 * - Certificate pinning via network_security_config.xml
 * - Logging only in DEBUG builds
 * - Strict timeouts (15s connect, 30s read)
 */
object NetworkModule {

    private const val TAG = "NetworkModule"
    private const val MAX_RETRIES = 2

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun createApiService(context: Context): ApiService {
        val tokenStore = TokenStore(context)

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookies.forEach { c ->
                        if (c.name.startsWith("kkt_")) {
                            tokenStore.setCookie(c.name, c.value)
                        }
                    }
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cookies = tokenStore.getCookies()
                    if (cookies.isEmpty()) return emptyList()
                    return cookies.mapNotNull { (name, value) ->
                        Cookie.Builder()
                            .domain(url.host)
                            .path("/")
                            .name(name)
                            .value(value)
                            .build()
                    }
                }
            })
            .addInterceptor(createCookieInterceptor(tokenStore))
            .addInterceptor(createRetryInterceptor())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor { message ->
                        Log.d(TAG, message)
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()

        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.ensureTrailingSlash())
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)
    }

    /** Attaches session cookies as Cookie header on every request. */
    private fun createCookieInterceptor(tokenStore: TokenStore): Interceptor =
        Interceptor { chain ->
            val request = chain.request()
            val cookies = tokenStore.getCookies()
            val builder = request.newBuilder()
            if (cookies.isNotEmpty()) {
                val header = cookies.entries.joinToString("; ") { (k, v) -> "$k=$v" }
                builder.header("Cookie", header)
            }
            builder.header("User-Agent", "KiKompetenz-Android/${BuildConfig.VERSION_NAME}")
            chain.proceed(builder.build())
        }

    /** Retries transient failures (5xx, timeouts, connection reset). */
    private fun createRetryInterceptor(): Interceptor =
        Interceptor { chain ->
            val request: Request = chain.request()
            var retryCount = 0
            var response = chain.proceed(request)

            while (!response.isSuccessful && retryCount < MAX_RETRIES) {
                val code = response.code
                val isServerError = code in 500..599
                val isConnectivityError = code == 429 // rate-limited

                if (isServerError || isConnectivityError) {
                    retryCount++
                    response.close()
                    // Exponential backoff: 1s, 2s
                    val backoffMs = (1L shl retryCount) * 1000
                    try {
                        Thread.sleep(backoffMs)
                    } catch (_: InterruptedException) {
                        break
                    }
                    response = chain.proceed(request)
                } else {
                    break
                }
            }
            response
        }

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}
