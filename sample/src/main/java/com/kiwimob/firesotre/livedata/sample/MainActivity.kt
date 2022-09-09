package com.kiwimob.firesotre.livedata.sample

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.kiwimob.firesotre.livedata.sample.databinding.ActivityMainBinding
import com.kiwimob.firestore.livedata.QueryStatus
import com.kiwimob.firestore.livedata.livedata

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()
        }

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .livedata()
            .observe(this, Observer<QueryStatus<QuerySnapshot>> {
                Log.d("MainActivity", "${it?.answer?.size()}")
            })

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .livedata(User::class.java)
            .observe(this, Observer<QueryStatus<List<User>>> {
                Log.d("MainActivity", "${it?.answer?.size}")
            })

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .livedata { parseUser(it) }
            .observe(this, Observer<QueryStatus<List<User>>> {
                Log.d("MainActivity", "${it?.answer?.size}")
            })
    }

    private fun parseUser(documentSnapshot: DocumentSnapshot) : User {
        return User(name = documentSnapshot.getString("name"), email = documentSnapshot.getString("email"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}