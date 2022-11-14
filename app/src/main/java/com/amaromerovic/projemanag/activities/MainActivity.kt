package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.activities.utils.Constants
import com.amaromerovic.projemanag.databinding.ActivityMainBinding
import com.amaromerovic.projemanag.databinding.NavHeaderMainBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.model.User
import com.bumptech.glide.Glide
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

        FirestoreHandler().loadUserData(this@MainActivity)

        binding.navView.setNavigationItemSelectedListener(this)

    }

    fun updateNavigationUserDetails(user: User) {
        Glide
            .with(this@MainActivity)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navViewHeaderBinding.circularImage);

        navViewHeaderBinding.userName.text = user.name.toString()

    }

    private fun goBack() {
        showProgressDialog()
        Handler.createAsync(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, StartActivity::class.java)
            intent.putExtra(Constants.DURATION_KEY, 0L)
            hideProgressDialog()
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }, 1000)
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
            R.id.myProfile -> {
                toggleDrawer()
                Handler.createAsync(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                }, 500)

            }
            R.id.singOut -> {
                FirebaseAuth.getInstance().signOut()
                goBack()
            }
        }
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.close()
        }
        return true
    }

}