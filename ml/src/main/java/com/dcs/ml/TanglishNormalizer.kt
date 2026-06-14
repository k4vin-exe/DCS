package com.dcs.ml

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Normalizes Tanglish (Tamil-English code-mixed) text to English equivalents.
 *
 * Two-phase normalization:
 * 1. Phrase-level: matches multi-word Tanglish patterns first (longest match priority)
 * 2. Word-level: replaces individual Tanglish words with English equivalents
 *
 * This ensures the downstream Rule Engine and ML model receive standardized English text.
 */
@Singleton
class TanglishNormalizer @Inject constructor() {

    /** Phrase-level mappings — checked first to preserve multi-word semantics */
    private val phraseMappings = linkedMapOf(
        // Threat / abuse phrases
        "poi saavu" to "go die",
        "sethu podu" to "kill yourself",
        "saavu da" to "die",
        "saavu di" to "die",
        "nee waste" to "you are worthless",
        "un life waste" to "your life is worthless",
        "kena payale" to "useless person",
        "nee mokka" to "you are useless",

        // Financial scam phrases
        "otp sollu" to "tell otp",
        "otp kudu" to "give otp",
        "panam anuppu" to "send money",
        "panam anupu" to "send money",
        "panam kudu" to "give money",
        "transfer pannu" to "transfer money",
        "verify pannu" to "verify",
        "bank details kudu" to "give bank details",
        "card number kudu" to "give card number",
        "upi pin sollu" to "tell upi pin",
        "account block" to "account blocked",

        // Common compound expressions
        "adichu podanum" to "must beat",
        "kollu unna" to "kill you",
        "un moolam" to "because of you"
    )

    /** Word-level mappings — applied after phrase normalization */
    private val wordMappings = mapOf(
        // Verbs
        "anupu" to "send",
        "anuppu" to "send",
        "kudu" to "give",
        "pannu" to "do",
        "sollu" to "tell",
        "vaangu" to "receive",
        "edhu" to "take",
        "kattu" to "pay",
        "thirudhu" to "steal",
        "thatti" to "steal",
        "adichu" to "hit",
        "kollu" to "kill",

        // Abuse / insults
        "sethuru" to "die",
        "saavu" to "die",
        "loosu" to "idiot",
        "mokka" to "useless",
        "kena" to "useless",
        "mutta" to "fool",
        "muttaal" to "fool",
        "venna" to "idiot",
        "otha" to "slur",
        "thevidiya" to "slur",
        "thevadiya" to "slur",
        "naaye" to "dog",

        // Nouns / context
        "panam" to "money",
        "kaasu" to "money",
        "mosam" to "fraud",
        "yemathuraan" to "cheater",
        "emathuran" to "cheater",
        "tholla" to "trouble",

        // Pronouns / particles
        "nee" to "you",
        "un" to "your",
        "poi" to "go",
        "poda" to "go away",
        "podi" to "go away",
        "waste" to "worthless",

        // Particles (context-dependent, normalize to empty or minimal)
        "da" to "",
        "di" to "",
        "la" to "in",
        "ku" to "to",

        // Pass-through English words commonly mixed in
        "urgent" to "urgent",
        "fast" to "fast",
        "link" to "link",
        "click" to "click",
        "winner" to "winner",
        "prize" to "prize",
        "lottery" to "lottery",
        "deal" to "deal"
    )

    /**
     * Normalize Tanglish text to English equivalents.
     * @param text Input text (may contain Tanglish, English, or mixed)
     * @return Normalized English text
     */
    fun normalize(text: String): String {
        var result = text.lowercase().trim()

        // Phase 1: phrase-level replacement (order matters — longest matches first)
        for ((tanglish, english) in phraseMappings) {
            result = result.replace(tanglish, english)
        }

        // Phase 2: word-level replacement
        val words = result.split("\\s+".toRegex())
        val normalized = words.map { word ->
            wordMappings[word] ?: word
        }.filter { it.isNotBlank() }

        return normalized.joinToString(" ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
