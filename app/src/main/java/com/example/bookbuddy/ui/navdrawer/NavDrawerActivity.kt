package com.example.bookbuddy.ui.navdrawer

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.ActivityNavDrawerBinding
import com.example.bookbuddy.ui.MainActivity
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.Tools.Companion.setNavigationProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream

class NavDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavDrawerBinding
    private lateinit var userPrefs: UserPreferences

    override fun onStart() {
        super.onStart()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNavDrawer.toolbar)

        if (currentProfile.authorId == null && currentProfile.genreId == null){
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Preferences")
                .setMessage("Remember you can change your preferences in your profile")
                .setPositiveButton("Accept") { dialog, _ ->
                    // Acciones a realizar al hacer clic en "Aceptar"
                    dialog.dismiss()
                }
                .show()
            /*
            val builder = AlertDialog.Builder(applicationContext)
            builder.setTitle("Preferences")
            builder.setMessage("Remember you can change your preferences in your profile")
            builder.setPositiveButton("Accept") { dialog, _ ->
                // Acciones a realizar al hacer clic en "Aceptar"
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
            */
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_nav_drawer)

        binding.navLogOut.setOnClickListener {
            currentPicture = null
            userPrefs = UserPreferences(this)
            var intent = Intent(this, MainActivity::class.java)
            lifecycleScope.launch {
                userPrefs.saveCredentials("", "")
                startActivity(intent)
                finish()
            }
        }

        setNavigationProfile(applicationContext, currentPicture, currentUser.name)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_search,
                R.id.nav_scan,
                R.id.nav_recommendations,
                R.id.nav_contacts,
                R.id.nav_profile,
                R.id.nav_admin
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        if (currentUser.isadmin){
            navView.menu.findItem(R.id.nav_admin).isVisible = true
            navView.menu.findItem(R.id.nav_admin).isChecked = false
            navView.menu.findItem(R.id.nav_home).isChecked = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.nav_drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_nav_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}