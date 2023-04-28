package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import drinkollect.v1.DrinkollectGrpc
import drinkollect.v1.DrinkollectOuterClass.RegisterRequest
import drinkollect.v1.DrinkollectOuterClass.RegisterResponse
import io.grpc.ManagedChannelBuilder
import ru.alzhanov.drinkkollect.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val channel = ManagedChannelBuilder.forAddress("renbou.ru", 18081).usePlaintext().build()
        val stub = DrinkollectGrpc.newBlockingStub(channel)
        val request = RegisterRequest.newBuilder()
            .setUsername("amogus").setPassword("amogus").build()
        val reply: RegisterResponse = stub.register(request)
        Log.i("GRPC Response: %s", reply.toString())

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
                    binding.toolbar.title = "Регистрация"
                    binding.toolbar.visibility = View.VISIBLE
                }
                R.id.ProfileFragment -> {
                    binding.toolbar.title = "@irdkwmnsb"
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