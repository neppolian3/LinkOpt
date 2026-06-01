package com.neppolian3.linkoptima.utils

import android.content.Context
import android.net.Uri
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import timber.log.Timber

object PdfParser {
    fun extractTextFromPdf(context: Context, uri: Uri): Result<String> = try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            Result.success(text)
        } ?: Result.failure(Exception("Unable to open PDF file"))
    } catch (e: Exception) {
        Timber.e(e, "Error parsing PDF")
        Result.failure(e)
    }
}

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> failure(exception: Exception): Result<T> = Error(exception)
    }
    
    inline fun <R> fold(onSuccess: (T) -> R, onFailure: (Exception) -> R): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Error -> onFailure(exception)
        }
    }
}