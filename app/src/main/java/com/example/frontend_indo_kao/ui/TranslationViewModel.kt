package com.example.frontend_indo_kao.ui

import android.app.Application
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_indo_kao.network.RetrofitClient
import com.example.frontend_indo_kao.utils.WavAudioRecorder
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale

data class LanguageOption(val code: String, val name: String)

class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    val availableLanguages = listOf(
        LanguageOption("pagu", "Bahasa Pagu"),
        LanguageOption("modole", "Bahasa Modole"),
        LanguageOption("towiliko", "Bahasa Towiliko")
    )

    var selectedLanguageCode by mutableStateOf("pagu")
    var isIndoToLocal by mutableStateOf(false)

    val currentArahTerjemahan: String
        get() = if (isIndoToLocal) "indonesia_ke_${selectedLanguageCode}" else "${selectedLanguageCode}_ke_indonesia"

    val directionOptions: List<Pair<String, String>>
        get() = availableLanguages.flatMap { lang ->
            listOf(
                "Bahasa Indonesia ke ${lang.name}" to "indonesia_ke_${lang.code}",
                "${lang.name} ke Bahasa Indonesia" to "${lang.code}_ke_indonesia"
            )
        }

    var selectedDirectionLabel by mutableStateOf("Bahasa Pagu ke Bahasa Indonesia")

    fun selectDirectionByLabel(label: String) {
        selectedDirectionLabel = label
        val matched = directionOptions.find { it.first == label }
        if (matched != null) {
            val valStr = matched.second
            if (valStr.startsWith("indonesia_ke_")) {
                isIndoToLocal = true
                selectedLanguageCode = valStr.removePrefix("indonesia_ke_")
            } else if (valStr.endsWith("_ke_indonesia")) {
                isIndoToLocal = false
                selectedLanguageCode = valStr.removeSuffix("_ke_indonesia")
            }
        }
    }

    fun toggleDirection() {
        isIndoToLocal = !isIndoToLocal
        updateDirectionLabel()
    }

    fun setLanguage(code: String) {
        selectedLanguageCode = code
        updateDirectionLabel()
    }

    private fun updateDirectionLabel() {
        val langName = availableLanguages.find { it.code == selectedLanguageCode }?.name ?: "Bahasa Pagu"
        selectedDirectionLabel = if (isIndoToLocal) "Bahasa Indonesia ke $langName" else "$langName ke Bahasa Indonesia"
    }

    var inputText by mutableStateOf("")
    var outputText by mutableStateOf("...")
    var status by mutableStateOf("Status Server: Menunggu...")
    var isTranslating by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var isRecording by mutableStateOf(false)

    private val wavAudioRecorder = WavAudioRecorder(application)
    private var textToSpeech: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        setupTextToSpeech(application)
    }

    private fun setupTextToSpeech(application: Application) {
        textToSpeech = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("id", "ID")
            }
        }
    }

    fun speakResult() {
        if (outputText != "..." && outputText.isNotBlank()) {
            textToSpeech?.speak(outputText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun playAudioUrl(url: String) {
        try {
            val fullUrl = if (url.startsWith("http")) {
                url
            } else {
                "${RetrofitClient.BASE_URL.removeSuffix("/")}/${url.removePrefix("/")}"
            }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(fullUrl)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnErrorListener { _, _, _ ->
                    speakResult()
                    true
                }
            }
        } catch (e: Exception) {
            speakResult()
        }
    }

    fun toggleRecording() {
        if (!isRecording) {
            wavAudioRecorder.startRecording()
            isRecording = true
            status = "Merekam suara... Tekan mikrofon lagi untuk selesai."
        } else {
            val audioFile = wavAudioRecorder.stopRecording()
            isRecording = false
            sendAudioToApi(audioFile)
        }
    }

    private fun sendAudioToApi(audioFile: File) {
        if (!audioFile.exists() || audioFile.length() == 0L) {
            status = "Gagal merekam audio."
            return
        }

        viewModelScope.launch {
            isLoading = true
            status = "Mengirim audio ke server ($selectedDirectionLabel)..."
            outputText = "Memproses pengenalan suara..."

            try {
                val requestFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData("audio_file", audioFile.name, requestFile)

                val arahRequestBody = currentArahTerjemahan.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitClient.instance.translateAudio(audioPart, arahRequestBody)

                val recognizedText = response.teks_sumber_dikenali
                val translationText = response.teks_terjemahan_akhir

                inputText = recognizedText
                outputText = if (translationText.isNotBlank()) translationText else "Tidak ada hasil."
                status = "Terjemahan audio selesai!"

                val audioUrl = response.url_audio_hasil
                if (!audioUrl.isNullOrBlank()) {
                    playAudioUrl(audioUrl)
                } else {
                    speakResult()
                }
            } catch (e: Exception) {
                status = "Error pengiriman audio: ${e.message}"
                outputText = if (e.message?.contains("400") == true) {
                    "Suara tidak berhasil dikenali. Pastikan mikrofon aktif dan berbicara dengan jelas."
                } else {
                    "Gagal menerjemahkan audio dari server."
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun translateText() {
        if (inputText.isBlank()) return

        viewModelScope.launch {
            isTranslating = true
            isLoading = true
            outputText = "Menerjemahkan..."
            status = "Mengirim data ke server ($selectedDirectionLabel)..."

            try {
                val response = RetrofitClient.instance.translateText(
                    teksSumber = inputText,
                    arahTerjemahan = currentArahTerjemahan
                )

                inputText = response.teks_sumber_dikenali
                outputText = response.teks_terjemahan_akhir
                status = "Terjemahan berhasil!"

                val audioUrl = response.url_audio_hasil
                if (!audioUrl.isNullOrBlank()) {
                    playAudioUrl(audioUrl)
                } else {
                    speakResult()
                }
            } catch (e: Exception) {
                outputText = "Error: ${e.message}"
                status = "Pastikan server backend berjalan!"
            } finally {
                isTranslating = false
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
