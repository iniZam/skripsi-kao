package com.example.frontend_indo_kao.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream

class WavAudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    @Volatile
    private var isRecording = false
    private var recordingThread: Thread? = null
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    val outputFile: File
        get() = File(context.cacheDir, "temp_recording.wav")

    fun startRecording() {
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = maxOf(minBufferSize, 2048)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()
            isRecording = true

            recordingThread = Thread {
                writeAudioDataToWavFile(bufferSize)
            }
            recordingThread?.start()
        } catch (e: SecurityException) {
            e.printStackTrace()
            isRecording = false
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    private fun writeAudioDataToWavFile(bufferSize: Int) {
        val pcmFile = File(context.cacheDir, "temp_pcm.raw")
        val data = ByteArray(bufferSize)
        var os: FileOutputStream? = null

        try {
            os = FileOutputStream(pcmFile)
            while (isRecording) {
                val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                if (read > 0) {
                    os.write(data, 0, read)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        convertPcmToWav(pcmFile, outputFile, sampleRate, 1, 16)
        if (pcmFile.exists()) {
            pcmFile.delete()
        }
    }

    fun stopRecording(): File {
        isRecording = false
        try {
            audioRecord?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
        try {
            recordingThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return outputFile
    }

    private fun convertPcmToWav(pcmFile: File, wavFile: File, sampleRate: Int, channels: Int, bitDepth: Int) {
        val pcmData = if (pcmFile.exists()) pcmFile.readBytes() else ByteArray(0)
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * channels * bitDepth / 8).toLong()

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate.toLong() and 0xff).toByte()
        header[25] = ((sampleRate.toLong() shr 8) and 0xff).toByte()
        header[26] = ((sampleRate.toLong() shr 16) and 0xff).toByte()
        header[27] = ((sampleRate.toLong() shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitDepth / 8).toByte()
        header[33] = 0
        header[34] = bitDepth.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val out = FileOutputStream(wavFile)
        out.write(header, 0, 44)
        out.write(pcmData)
        out.close()
    }
}
