package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.JournalEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private fun getExportsDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun exportEntryAsPdf(context: Context, entry: JournalEntry) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaPaint = TextPaint().apply {
            color = Color.DKGRAY
            textSize = 14f
        }
        val bodyPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 14f
        }
        
        var yPos = 50f
        val margin = 50f
        val width = pageInfo.pageWidth - (margin * 2)

        // Title
        canvas.drawText("Clarté", margin, yPos, titlePaint)
        yPos += 40f

        // Metadata
        val format = SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.FRENCH)
        val dateStr = format.format(Date(entry.createdAt))
        canvas.drawText("Date : $dateStr", margin, yPos, metaPaint)
        yPos += 20f
        canvas.drawText("Mode : ${entry.writingMode}", margin, yPos, metaPaint)
        yPos += 20f
        canvas.drawText("Humeur : ${entry.mood} (${entry.intensity}/10)", margin, yPos, metaPaint)
        yPos += 20f
        if (entry.tags.isNotEmpty()) {
            canvas.drawText("Tags : ${entry.tags.joinToString(", ")}", margin, yPos, metaPaint)
            yPos += 20f
        }
        yPos += 20f // padding

        // Body
        val textLayout = StaticLayout.Builder.obtain(entry.content, 0, entry.content.length, bodyPaint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()
        
        canvas.save()
        canvas.translate(margin, yPos)
        textLayout.draw(canvas)
        canvas.restore()
        
        yPos += textLayout.height + 40f

        // AI Reflection
        if (entry.aiReflection != null) {
            val refTitlePaint = TextPaint().apply { color = Color.BLACK; textSize = 16f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvas.drawText("Réflexion", margin, yPos, refTitlePaint)
            yPos += 25f

            val refLayout = StaticLayout.Builder.obtain(entry.aiReflection, 0, entry.aiReflection.length, bodyPaint, width.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1f, 1f)
                .setIncludePad(false)
                .build()
            
            canvas.save()
            canvas.translate(margin, yPos)
            refLayout.draw(canvas)
            canvas.restore()
        }

        document.finishPage(page)

        val formatFile = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.FRENCH)
        val dateFileStr = formatFile.format(Date(entry.createdAt))
        val fileName = "clarte_entree_$dateFileStr.pdf"
        val file = File(getExportsDir(context), fileName)
        
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        document.close()

        shareFile(context, file, "application/pdf", "Partager l'entrée (PDF)")
    }

    fun exportReviewAsPdf(context: Context, title: String, content: String, dateSlug: String, filenamePrefix: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 A bit naive (only 1 page), but ok for now
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 14f
        }
        
        val margin = 50f
        val width = pageInfo.pageWidth - (margin * 2)
        var yPos = 50f

        canvas.drawText("Clarté - $title", margin, yPos, titlePaint)
        yPos += 40f

        val textLayout = StaticLayout.Builder.obtain(content, 0, content.length, bodyPaint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()
        
        canvas.save()
        canvas.translate(margin, yPos)
        textLayout.draw(canvas)
        canvas.restore()

        document.finishPage(page)

        val fileName = "${filenamePrefix}_$dateSlug.pdf"
        val file = File(getExportsDir(context), fileName)
        
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        document.close()

        shareFile(context, file, "application/pdf", "Partager $title")
    }

    fun exportQuoteImage(context: Context, content: String) {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark background
        canvas.drawColor(Color.parseColor("#121212"))

        val textPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 60f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        
        val signaturePaint = TextPaint().apply {
            color = Color.LTGRAY
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        // Draw centered quote
        val margin = 120
        val textWidth = width - (margin * 2)
        val textLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.2f, 1f)
            .setIncludePad(false)
            .build()

        val yPos = (height - textLayout.height) / 2f
        canvas.save()
        canvas.translate(width / 2f, yPos)
        textLayout.draw(canvas)
        canvas.restore()

        // Signature
        canvas.drawText("Clarté", width / 2f, height - 120f, signaturePaint)

        val formatFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)
        val fileName = "clarte_citation_${formatFile.format(Date())}.png"
        val file = File(getExportsDir(context), fileName)
        
        try {
            file.outputStream().use { 
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        shareFile(context, file, "image/png", "Partager la citation")
    }
}
