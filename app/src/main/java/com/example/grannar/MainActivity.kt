package com.example.grannar


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject


interface SignInResultListener {
    fun onSignInSuccess()
    fun onSignInFailure()
}
class MainActivity : AppCompatActivity(), SignInResultListener {

    private var pendingDestination: NavDestination? = null
    private var pendingBundle: Bundle? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.nav_host_fragment)


        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, bundle ->

            if (destination.id == R.id.profileFragment && !isLoggedIn()) {

                pendingDestination = destination
                pendingBundle = bundle

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

    private fun clearPendingDestination(){
        pendingDestination = null
        pendingBundle = null
    }


    override fun onSignInSuccess() {
        val navController = findNavController(R.id.nav_host_fragment)
        pendingDestination?.id?.let {id -> navController.navigate(id, pendingBundle) }
        clearPendingDestination()
    }

    override fun onSignInFailure() {
        clearPendingDestination()

    }
}