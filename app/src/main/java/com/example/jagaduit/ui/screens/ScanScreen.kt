package com.example.jagaduit.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.jagaduit.utils.GeminiHelper
import com.example.jagaduit.utils.saveBitmapToStorage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Camera State
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // AI Processing State
    var isLoading by remember { mutableStateOf(false) }
    val geminiHelper = remember { GeminiHelper() }

    // Permission State
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            processImage(context, it, geminiHelper, navController) { loading -> isLoading = loading }
        }
    }

    // --- UI CONTENT ---
    if (!cameraPermissionState.status.isGranted) {
        // Minta izin
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera permission needed", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    } else {
        // Camera View
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // 1. Preview Kamera
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )

            // 2. Overlay UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Galeri
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(50.dp).background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White)
                    }

                    // Tombol Shot
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                takePhoto(context, imageCapture!!, geminiHelper, navController) { isLoading = it }
                            },
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {}
                    }

                    // Tombol Flash / Lightning
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.enableTorch(isFlashOn) // Nyalakan Senter
                        },
                        modifier = Modifier.size(50.dp).background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }
                }
            }

            // 3. Loading Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing Receipt...", color = Color.White)
                    }
                }
            }
        }
    }
}

// Logic Ambil Foto dari Kamera
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    geminiHelper: GeminiHelper,
    navController: NavController,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // Konversi ImageProxy ke Bitmap
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // Kirim ke AI (Panggil fungsi helper)
                processBitmap(bitmap, geminiHelper, navController, setLoading, context)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                setLoading(false)
                Toast.makeText(context, "Capture Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

// Logic Ambil dari Galeri
private fun processImage(
    context: Context,
    uri: Uri,
    geminiHelper: GeminiHelper,
    navController: NavController,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        processBitmap(bitmap, geminiHelper, navController, setLoading, context)
    } catch (e: Exception) {
        setLoading(false)
        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
    }
}

// Logic Kirim ke Gemini
private fun processBitmap(
    bitmap: Bitmap,
    geminiHelper: GeminiHelper,
    navController: NavController,
    setLoading: (Boolean) -> Unit,
    context: Context
) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
        // 1. Simpan gambar struk ke file
        val savedPath = saveBitmapToStorage(context, bitmap)

        // 2. Analisis AI
        val result = geminiHelper.analyzeReceipt(bitmap)
        setLoading(false)

        if (result != null) {
            // 3. Kirim path gambar via URL (encode URL jika perlu, tapi path internal aman biasanya)
            val route = "input_transaction?" +
                    "amount=${result.amount.toLong()}&" +
                    "category=${result.category ?: ""}&" +
                    "dateStr=${result.date ?: ""}&" +
                    "imagePath=$savedPath"

            navController.navigate(route)
        } else {
            Toast.makeText(context, "Failed to analyze", Toast.LENGTH_LONG).show()
        }
    }
}