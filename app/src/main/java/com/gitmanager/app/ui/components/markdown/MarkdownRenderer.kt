package com.gitmanager.app.ui.components.markdown

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -------------------------------------------------------------
// Markdown Ast / Block Definitions
// -------------------------------------------------------------
sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class BlockQuote(val text: String, val alertType: String? = null) : MarkdownBlock
    data class UnorderedList(val items: List<ListItem>) : MarkdownBlock
    data class OrderedList(val items: List<Pair<String, String>>) : MarkdownBlock
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock
    data object HorizontalRule : MarkdownBlock
    data class ImageBlock(val alt: String, val url: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
}

data class ListItem(
    val text: String,
    val isTask: Boolean = false,
    val isChecked: Boolean = false,
    val level: Int = 0
)

// -------------------------------------------------------------
// Main Composable Markdown Renderer
// -------------------------------------------------------------
@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownToBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> RenderHeader(block)
                is MarkdownBlock.CodeBlock -> RenderCodeBlock(block)
                is MarkdownBlock.BlockQuote -> RenderBlockQuote(block)
                is MarkdownBlock.UnorderedList -> RenderUnorderedList(block)
                is MarkdownBlock.OrderedList -> RenderOrderedList(block)
                is MarkdownBlock.Table -> RenderTable(block)
                is MarkdownBlock.HorizontalRule -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }
                is MarkdownBlock.ImageBlock -> RenderImage(block.alt, block.url)
                is MarkdownBlock.Paragraph -> RenderParagraph(block.text)
            }
        }
    }
}

// -------------------------------------------------------------
// Header Rendering
// -------------------------------------------------------------
@Composable
private fun RenderHeader(header: MarkdownBlock.Header) {
    val primaryColor = MaterialTheme.colorScheme.onSurface
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        when (header.level) {
            1 -> {
                Text(
                    text = header.text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    thickness = 1.dp
                )
            }
            2 -> {
                Text(
                    text = header.text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    thickness = 0.8.dp
                )
            }
            3 -> {
                Text(
                    text = header.text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = primaryColor
                )
            }
            4 -> {
                Text(
                    text = header.text,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = primaryColor
                )
            }
            else -> {
                Text(
                    text = header.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = primaryColor
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Code Block Rendering
// -------------------------------------------------------------
@Composable
private fun RenderCodeBlock(codeBlock: MarkdownBlock.CodeBlock) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column {
            // Header bar with language tag and Copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codeBlock.language.ifEmpty { "code" }.lowercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(codeBlock.code))
                        isCopied = true
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                        contentDescription = "Copy code",
                        modifier = Modifier.size(14.dp),
                        tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Code Content with horizontal scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = codeBlock.code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// -------------------------------------------------------------
// BlockQuote & Alert Rendering
// -------------------------------------------------------------
@Composable
private fun RenderBlockQuote(quote: MarkdownBlock.BlockQuote) {
    val (icon, title, accentColor) = when (quote.alertType?.uppercase()) {
        "NOTE" -> Triple(Icons.Outlined.Info, "NOTE", MaterialTheme.colorScheme.primary)
        "TIP" -> Triple(Icons.Outlined.Lightbulb, "TIP", MaterialTheme.colorScheme.primary)
        "IMPORTANT" -> Triple(Icons.Outlined.PriorityHigh, "IMPORTANT", MaterialTheme.colorScheme.primary)
        "WARNING" -> Triple(Icons.Outlined.Warning, "WARNING", MaterialTheme.colorScheme.error)
        "CAUTION" -> Triple(Icons.Outlined.Shield, "CAUTION", MaterialTheme.colorScheme.error)
        else -> Triple<ImageVector?, String?, Color>(null, null, MaterialTheme.colorScheme.outline)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        // Vertical accent line
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(if (title != null) 36.dp else 24.dp)
                .background(accentColor, shape = RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (title != null && icon != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }

            RenderInlineRichText(
                rawText = quote.text,
                modifier = Modifier.fillMaxWidth(),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// -------------------------------------------------------------
// Unordered List & Task List Rendering
// -------------------------------------------------------------
@Composable
private fun RenderUnorderedList(list: MarkdownBlock.UnorderedList) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        list.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (item.level * 16).dp),
                verticalAlignment = Alignment.Top
            ) {
                if (item.isTask) {
                    Icon(
                        imageVector = if (item.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                        contentDescription = if (item.isChecked) "Completed" else "Todo",
                        modifier = Modifier
                            .padding(top = 2.dp, end = 6.dp)
                            .size(16.dp),
                        tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                RenderInlineRichText(
                    rawText = item.text,
                    modifier = Modifier.weight(1f),
                    textDecoration = if (item.isTask && item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Ordered List Rendering
// -------------------------------------------------------------
@Composable
private fun RenderOrderedList(list: MarkdownBlock.OrderedList) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        list.items.forEach { (numberStr, itemText) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "$numberStr.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .width(24.dp)
                        .padding(top = 1.dp)
                )

                RenderInlineRichText(
                    rawText = itemText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Table Rendering
// -------------------------------------------------------------
@Composable
private fun RenderTable(table: MarkdownBlock.Table) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            // Header Row
            if (table.headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    table.headers.forEach { header ->
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = header.trim(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            }

            // Body Rows
            table.rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    row.forEachIndexed { cellIdx, cell ->
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .padding(horizontal = 4.dp)
                        ) {
                            RenderInlineRichText(
                                rawText = cell.trim(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                if (index < table.rows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Image Rendering
// -------------------------------------------------------------
@Composable
private fun RenderImage(alt: String, url: String) {
    val isBadge = url.contains("shields.io") || url.contains("badge") || url.contains(".svg")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isBadge) Alignment.CenterStart else Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = alt.ifEmpty { "Image" },
            modifier = Modifier
                .heightIn(max = if (isBadge) 32.dp else 240.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

// -------------------------------------------------------------
// Paragraph & Mixed Content Rendering
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderParagraph(text: String) {
    // Check if the paragraph contains images/badges
    val images = extractImages(text)
    if (images.isNotEmpty() && text.trim().startsWith("![")) {
        // Display as flow row of badges / images
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            images.forEach { (alt, url) ->
                val isBadge = url.contains("shields.io") || url.contains("badge") || url.contains(".svg")
                AsyncImage(
                    model = url,
                    contentDescription = alt,
                    modifier = Modifier
                        .heightIn(max = if (isBadge) 28.dp else 200.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    } else {
        RenderInlineRichText(
            rawText = text,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// Inline Markdown & Links Parser Engine
// -------------------------------------------------------------
@Composable
fun RenderInlineRichText(
    rawText: String,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.onSurface

    val cleanText = remember(rawText) { sanitizeHtml(rawText) }

    val (annotatedString, linkUrls) = remember(cleanText, textColor, linkColor, codeBgColor, codeTextColor) {
        buildRichTextAnnotatedString(
            cleanText,
            textColor = textColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            fontStyle = fontStyle,
            textDecoration = textDecoration
        )
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            color = textColor,
            lineHeight = 18.sp,
            fontSize = 13.sp
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open link: ${annotation.item}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    )
}

// -------------------------------------------------------------
// Markdown AST Parsing Helper
// -------------------------------------------------------------
private fun parseMarkdownToBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.replace("\r\n", "\n").lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Skip empty lines
        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // 2. Fenced Code Block
        if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
            val language = trimmed.removePrefix("```").removePrefix("~~~").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```") && !lines[i].trim().startsWith("~~~")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++ // skip ending ```
            blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        // 3. Headers (# H1 to ###### H6)
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') {
                val headerText = trimmed.substring(level + 1).trim()
                blocks.add(MarkdownBlock.Header(level, sanitizeHtml(headerText)))
                i++
                continue
            }
        }

        // 4. Horizontal Rules
        if (trimmed.matches(Regex("^([-*_])\\s*\\1\\s*\\1[\\s\\1]*$"))) {
            blocks.add(MarkdownBlock.HorizontalRule)
            i++
            continue
        }

        // 5. Blockquotes (> text or > [!NOTE])
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            var alertType: String? = null

            while (i < lines.size && lines[i].trim().startsWith(">")) {
                val qLine = lines[i].trim().removePrefix(">").trim()
                if (quoteLines.isEmpty()) {
                    val alertMatch = Regex("^\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\\]", RegexOption.IGNORE_CASE).find(qLine)
                    if (alertMatch != null) {
                        alertType = alertMatch.groupValues[1]
                        val remaining = qLine.removePrefix(alertMatch.value).trim()
                        if (remaining.isNotEmpty()) quoteLines.add(remaining)
                    } else {
                        quoteLines.add(qLine)
                    }
                } else {
                    quoteLines.add(qLine)
                }
                i++
            }
            blocks.add(MarkdownBlock.BlockQuote(quoteLines.joinToString(" "), alertType))
            continue
        }

        // 6. Markdown Table (| header | header |)
        if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size && lines[i + 1].trim().matches(Regex("^\\|[\\s\\-:\\|]+\\|$"))) {
            val headers = parseTableRow(trimmed)
            i += 2 // skip header and separator row
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                rows.add(parseTableRow(lines[i].trim()))
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, rows))
            continue
        }

        // 7. Unordered List (- item, * item, + item, - [ ] item)
        if (trimmed.matches(Regex("^(\\s*)[-*+]\\s+.*"))) {
            val items = mutableListOf<ListItem>()
            while (i < lines.size && lines[i].trim().matches(Regex("^(\\s*)[-*+]\\s+.*"))) {
                val currentLine = lines[i]
                val indent = currentLine.takeWhile { it == ' ' }.length / 2
                var content = currentLine.trim().replaceFirst(Regex("^[-*+]\\s+"), "")
                var isTask = false
                var isChecked = false

                if (content.startsWith("[ ] ")) {
                    isTask = true
                    isChecked = false
                    content = content.removePrefix("[ ] ")
                } else if (content.startsWith("[x] ", ignoreCase = true)) {
                    isTask = true
                    isChecked = true
                    content = content.substring(4)
                }

                items.add(ListItem(text = content, isTask = isTask, isChecked = isChecked, level = indent))
                i++
            }
            blocks.add(MarkdownBlock.UnorderedList(items))
            continue
        }

        // 8. Ordered List (1. item)
        if (trimmed.matches(Regex("^\\d+\\.\\s+.*"))) {
            val items = mutableListOf<Pair<String, String>>()
            while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s+.*"))) {
                val current = lines[i].trim()
                val dotIndex = current.indexOf('.')
                val number = current.substring(0, dotIndex)
                val text = current.substring(dotIndex + 1).trim()
                items.add(Pair(number, text))
                i++
            }
            blocks.add(MarkdownBlock.OrderedList(items))
            continue
        }

        // 9. Standalone Image (![alt](url) or <img src="url"/>)
        val standaloneImageMatch = Regex("^!\\[(.*?)\\]\\((.*?)\\)$").find(trimmed)
        if (standaloneImageMatch != null) {
            val alt = standaloneImageMatch.groupValues[1]
            val url = standaloneImageMatch.groupValues[2]
            blocks.add(MarkdownBlock.ImageBlock(alt, url))
            i++
            continue
        }

        // 10. General Paragraph
        val paragraphLines = mutableListOf<String>()
        while (i < lines.size && lines[i].trim().isNotEmpty() &&
            !lines[i].trim().startsWith("#") &&
            !lines[i].trim().startsWith("```") &&
            !lines[i].trim().startsWith("~~~") &&
            !lines[i].trim().startsWith(">") &&
            !lines[i].trim().matches(Regex("^[-*+]\\s+.*")) &&
            !lines[i].trim().matches(Regex("^\\d+\\.\\s+.*")) &&
            !(lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|"))
        ) {
            paragraphLines.add(lines[i].trim())
            i++
        }
        if (paragraphLines.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ")))
        }
    }

    return blocks
}

// -------------------------------------------------------------
// Helper Parsers
// -------------------------------------------------------------
private fun parseTableRow(row: String): List<String> {
    return row.removePrefix("|").removeSuffix("|").split("|").map { it.trim() }
}

private fun extractImages(text: String): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    val regex = Regex("!\\[(.*?)\\]\\((.*?)\\)")
    regex.findAll(text).forEach { match ->
        result.add(Pair(match.groupValues[1], match.groupValues[2]))
    }
    val htmlImgRegex = Regex("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", RegexOption.IGNORE_CASE)
    htmlImgRegex.findAll(text).forEach { match ->
        result.add(Pair("", match.groupValues[1]))
    }
    return result
}

private fun sanitizeHtml(input: String): String {
    return input
        .replace(Regex("<!--[\\s\\S]*?-->"), "")
        .replace(Regex("<div[^>]*>"), "")
        .replace("</div>", "")
        .replace(Regex("<p[^>]*>"), "")
        .replace("</p>", "")
        .replace("<center>", "")
        .replace("</center>", "")
        .replace("<sub>", "")
        .replace("</sub>", "")
        .replace("<sup>", "")
        .replace("</sup>", "")
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
}

private fun buildRichTextAnnotatedString(
    text: String,
    textColor: Color,
    linkColor: Color,
    codeBgColor: Color,
    codeTextColor: Color,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null
): Pair<AnnotatedString, List<String>> {
    val urls = mutableListOf<String>()

    val annotated = buildAnnotatedString {
        var cursor = 0

        // Regex that finds links, inline code, bold/italic, strikethrough
        val pattern = Regex(
            "(\\[([^\\]]+)\\]\\(([^\\)]+)\\))|(`([^`]+)`)|(\\*\\*([^*]+)\\*\\*)|(__([^_]+)__)|(\\*([^*]+)\\*)|(_([^_]+)_)|(~~([^~]+)~~)"
        )

        pattern.findAll(text).forEach { matchResult ->
            val matchStart = matchResult.range.first
            val matchEnd = matchResult.range.last + 1

            // Append raw text before this match
            if (matchStart > cursor) {
                append(text.substring(cursor, matchStart))
            }

            val matchedStr = matchResult.value
            when {
                // Link [title](url)
                matchedStr.startsWith("[") && matchedStr.contains("](") -> {
                    val title = matchResult.groupValues[2]
                    val url = matchResult.groupValues[3]
                    val startIndex = length
                    pushStringAnnotation(tag = "URL", annotation = url)
                    withStyle(
                        SpanStyle(
                            color = linkColor,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(title)
                    }
                    pop()
                    urls.add(url)
                }

                // Inline code `code`
                matchedStr.startsWith("`") && matchedStr.endsWith("`") -> {
                    val code = matchResult.groupValues[5]
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            color = codeTextColor,
                            background = codeBgColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(" $code ")
                    }
                }

                // Bold **text** or __text__
                matchedStr.startsWith("**") || matchedStr.startsWith("__") -> {
                    val boldText = if (matchedStr.startsWith("**")) matchResult.groupValues[7] else matchResult.groupValues[9]
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                        append(boldText)
                    }
                }

                // Italic *text* or _text_
                matchedStr.startsWith("*") || matchedStr.startsWith("_") -> {
                    val italicText = if (matchedStr.startsWith("*")) matchResult.groupValues[11] else matchResult.groupValues[13]
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                        append(italicText)
                    }
                }

                // Strike ~~text~~
                matchedStr.startsWith("~~") -> {
                    val strikeText = matchResult.groupValues[15]
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = textColor.copy(alpha = 0.7f))) {
                        append(strikeText)
                    }
                }
            }

            cursor = matchEnd
        }

        // Append remainder of text
        if (cursor < text.length) {
            append(text.substring(cursor))
        }

        // Apply default styles
        if (fontStyle != null || textDecoration != null) {
            addStyle(
                SpanStyle(
                    fontStyle = fontStyle ?: FontStyle.Normal,
                    textDecoration = textDecoration ?: TextDecoration.None
                ),
                0,
                length
            )
        }
    }

    return Pair(annotated, urls)
}
