package com.example.bookbuddy.ui.navdrawer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
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
import com.example.bookbuddy.utils.UserPreferences
import com.example.bookbuddy.utils.Tools.Companion.setNavigationProfile
import com.example.bookbuddy.utils.currentPicture
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import com.example.bookbuddy.utils.navView
import kotlinx.coroutines.launch
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream

class NavDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavDrawerBinding
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNavDrawer.toolbar)



        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_nav_drawer)

        binding.navLogOut.setOnClickListener {
            userPrefs = UserPreferences(this)
            lifecycleScope.launch {
                userPrefs.saveCredentials("", "")
            }
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            //val drawerLayout = navView.parent as DrawerLayout
            //drawerLayout.closeDrawers()
            //navController.navigate(R.id.nav_settings)
        setNavigationProfile(applicationContext, currentPicture, currentUser.name)
        //setNavigationProfile(applicationContext, BitmapFactory.decodeFile(currentPicture!!.absolutePath), currentUser.name)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_search, R.id.nav_scan, R.id.nav_recommendations, R.id.nav_contacts, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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