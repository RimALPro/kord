package com.gitlab.kordlib.rest

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.util.toByteArray
import kotlinx.coroutines.Dispatchers
import java.util.*


class Image private constructor(val data: ByteArray, val format: Format) {

    val dataUri: String
        get() {
            val hash = Base64.getEncoder().encodeToString(data)
            return "data:image/${format.extension};base64,$hash"
        }

    companion object {
        fun raw(data: ByteArray, format: Format): Image {
            return Image(data, format)
        }

        suspend fun fromUrl(client: HttpClient, url: String): Image = with(Dispatchers.IO) {
            val call = client.request<HttpResponse>(url) { method = HttpMethod.Get }
            val contentType = call.headers["Content-Type"]
                    ?: error("expected 'Content-Type' header in image request")

            @Suppress("EXPERIMENTAL_API_USAGE")
            val bytes = call.content.toByteArray()

            Image(bytes, Format.fromContentType(contentType))
        }
    }

    sealed class Format(val extension: String) {
        object JPEG : Format("jpeg")
        object PNG : Format("png")
        object WEBP : Format("webp")
        object GIF : Format("gif")

        companion object {
            fun fromContentType(type: String) = when (type) {
                "image/jpeg" -> JPEG
                "image/png" -> PNG
                "image/webp" -> WEBP
                "image/gif" -> GIF
                else -> error(type)
            }
        }
    }
}