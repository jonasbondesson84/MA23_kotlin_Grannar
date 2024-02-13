package com.example.grannar


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.nav_host_fragment)

        //Gets logged in users from database and save it to CurrentUser
        val auth = FirebaseAuth.getInstance()
        if( auth.currentUser != null) {
            CurrentUser.loadUserInfo(auth.uid.toString())
        }

        bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.profileFragment && !isLoggedIn()) {

                navController.popBackStack()
                val dialogFragment = SignInDialogFragment()
                dialogFragment.show(supportFragmentManager, "SignInDialogFragment")
            }
        }

    }

    private fun isLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            return true
        }
        else return false
    }
}