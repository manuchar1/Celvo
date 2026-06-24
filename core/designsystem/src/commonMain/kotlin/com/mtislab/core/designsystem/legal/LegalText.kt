package com.mtislab.core.designsystem.legal

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.utils.rememberBrowserOpener

/** A single tappable legal link: a visible [label] that opens [url] in an in-app browser. */
data class LegalLink(val label: String, val url: String)

/**
 * The app-wide entry point for opening a legal document.
 *
 * Wraps [rememberBrowserOpener] so every legal link — whether an inline span or a
 * settings row — opens in the platform's in-app browser (SFSafariViewController on
 * iOS, Chrome Custom Tabs on Android) and keeps the user in context.
 */
@Composable
fun rememberLegalOpener(): (String) -> Unit = rememberBrowserOpener()

/**
 * Matches positional placeholders (`%1$s`, `%2$s`, …) in a localized template so each
 * one can be swapped for an inline, tappable link while preserving locale word order.
 */
private val PLACEHOLDER = Regex("""%(\d+)\${'$'}s""")

/**
 * Renders consent / legal text with inline tappable links.
 *
 * [template] is a localized string containing positional placeholders (`%1$s`, `%2$s`, …).
 * Placeholder N is replaced by [links]`[N-1]`, rendered as a styled link that opens its URL
 * in an in-app browser. Tapping and accessibility are handled natively by the text layout —
 * Compose exposes each [LinkAnnotation] to TalkBack / VoiceOver as a link, and the whole
 * line scales with the user's font-size preference.
 *
 * Centralizing the rendering here keeps every consent string visually consistent and ensures
 * links always route through [rememberBrowserOpener].
 */
@Composable
fun LegalConsentText(
    template: String,
    links: List<LegalLink>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.extended.textTertiary,
    textAlign: TextAlign = TextAlign.Center,
) {
    val openBrowser = rememberBrowserOpener()
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.extended.textLink,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
        )
    )

    val annotated = buildAnnotatedString {
        var cursor = 0
        for (match in PLACEHOLDER.findAll(template)) {
            if (match.range.first > cursor) {
                append(template.substring(cursor, match.range.first))
            }
            val link = match.groupValues[1].toIntOrNull()
                ?.minus(1)
                ?.let(links::getOrNull)
            if (link != null) {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = link.url,
                        styles = linkStyles,
                        linkInteractionListener = { openBrowser(link.url) },
                    )
                ) {
                    append(link.label)
                }
            } else {
                // Unmatched placeholder — render it verbatim rather than dropping text.
                append(match.value)
            }
            cursor = match.range.last + 1
        }
        if (cursor < template.length) {
            append(template.substring(cursor))
        }
    }

    Text(
        text = annotated,
        style = style,
        color = color,
        textAlign = textAlign,
        modifier = modifier,
    )
}
