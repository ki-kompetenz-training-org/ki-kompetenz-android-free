package ai.ki_kompetenz_training_org.data.api

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NetworkCacheTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun client(cacheDir: File = tmp.newFolder()) = NetworkModule.buildClient(
        tokenStore = mockk(relaxed = true),
        authRepository = mockk(relaxed = true),
        cacheDir = cacheDir,
    )

    @Test
    fun `repeated GET served from cache`() {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setBody("""{"ok":true}""").setHeader("Cache-Control", "public, max-age=600")
        )
        server.start()
        val c = client()
        val url = server.url("/api/content/lessons")
        c.newCall(Request.Builder().url(url).build()).execute().use { it.body!!.string() }
        c.newCall(Request.Builder().url(url).build()).execute().use { it.body!!.string() }
        assertEquals(1, server.requestCount) // 2. Call aus Cache, kein zweiter Hit am Server
        server.shutdown()
    }

    @Test
    fun `POST is never cached`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"ok":true}""").setHeader("Cache-Control", "public, max-age=600"))
        server.enqueue(MockResponse().setBody("""{"ok":true}""").setHeader("Cache-Control", "public, max-age=600"))
        server.start()
        val c = client()
        val url = server.url("/api/srs/review")
        repeat(2) {
            c.newCall(
                Request.Builder().url(url).post(
                    """{"card":"x"}""".toRequestBody()
                ).build()
            ).execute().use { it.body!!.string() }
        }
        assertEquals(2, server.requestCount)
        server.shutdown()
    }

    @Test
    fun `GET requests carry max-stale header`() {
        val request = Request.Builder().url("https://example.org/api/content/lessons").get().build()
        val fakeResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("""{"ok":true}""".toResponseBody())
            .build()
        val chain = mockk<Interceptor.Chain> {
            every { this@mockk.request() } returns request
            every { proceed(any()) } returns fakeResponse
        }
        val captured = slot<Request>()
        every { chain.proceed(capture(captured)) } returns fakeResponse
        NetworkModule.createCacheInterceptor().intercept(chain)
        assertEquals("max-stale=604800", captured.captured.header("Cache-Control"))
    }

    @Test
    fun `POST does not get max-stale header`() {
        val request = Request.Builder().url("https://example.org/api/srs/review")
            .post("""{"card":"x"}""".toRequestBody())
            .build()
        val fakeResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("""{"ok":true}""".toResponseBody())
            .build()
        val chain = mockk<Interceptor.Chain> {
            every { this@mockk.request() } returns request
            every { proceed(any()) } returns fakeResponse
        }
        val captured = slot<Request>()
        every { chain.proceed(capture(captured)) } returns fakeResponse
        NetworkModule.createCacheInterceptor().intercept(chain)
        assertNull(captured.captured.header("Cache-Control"))
    }
}

private fun String.toRequestBody() = okhttp3.RequestBody.create(
    "application/json".toMediaTypeOrNull(), this
)
