package com.mml.dummyapp_kotlin

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.navigateUp
import com.mml.dummyapp_kotlin.databinding.ActivityMainBinding
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var postViewModel: PostViewModel
    private lateinit var navGraph: NavGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout

//        navController = findNavController(R.id.nav_host_fragment_content_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?

        if (navHostFragment != null) {
            navController = navHostFragment.navController
        }
        navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_accessory,
                R.id.nav_arcade, R.id.nav_fashion,
                R.id.nav_food, R.id.nav_heath,
                R.id.nav_lifestyle, R.id.nav_sports, R.id.nav_favorites, R.id.about
            ), drawerLayout
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding.navView, navController)



        postViewModel.currentDestination.observe(this) {currentDestination->

//            Log.w(
//                TAG, "currentDestination: $currentDestination"
//            )
//            Toast.makeText(this, "currentDestination$currentDestination", Toast.LENGTH_SHORT)
//                .show()

            if(currentDestination != R.id.about) {
                navGraph.setStartDestination(currentDestination)
                navController.graph = navGraph
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
//            Log.d(TAG, "addOnDestinationChangedListener: " + destination.id)
//            Toast.makeText(
//                this, "addOnDestinationChangedListener" + destination.id, Toast.LENGTH_SHORT
//            ).show()
            postViewModel.saveCurrentDestination(currentDestination = destination.id)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}