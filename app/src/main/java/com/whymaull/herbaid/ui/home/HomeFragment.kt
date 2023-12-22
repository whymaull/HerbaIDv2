package com.whymaull.herbaid.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.whymaull.herbaid.databinding.FragmentHomeBinding
import com.whymaull.herbaid.utils.getImageUri
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@Suppress("INTEGER_OVERFLOW", "CAST_NEVER_SUCCEEDS")
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null
    private var outputLabels: List<String>? = null

    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        interpreter = Interpreter(loadModelFile())
        outputLabels = readLabels()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpload.setOnClickListener {
            startGallery()
        }
        binding.btnTakePict.setOnClickListener {
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            currentImageUri = getImageUri(requireContext(),224, 224, 3)
            launcherIntentCamera.launch(currentImageUri)
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Izin kamera diperlukan.", Toast.LENGTH_SHORT).show()
            }
        }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
            processImage()
        }
    }

    private fun processImage() {
        val preprocessedImage = preprocessImage(currentImageUri)
        val result = runInference(preprocessedImage)

        Log.d("Model Inference", "Result: $result")
        Toast.makeText(requireContext(), "Result: $result", Toast.LENGTH_SHORT).show()
    }

    private fun runInference(inputBuffer: ByteBuffer): String {
        val outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE * 4)
        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val probabilities = FloatArray(OUTPUT_SIZE)
        outputBuffer.asFloatBuffer().get(probabilities)

        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        val resultLabel = outputLabels?.get(maxIndex)



        return "Class: $resultLabel, Probability: ${probabilities[maxIndex]}"

    }

    private fun preprocessImage(uri: Uri?): ByteBuffer {
        val inputStream: InputStream? = uri?.let { requireContext().contentResolver.openInputStream(it) }
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, INPUT_WIDTH, INPUT_HEIGHT, true)

        val byteBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        for (pixelValue in pixels) {
            val normalizedValue = (pixelValue and 0xFF) / 255.0f
            byteBuffer.putFloat(normalizedValue)
        }
        byteBuffer.rewind()

        return byteBuffer
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        launcherGallery.launch(intent)
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentImageUri = uri
                showImage()
            }
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }
    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imgUploaded.setImageURI(it)
            processImage()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetManager = requireContext().assets
        val modelFilename = "model.tflite"
        val fileDescriptor = assetManager.openFd(modelFilename)

        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel: FileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    private fun readLabels(): List<String> {
        val labelFilename = "label.txt"
        val inputStream: InputStream = requireContext().assets.open(labelFilename)
        val labels = mutableListOf<String>()

        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { labels.add(it) }
        }

        return labels
    }

    companion object {
        private const val INPUT_WIDTH = 224
        private const val INPUT_HEIGHT = 224
        private const val PIXEL_SIZE = 3
        private const val INPUT_SIZE = INPUT_WIDTH * INPUT_HEIGHT * PIXEL_SIZE
        private const val OUTPUT_SIZE = 10
    }
}