package org.example.edvo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform