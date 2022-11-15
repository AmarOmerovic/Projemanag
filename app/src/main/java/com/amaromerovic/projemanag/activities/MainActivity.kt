package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.BoardItemAdapter
import com.amaromerovic.projemanag.databinding.ActivityMainBinding
import com.amaromerovic.projemanag.databinding.NavHeaderMainBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewHeader: View
    private lateinit var navViewHeaderBinding: NavHeaderMainBinding
    private lateinit var userName: String

    private val startUpdateActivityAndGetResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                FirestoreHandler().loadUserData(this)
            }
        }

    private val startBoardListUpdate =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                FirestoreHandler().getBoardList(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewHeader = binding.navView.getHeaderView(0)
        navViewHeaderBinding = NavHeaderMainBinding.bind(viewHeader)
        setContentView(binding.root)

        FirestoreHandler().loadUserData(this@MainActivity, true)

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

        binding.navView.setNavigationItemSelectedListener(this)

        binding.appBarId.createBoard.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.USER_NAME, userName)
            startBoardListUpdate.launch(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

    fun populateBoardsListToUI(boards: ArrayList<Board>) {
        hideProgressDialog()

        if (boards.size > 0) {
            binding.appBarId.mainContentLayout.recyclerView.visibility = View.VISIBLE
            binding.appBarId.mainContentLayout.noBoardsText.visibility = View.GONE

            binding.appBarId.mainContentLayout.recyclerView.layoutManager =
                LinearLayoutManager(this)
            binding.appBarId.mainContentLayout.recyclerView.setHasFixedSize(true)

            val adapter = BoardItemAdapter(this@MainActivity, boards)
            binding.appBarId.mainContentLayout.recyclerView.adapter = adapter
        } else {
            binding.appBarId.mainContentLayout.recyclerView.visibility = View.GONE
            binding.appBarId.mainContentLayout.noBoardsText.visibility = View.VISIBLE
        }


    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean) {

        userName = user.name.toString()

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navViewHeaderBinding.circularImage)

        navViewHeaderBinding.userName.text = user.name.toString()

        if (readBoardList) {
            showProgressDialog()
            FirestoreHandler().getBoardList(this)
        }

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
                    startUpdateActivityAndGetResult.launch(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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