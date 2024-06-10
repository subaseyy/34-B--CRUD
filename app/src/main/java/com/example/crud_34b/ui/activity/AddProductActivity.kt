package com.example.crud_34b.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crud_34b.R
import com.example.crud_34b.databinding.ActivityAddProductBinding
import com.example.crud_34b.model.ProductModel
import com.example.crud_34b.utils.ImageUtils
import com.example.crud_34b.viewmodel.ProductViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class AddProductActivity : AppCompatActivity() {


    lateinit var addProductBinding: ActivityAddProductBinding

    lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    var imageUri : Uri? = null

    lateinit var imageUtils: ImageUtils
    lateinit var productViewModel: ProductViewModel

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        addProductBinding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(addProductBinding.root)

        registerActivityForResult()

        addProductBinding.imageBrowse.setOnClickListener{
            var permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                android.Manifest.permission.READ_MEDIA_IMAGES
            }else{
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (ContextCompat.checkSelfPermission(this,permissions) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(permissions),1)
            }else{
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                activityResultLauncher.launch(intent)
            }
        }
        addProductBinding.btnPost.setOnClickListener {
            if(imageUri != null){
                uploadImage()
            }else{
                Toast.makeText(applicationContext,"Please upload image first",
                    Toast.LENGTH_LONG
                    ).show()
            }

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun uploadImage(){
        val imageName = UUID.randomUUID().toString()
        var imageReference = storageRef.child("products").child(imageName)
        imageUri?.let { url->
           imageReference.putFile(url).addOnSuccessListener {
              imageReference.downloadUrl.addOnSuccessListener {downloadUrl->
                  var imagesUrl = downloadUrl.toString()
                  addProduct(imagesUrl,imageName)
              }
           }.addOnFailureListener {
               Toast.makeText(applicationContext,it.localizedMessage,
                   Toast.LENGTH_LONG).show()
           }
        }


    }
    fun addProduct(url: String,imageName: String){
        var name : String = addProductBinding.editTextProductName.text.toString()
        var price : Int = addProductBinding.editTextProductPrice.text.toString().toInt()
        var desc : String = addProductBinding.editTextProductDesc.text.toString()

        var id = ref.push().key.toString()

        var data = ProductModel(id,name,price,desc,url,imageName)

        ref.child(id).setValue(data).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(applicationContext,"Data added",
                    Toast.LENGTH_LONG).show()
                finish()
            }else{
                Toast.makeText(applicationContext,it.exception?.message,
                    Toast.LENGTH_LONG).show()
            }
        }
    }

}