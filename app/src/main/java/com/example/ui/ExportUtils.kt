package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ExportUtils {

    fun shareTextFile(context: Context, filename: String, textContent: String) {
        try {
            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use { out ->
                out.write(textContent.toByteArray())
            }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "com.aistudio.linkedinprofileoptimizer.yqwpks.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save or Send Text File"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export text: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun sharePdfFile(context: Context, filename: String, title: String, sections: List<Pair<String, String>>) {
        try {
            val pdfDocument = PdfDocument()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                isAntiAlias = true
            }
            val titlePaint = Paint().apply {
                color = Color.rgb(10, 102, 194) // LinkedIn Blue
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val sectionPaint = Paint().apply {
                color = Color.rgb(0, 65, 130) // LinkedIn Darker Blue
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4 size
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            var y = 50f
            val margin = 45f
            val widthLimit = 595f - margin * 2

            // Draw Header
            canvas.drawText(title, margin, y, titlePaint)
            y += 40f

            fun printParagraph(textStr: String) {
                val words = textStr.split(" ")
                var line = ""
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    val testWidth = textPaint.measureText(testLine)
                    if (testWidth > widthLimit) {
                        if (y > 780f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            y = 50f
                        }
                        canvas.drawText(line, margin, y, textPaint)
                        y += 18f
                        line = word
                    } else {
                        line = testLine
                    }
                }
                if (line.isNotEmpty()) {
                    if (y > 780f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 50f
                    }
                    canvas.drawText(line, margin, y, textPaint)
                    y += 18f
                }
            }

            for ((sectionTitle, content) in sections) {
                if (y > 740f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                }

                y += 12f
                canvas.drawText(sectionTitle, margin, y, sectionPaint)
                y += 20f

                val lines = content.split("\n")
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) {
                        y += 8f
                        continue
                    }
                    printParagraph(trimmed)
                }
                y += 15f
            }

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "com.aistudio.linkedinprofileoptimizer.yqwpks.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save or Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
