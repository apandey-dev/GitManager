package com.gitmanager.app.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import com.gitmanager.app.data.model.GitHubRepo
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareRepoUrl(context: Context, repo: GitHubRepo) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, repo.fullName)
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out ${repo.fullName} on GitHub:\n${repo.htmlUrl}\n\n${repo.description ?: ""}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Repository"))
    }

    /**
     * Generates a sleek monochrome card image for the repo and opens the share sheet
     */
    fun shareRepoCardImage(context: Context, repo: GitHubRepo) {
        try {
            val bitmap = createRepoCardBitmap(repo)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "repo_${repo.name}_card.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, repo.fullName)
                putExtra(Intent.EXTRA_TEXT, "${repo.fullName} - ${repo.htmlUrl}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Repo Card"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text share
            shareRepoUrl(context, repo)
        }
    }

    fun createRepoCardBitmap(repo: GitHubRepo): Bitmap {
        val width = 1080
        val height = 620
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Outer border (minimalist 2dp)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E4E4E7")
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        val cardRect = RectF(30f, 30f, (width - 30).toFloat(), (height - 30).toFloat())
        canvas.drawRoundRect(cardRect, 16f, 16f, borderPaint)

        // Header / Owner
        val smallTextPaint = Paint().apply {
            color = Color.parseColor("#71717A")
            textSize = 32f
            isAntiAlias = true
        }
        val ownerName = repo.owner?.login ?: "GitHub"
        canvas.drawText("@$ownerName", 70f, 100f, smallTextPaint)

        // Repo Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#09090B")
            textSize = 56f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(repo.name, 70f, 180f, titlePaint)

        // Description
        val descPaint = Paint().apply {
            color = Color.parseColor("#3F3F46")
            textSize = 34f
            isAntiAlias = true
        }
        val desc = repo.description ?: "No description provided."
        val truncatedDesc = if (desc.length > 80) desc.take(77) + "..." else desc
        canvas.drawText(truncatedDesc, 70f, 250f, descPaint)

        // Stats Divider
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#ECECEE")
            strokeWidth = 2f
        }
        canvas.drawLine(70f, 320f, (width - 70).toFloat(), 320f, dividerPaint)

        // Stats (Language, Stars, Forks)
        val statsPaint = Paint().apply {
            color = Color.parseColor("#18181B")
            textSize = 34f
            isAntiAlias = true
        }
        val lang = repo.language ?: "Markdown"
        canvas.drawText("Language: $lang", 70f, 390f, statsPaint)
        canvas.drawText("★ ${repo.stargazersCount} Stars", 70f, 450f, statsPaint)
        canvas.drawText("⑂ ${repo.forksCount} Forks", 70f, 510f, statsPaint)

        // Embed QR Code in the right corner of the card
        val qrBitmap = QrCodeGenerator.generateQrBitmap(repo.htmlUrl, 260)
        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, (width - 340).toFloat(), 310f, null)
        }

        return bitmap
    }
}
