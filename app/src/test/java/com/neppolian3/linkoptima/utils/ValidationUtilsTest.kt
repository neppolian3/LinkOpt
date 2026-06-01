package com.neppolian3.linkoptima.utils

import org.junit.Test
import org.junit.Assert.*

class ValidationUtilsTest {
    
    @Test
    fun testValidLinkedInUrl() {
        val validUrl = "https://linkedin.com/in/john-doe"
        assertTrue(ValidationUtils.isValidLinkedInUrl(validUrl))
    }
    
    @Test
    fun testInvalidLinkedInUrl() {
        val invalidUrl = "https://twitter.com/john"
        assertFalse(ValidationUtils.isValidLinkedInUrl(invalidUrl))
    }
}