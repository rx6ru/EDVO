package org.example.edvo.core.generator

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for password, passphrase, and username generation.
 */
class GeneratorTest {

    // ==================== PASSWORD GENERATION ====================

    @Test
    fun test_generatePassword_customLength() {
        listOf(8, 12, 20, 32).forEach { length ->
            val password = SecurityGenerator.generatePassword(
                length = length,
                useUpper = true,
                useLower = true,
                useDigits = true,
                useSymbols = true,
                avoidAmbiguous = false
            )
            assertEquals("Password should be $length characters", length, password.length)
        }
    }

    @Test
    fun test_generatePassword_withUppercaseOnly() {
        val password = SecurityGenerator.generatePassword(
            length = 50, 
            useUpper = true,
            useLower = false,
            useDigits = false,
            useSymbols = false,
            avoidAmbiguous = false
        )
        assertTrue("Password should contain uppercase", password.any { it.isUpperCase() })
        assertTrue("Password should only contain uppercase", password.all { it.isUpperCase() })
    }

    @Test
    fun test_generatePassword_withLowercaseOnly() {
        val password = SecurityGenerator.generatePassword(
            length = 50, 
            useUpper = false,
            useLower = true,
            useDigits = false,
            useSymbols = false,
            avoidAmbiguous = false
        )
        assertTrue("Password should contain lowercase", password.any { it.isLowerCase() })
        assertTrue("Password should only contain lowercase", password.all { it.isLowerCase() })
    }

    @Test
    fun test_generatePassword_withDigitsOnly() {
        val password = SecurityGenerator.generatePassword(
            length = 50, 
            useUpper = false,
            useLower = false,
            useDigits = true,
            useSymbols = false,
            avoidAmbiguous = false
        )
        assertTrue("Password should contain digits", password.any { it.isDigit() })
        assertTrue("Password should only contain digits", password.all { it.isDigit() })
    }

    @Test
    fun test_generatePassword_avoidAmbiguous() {
        val ambiguousChars = "lI1O0"
        val passwords = (1..20).map {
            SecurityGenerator.generatePassword(
                length = 50,
                useUpper = true,
                useLower = true,
                useDigits = true,
                useSymbols = false,
                avoidAmbiguous = true
            )
        }
        
        val containsAmbiguous = passwords.any { pwd -> pwd.any { it in ambiguousChars } }
        assertFalse("Should not contain ambiguous characters", containsAmbiguous)
    }

    @Test
    fun test_generatePassword_uniqueEachCall() {
        val passwords = (1..50).map { 
            SecurityGenerator.generatePassword(
                length = 16,
                useUpper = true,
                useLower = true,
                useDigits = true,
                useSymbols = true,
                avoidAmbiguous = false
            )
        }.toSet()
        
        assertEquals("All generated passwords should be unique", 50, passwords.size)
    }

    @Test
    fun test_generatePassword_noCharTypes_returnsEmpty() {
        val password = SecurityGenerator.generatePassword(
            length = 16,
            useUpper = false,
            useLower = false,
            useDigits = false,
            useSymbols = false,
            avoidAmbiguous = false
        )
        assertEquals("No char types should return empty", "", password)
    }

    // ==================== PASSPHRASE GENERATION ====================

    @Test
    fun test_generatePassphrase_wordCount() {
        listOf(3, 4, 5, 6).forEach { count ->
            val passphrase = SecurityGenerator.generatePassphrase(
                wordCount = count,
                separator = "-",
                capitalize = false,
                includeNumber = false
            )
            val words = passphrase.split("-")
            assertEquals("Should have $count words", count, words.size)
        }
    }

    @Test
    fun test_generatePassphrase_customSeparator() {
        val passphrase = SecurityGenerator.generatePassphrase(
            wordCount = 4,
            separator = "_",
            capitalize = false,
            includeNumber = false
        )
        assertTrue("Should use underscore separator", passphrase.contains("_"))
        assertFalse("Should not use dash separator", passphrase.contains("-"))
    }

    @Test
    fun test_generatePassphrase_capitalize() {
        val passphrase = SecurityGenerator.generatePassphrase(
            wordCount = 4,
            separator = "-",
            capitalize = true,
            includeNumber = false
        )
        val words = passphrase.split("-")
        assertTrue("First word should be capitalized", words[0][0].isUpperCase())
    }

    @Test
    fun test_generatePassphrase_includeNumber() {
        val passphrases = (1..20).map {
            SecurityGenerator.generatePassphrase(
                wordCount = 4,
                separator = "-",
                capitalize = false,
                includeNumber = true
            )
        }
        
        val hasNumber = passphrases.any { pp -> pp.any { it.isDigit() } }
        assertTrue("Should contain a number", hasNumber)
    }

    // ==================== USERNAME GENERATION ====================

    @Test
    fun test_generateUsername_randomWord() {
        val username = SecurityGenerator.generateUsername(
            style = SecurityGenerator.UsernameStyle.RANDOM_WORD,
            capitalize = false,
            includeNumber = false
        )
        assertTrue("Username should not be empty", username.isNotEmpty())
    }

    @Test
    fun test_generateUsername_adjectiveNoun() {
        val username = SecurityGenerator.generateUsername(
            style = SecurityGenerator.UsernameStyle.ADJECTIVE_NOUN,
            capitalize = false,
            includeNumber = false
        )
        assertTrue("Username should not be empty", username.isNotEmpty())
    }

    @Test
    fun test_generateUsername_capitalize() {
        val username = SecurityGenerator.generateUsername(
            style = SecurityGenerator.UsernameStyle.RANDOM_WORD,
            capitalize = true,
            includeNumber = false
        )
        assertTrue("Username should start with uppercase", username[0].isUpperCase())
    }

    @Test
    fun test_generateUsername_includeNumber() {
        val usernames = (1..10).map {
            SecurityGenerator.generateUsername(
                style = SecurityGenerator.UsernameStyle.ADJECTIVE_NOUN,
                capitalize = false,
                includeNumber = true
            )
        }
        
        val allHaveNumber = usernames.all { un -> un.any { it.isDigit() } }
        assertTrue("All usernames should contain a number", allHaveNumber)
    }

    // ==================== UNIQUENESS ====================

    @Test
    fun test_generatePassphrase_uniqueEachCall() {
        val passphrases = (1..30).map { 
            SecurityGenerator.generatePassphrase(
                wordCount = 4,
                separator = "-",
                capitalize = false,
                includeNumber = false
            )
        }.toSet()
        
        assertTrue("Most passphrases should be unique", passphrases.size > 25)
    }
}
