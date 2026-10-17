package com.example.frontend_indo_kao

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_indo_kao.ui.TranslationViewModel
import com.example.frontend_indo_kao.ui.theme.FrontendindokaoTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var fontSizeScale by remember { mutableStateOf(1f) }
            var currentScreen by remember { mutableStateOf("welcome") } // "welcome" or "translation"

            FrontendindokaoTheme(darkTheme = isDarkMode, dynamicColor = false) {
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = currentDensity.density,
                        fontScale = currentDensity.fontScale * fontSizeScale
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (isDarkMode) Color(0xFF121212) else Color(0xFFF4F7F6)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            if (screen == "welcome") {
                                WelcomeScreen(
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { isDarkMode = it },
                                    fontSizeScale = fontSizeScale,
                                    onFontSizeScaleChange = { fontSizeScale = it },
                                    onStartClick = { currentScreen = "translation" }
                                )
                            } else {
                                TranslationScreen(
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { isDarkMode = it },
                                    fontSizeScale = fontSizeScale,
                                    onFontSizeScaleChange = { fontSizeScale = it },
                                    onBackToWelcome = { currentScreen = "welcome" },
                                    onRequestPermission = { checkAndRequestPermission() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun SettingsDialog(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    fontSizeScale: Float,
    onFontSizeScaleChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Pengaturan", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDarkMode) "Mode Gelap 🌙" else "Mode Terang ☀️",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Ukuran Teks", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "A", fontSize = 12.sp)
                    Slider(
                        value = fontSizeScale,
                        onValueChange = onFontSizeScaleChange,
                        valueRange = 0.8f..1.5f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(text = "A", fontSize = 20.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun WelcomeScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    fontSizeScale: Float,
    onFontSizeScaleChange: (Float) -> Unit,
    onStartClick: () -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Kembalikan ke warna background yang terang/putih
    val bgColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val textColor = if (isDarkMode) Color(0xFFEEEEEE) else Color(0xFF333333)
    val buttonColor = if (isDarkMode) Color(0xFF2ECC71) else Color(0xFF2ECC71)

    if (showSettingsDialog) {
        SettingsDialog(
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode,
            fontSizeScale = fontSizeScale,
            onFontSizeScaleChange = onFontSizeScaleChange,
            onDismiss = { showSettingsDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .size(48.dp)
        ) {
            Text(text = "⚙️", fontSize = 28.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selamat Datang!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aplikasi Penerjemah\nBahasa Kao-Indonesia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }

            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
                contentScale = ContentScale.Fit
            )

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Mulai",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorRow(
    isIndoToLocal: Boolean,
    selectedLocalLanguage: String,
    availableLanguages: List<com.example.frontend_indo_kao.ui.LanguageOption>,
    onToggleDirection: () -> Unit,
    onLocalLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Sumber Bahasa
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isIndoToLocal) {
                OutlinedTextField(
                    value = "Bahasa Indonesia",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLocalLanguage,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                        singleLine = true,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.name) },
                                onClick = {
                                    onLocalLanguageSelected(lang.code)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tombol Tukar (Swap)
        IconButton(
            onClick = onToggleDirection,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(40.dp)
        ) {
            Text(text = "⇄", fontSize = 24.sp, color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
        }

        // Target Bahasa
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (!isIndoToLocal) {
                OutlinedTextField(
                    value = "Bahasa Indonesia",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLocalLanguage,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                        singleLine = true,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.name) },
                                onClick = {
                                    onLocalLanguageSelected(lang.code)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranslationScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    fontSizeScale: Float,
    onFontSizeScaleChange: (Float) -> Unit,
    onBackToWelcome: () -> Unit,
    viewModel: TranslationViewModel = viewModel(),
    onRequestPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF4F7F6)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color(0xFFEEEEEE) else Color(0xFF333333)
    val subTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF666666)
    val outputBoxBg = if (isDarkMode) Color(0xFF2A3A4A) else Color(0xFFEAF2F8)
    val outputBoxText = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF2C3E50)

    if (showSettingsDialog) {
        SettingsDialog(
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode,
            fontSizeScale = fontSizeScale,
            onFontSizeScaleChange = onFontSizeScaleChange,
            onDismiss = { showSettingsDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToWelcome) {
                    Text(text = "⬅️", fontSize = 22.sp)
                }
                IconButton(onClick = { showSettingsDialog = true }) {
                    Text(text = "⚙️", fontSize = 24.sp)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.welcome_illustration),
                        contentDescription = "Dekorasi Karakter",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Penerjemah Suara Kao",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val currentLocalName = viewModel.availableLanguages.find { it.code == viewModel.selectedLanguageCode }?.name ?: "Bahasa Daerah"
                    
                    LanguageSelectorRow(
                        isIndoToLocal = viewModel.isIndoToLocal,
                        selectedLocalLanguage = currentLocalName,
                        availableLanguages = viewModel.availableLanguages,
                        onToggleDirection = { viewModel.toggleDirection() },
                        onLocalLanguageSelected = { code -> viewModel.setLanguage(code) }
                    )

                    // Tombol Mikrofon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val micScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = if (viewModel.isRecording) 1.2f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "mic_pulse"
                        )

                        FilledIconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.toggleRecording()
                                } else {
                                    onRequestPermission()
                                }
                            },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            enabled = !viewModel.isLoading,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Image(
                                painter = painterResource(id = if (viewModel.isRecording) R.drawable.mic_active else R.drawable.mic_inactive),
                                contentDescription = "Mikrofon",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(micScale)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (viewModel.isRecording) "Merekam Suara... (Tekan untuk Selesai)" else "Tekan Mikrofon untuk Merekam",
                            fontSize = 12.sp,
                            color = if (viewModel.isRecording) Color(0xFFC0392B) else subTextColor,
                            fontWeight = if (viewModel.isRecording) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Indikator Loading Berputar saat Mengirim & Memproses Audio
                    if (viewModel.isLoading) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = outputBoxBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color(0xFF2ECC71),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Memproses pengenalan suara...",
                                    fontSize = 14.sp,
                                    color = outputBoxText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Teks Sumber Dikenali (Input Text Box)
                    OutlinedTextField(
                        value = viewModel.inputText,
                        onValueChange = { viewModel.inputText = it },
                        label = { Text("Teks Sumber Dikenali") },
                        placeholder = { Text("Hasil rekam suara akan tampil di sini...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Tombol Terjemahkan Teks Manual
                    Button(
                        onClick = { viewModel.translateText() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(12.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        Text(text = "Terjemahkan Teks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // Hasil Terjemahan
                    AnimatedVisibility(
                        visible = viewModel.outputText.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hasil Terjemahan Akhir:",
                                    fontSize = 14.sp,
                                    color = subTextColor,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.speakResult() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Text(text = "🔊", fontSize = 18.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .background(outputBoxBg, shape = RoundedCornerShape(8.dp))
                                    .padding(15.dp)
                                ) {
                                Text(
                                    text = viewModel.outputText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = outputBoxText
                                )
                            }
                        }
                    }

                    Text(
                        text = viewModel.status,
                        fontSize = 12.sp,
                        color = if (viewModel.status.contains("Error") || viewModel.status.contains("Gagal")) Color.Red else subTextColor,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    }
}
