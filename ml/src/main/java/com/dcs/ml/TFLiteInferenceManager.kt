package com.dcs.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages TensorFlow Lite model lifecycle and runs inference.
 *
 * Model: assets/threat_detector.tflite
 * Input:  Float32 tensor [1, 128] — padded token IDs
 * Output: Float32 tensor [1, 3]   — [safe, scam, abuse] probabilities
 */
@Singleton
class TFLiteInferenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val textPreprocessor: TextPreprocessor
) {
    companion object {
        private const val MODEL_FILE = "threat_detector.tflite"
        private const val NUM_CLASSES = 3
        val CLASS_LABELS = arrayOf("safe", "scam", "abuse")
    }

    private var interpreter: Interpreter? = null

    @Synchronized
    private fun getInterpreter(): Interpreter {
        if (interpreter == null) {
            val model = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            interpreter = Interpreter(model, options)
        }
        return interpreter!!
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    /**
     * Classify text and return probability map.
     * @param text Normalized text to classify
     * @return Map of class labels to probabilities, e.g. {"safe": 0.10, "scam": 0.82, "abuse": 0.08}
     */
    fun classify(text: String): Map<String, Float> {
        return try {
            val tokenIds = textPreprocessor.preprocess(text)

            // Create input buffer [1, MAX_SEQUENCE_LENGTH] as float32
            val inputBuffer = ByteBuffer.allocateDirect(tokenIds.size * 4).apply {
                order(ByteOrder.nativeOrder())
                tokenIds.forEach { putFloat(it.toFloat()) }
                rewind()
            }

            // Create output buffer [1, NUM_CLASSES]
            val outputBuffer = ByteBuffer.allocateDirect(NUM_CLASSES * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            // Run inference
            getInterpreter().run(inputBuffer, outputBuffer)

            // Parse output probabilities
            outputBuffer.rewind()
            val scores = FloatArray(NUM_CLASSES) { outputBuffer.float }

            // Apply softmax if model output is logits
            val softmaxed = softmax(scores)

            CLASS_LABELS.zip(softmaxed.toList()).toMap()
        } catch (e: Exception) {
            // Graceful fallback — return neutral scores so rule engine can still work
            mapOf("safe" to 0.34f, "scam" to 0.33f, "abuse" to 0.33f)
        }
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.max()
        val exps = logits.map { Math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }

    /** Release interpreter resources. */
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
