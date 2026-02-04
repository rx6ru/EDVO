package org.example.edvo.core.update

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.edvo.getAppVersion

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val html_url: String,
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val browser_download_url: String,
    val name: String,
    val content_type: String
)

class UpdateManager {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun checkForUpdates(): GitHubRelease? {
        return try {
            // Using user's repo: rx6ru/EDVO
            val release: GitHubRelease = client.get("https://api.github.com/repos/rx6ru/EDVO/releases/latest").body()
            
            val currentVersion = getAppVersion().removePrefix("v")
            val latestVersion = release.tag_name.removePrefix("v")

            if (isUpdateAvailable(currentVersion, latestVersion)) {
                release
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun downloadAsset(url: String): ByteArray? {
        return try {
            client.get(url).body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isUpdateAvailable(current: String, latest: String): Boolean {
        // Simple semantic versioning check (e.g. 0.4.0 vs 0.4.1)
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(currentParts.size, latestParts.size)
        
        for (i in 0 until length) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
