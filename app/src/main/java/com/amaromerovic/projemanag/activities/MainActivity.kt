package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.activities.utils.Constants
import com.amaromerovic.projemanag.databinding.ActivityMainBinding
import com.amaromerovic.projemanag.databinding.NavHeaderMainBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.model.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewHeader: View
    private lateinit var navViewHeaderBinding: NavHeaderMainBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewHeader = binding.navView.getHeaderView(0)
        navViewHeaderBinding = NavHeaderMainBinding.bind(viewHeader)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarId.toolbarMainActivity)
        binding.appBarId.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_nav_menu)
        binding.appBarId.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isOpen) {
                    binding.drawerLayout.close()
                } else {
                    doubleBackToExit()
                }
            }
        })

        firestore = FirebaseFirestore.getInstance()

        firestore.collection(Constants.USERS_COLLECTION_KEY)
            .document(FirestoreHandler().getCurrentUserUID())
            .get()
            .addOnSuccessListener {
                val loggedInUser = it.toObject(User::class.java)
                navViewHeaderBinding.userName.text = loggedInUser?.name.toString()
            }


        binding.navView.setNavigationItemSelectedListener(this)

    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.close()
        } else {
            binding.drawerLayout.open()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.myProfile -> Toast.makeText(this@MainActivity, "My Profile", Toast.LENGTH_LONG)
                .show()
            R.id.singOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, StartActivity::class.java)
                intent.putExtra(Constants.DURATION_KEY, 0L)
                startActivity(intent)
                finish()
            }
        }
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.close()
        }
        return true
    }

}