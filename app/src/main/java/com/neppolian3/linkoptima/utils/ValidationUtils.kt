package com.neppolian3.linkoptima.utils

object ValidationUtils {
    fun isValidLinkedInUrl(url: String): Boolean {
        return url.contains("linkedin.com") && url.contains("/in/")
    }
    
    fun validateProfileData(profileText: String): Boolean {
        return profileText.length > 50
    }
    
    fun extractProfileInfo(profileText: String): Map<String, String> {
        val info = mutableMapOf<String, String>()
        
        // Extract headline (usually appears early)
        val headlinePattern = "^[^\n]+".toRegex()
        headlinePattern.find(profileText)?.let {
            info["headline"] = it.value
        }
        
        // Extract about section
        val aboutPattern = "(?i)(about|summary):?([^\n]{20,})".toRegex()
        aboutPattern.find(profileText)?.let {
            info["about"] = it.groupValues[2].trim()
        }
        
        return info
    }
}