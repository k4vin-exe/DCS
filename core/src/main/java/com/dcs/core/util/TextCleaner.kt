package com.dcs.core.util

/**
 * Text preprocessing utility for the detection pipeline.
 * Cleans, normalizes, and prepares raw text before analysis.
 */
object TextCleaner {

    private val URL_PATTERN = Regex(
        """(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9.-]+\.(com|org|net|in|co|io|xyz|info|biz|tk|ml|ga|cf|gq)[/\S]*)""",
        RegexOption.IGNORE_CASE
    )
    private val SPECIAL_CHARS_PATTERN = Regex("""[^\w\s@.#]""")
    private val MULTIPLE_SPACES_PATTERN = Regex("""\s+""")
    private val EMOJI_PATTERN = Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF\u2600-\u27BF\uFE00-\uFE0F\u200D]""")

    /**
     * Full cleaning pipeline: lowercase → URL removal → emoji removal →
     * special char removal → whitespace normalization.
     */
    fun clean(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .lowercase()
            .let { removeEmojis(it) }
            .let { removeUrls(it) }
            .let { removeSpecialChars(it) }
            .let { normalizeWhitespace(it) }
            .trim()
    }

    /** Check if the raw text contains a URL (before cleaning). */
    fun containsUrl(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        return URL_PATTERN.containsMatchIn(text)
    }

    /** Extract all URLs found in the text. */
    fun extractUrls(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        return URL_PATTERN.findAll(text).map { it.value }.toList()
    }

    private fun removeUrls(text: String): String =
        URL_PATTERN.replace(text, " url ")

    private fun removeEmojis(text: String): String =
        EMOJI_PATTERN.replace(text, " ")

    private fun removeSpecialChars(text: String): String =
        SPECIAL_CHARS_PATTERN.replace(text, " ")

    private fun normalizeWhitespace(text: String): String =
        MULTIPLE_SPACES_PATTERN.replace(text, " ")
}
