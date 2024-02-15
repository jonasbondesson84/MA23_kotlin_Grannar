package com.example.grannar


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


interface SignInResultListener {
    fun onSignInSuccess()
    fun onSignInFailure()

    fun onSignUpPress()
}
class MainActivity : AppCompatActivity(), SignInResultListener {

    private var pendingDestination: NavDestination? = null
    private var pendingBundle: Bundle? = null
    private lateinit var signUpLauncher: ActivityResultLauncher<Intent>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.nav_host_fragment)

        signUpLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                val signedUp = data?.getBooleanExtra("signed_up", false) ?: false
                if (signedUp) {
                    navController.navigate(R.id.profileFragment)
                }
            }
        }




        //Gets logged in users from database and save it to CurrentUser
        val auth = FirebaseAuth.getInstance()
        if( auth.currentUser != null) {
            CurrentUser.loadUserInfo(auth.uid.toString())
        }

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

    override fun onSignUpPress() {
        val intent = Intent(this, SignUpActivity::class.java)
        signUpLauncher.launch(intent)
        //startActivity(intent)
    }


}