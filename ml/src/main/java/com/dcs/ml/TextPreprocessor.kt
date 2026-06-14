package com.dcs.ml

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converts raw text into a fixed-length numeric tensor suitable for TFLite inference.
 * Uses a vocabulary mapping loaded from assets/vocab.json.
 */
@Singleton
class TextPreprocessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_SEQUENCE_LENGTH = 128
        private const val VOCAB_FILE = "vocab.json"
        private const val PAD_TOKEN = 0
        private const val UNK_TOKEN = 1
    }

    private val vocabulary: Map<String, Int> by lazy { loadVocabulary() }

    private fun loadVocabulary(): Map<String, Int> {
        return try {
            val json = context.assets.open(VOCAB_FILE).bufferedReader().readText()
            val type = object : TypeToken<Map<String, Int>>() {}.type
            Gson().fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Convert text to a padded/truncated IntArray of token IDs.
     * Output length is always [MAX_SEQUENCE_LENGTH].
     */
    fun preprocess(text: String): IntArray {
        val tokens = tokenize(text)
        val tokenIds = tokens.map { vocabulary[it] ?: UNK_TOKEN }

        return when {
            tokenIds.size >= MAX_SEQUENCE_LENGTH ->
                tokenIds.take(MAX_SEQUENCE_LENGTH).toIntArray()
            else -> {
                val padded = IntArray(MAX_SEQUENCE_LENGTH) { PAD_TOKEN }
                tokenIds.forEachIndexed { index, id -> padded[index] = id }
                padded
            }
        }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
}
