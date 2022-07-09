package com.arvind.fileencryptedaes

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.arvind.fileencryptedaes.databinding.ActivityMainBinding
import com.arvind.fileencryptedaes.utils.AESEncryption
import com.arvind.fileencryptedaes.utils.Constants.DEBUG_TAG
import com.arvind.fileencryptedaes.utils.URIPathHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView(binding)

    }

    private fun initView(binding: ActivityMainBinding) {
        // Setup image picker launcher
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
                imageUri?.let {
                    saveEncryptedImage(imageUri)
                    displayDecryptedImage(imageUri)
                }
            }
        binding.buttonSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/jpeg")
        }
        binding.buttonReadImage.setOnClickListener {
           
        }
    }

    private fun saveEncryptedImage(imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uriPathHelper = URIPathHelper()
                val filePath = uriPathHelper.getPath(this@MainActivity, imageUri)
                val encryptedImage = AESEncryption().encryptFile("$filePath")
                Log.e(DEBUG_TAG, encryptedImage)

                createDirectoryAndSaveImagePackage(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createDirectoryAndSaveImagePackage(filePath: String?):String {
        val file = filePath?.let { File(this@MainActivity.filesDir, it) }
        CoroutineScope(Dispatchers.IO).launch {

            val outputStream = FileOutputStream(file)

            outputStream.flush()
            outputStream.close()
        }
        return file!!.path

    }

    private fun displayDecryptedImage(imageUri: Uri) {
        val uriPathHelper = URIPathHelper()
        val filePath = uriPathHelper.getPath(this@MainActivity, imageUri)
        val file = File(this.filesDir, filePath)
        val encryptedPath=createDirectoryAndSaveImagePackage(file.path)
        Log.e(DEBUG_TAG, encryptedPath)


        val decryptedImage = AESEncryption().decryptFile("$file")

        Log.e(DEBUG_TAG, decryptedImage)
    }

}