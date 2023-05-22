package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import ru.alzhanov.drinkkollect.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val host by lazy { resources.getString(R.string.api_host) }
    private val port by lazy { Integer.parseInt(resources.getString(R.string.api_port)) }
    val service by lazy { DrinkKollectService(host, port) }
    val s3service by lazy { DrinkKollectS3Service(resources.getString(R.string.s3_host),
        resources.getString(R.string.s3_access_key),
        resources.getString(R.string.s3_secret_key)
    )}


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // transitions between fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.LoginFragment -> {
                    binding.toolbar.visibility = View.GONE
                }
                R.id.RegisterFragment -> {
                    binding.toolbar.title = getString(R.string.registration)
                    binding.toolbar.visibility = View.VISIBLE
                }
                R.id.ProfileFragment -> {
                    binding.toolbar.title = service.getUsername()
                    binding.toolbar.visibility = View.VISIBLE
                }
                else -> {
                    binding.toolbar.title = "DrinkKollect"
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}