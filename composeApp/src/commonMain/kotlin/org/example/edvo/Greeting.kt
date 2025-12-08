package org.example.edvo

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hi, ${platform.name}!"
    }
}