package com.amaromerovic.projemanag.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.BoardListAdapter
import com.amaromerovic.projemanag.databinding.ActivityMainBinding
import com.amaromerovic.projemanag.databinding.NavHeaderMainBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewHeader: View
    private lateinit var navViewHeaderBinding: NavHeaderMainBinding
    private lateinit var userName: String
    private lateinit var sharedPreferences: SharedPreferences

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

        if (!Constants.isNetworkAvailable(this@MainActivity)) {
            noInternetConnectionDialog()
        }

            sharedPreferences =
            this.getSharedPreferences(Constants.PROJEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog()
            FirestoreHandler().loadUserData(this@MainActivity, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
                updateFCMToken(result)
            }
        }

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


        binding.appBarId.mainContentLayout.refreshLayout.setOnRefreshListener {
            binding.appBarId.mainContentLayout.refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.darkBlue
                ),
                ContextCompat.getColor(this@MainActivity, R.color.darkBlue),
                ContextCompat.getColor(this@MainActivity, R.color.darkBlue)
            )
            if (binding.appBarId.mainContentLayout.refreshLayout.isRefreshing) {
                binding.appBarId.mainContentLayout.refreshLayout.isRefreshing = false
            }
            showProgressDialog()
            FirestoreHandler().getBoardList(this)
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog()
        FirestoreHandler().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog()
        FirestoreHandler().updateUserProfileData(this@MainActivity, userHashMap)
    }

    fun populateBoardsListToUI(boards: ArrayList<Board>) {
        hideProgressDialog()

        if (boards.size > 0) {
            binding.appBarId.mainContentLayout.recyclerView.visibility = View.VISIBLE
            binding.appBarId.mainContentLayout.noBoardsText.visibility = View.GONE

            binding.appBarId.mainContentLayout.recyclerView.layoutManager =
                LinearLayoutManager(this)
            binding.appBarId.mainContentLayout.recyclerView.setHasFixedSize(true)

            val adapter = BoardListAdapter(this@MainActivity, boards)
            binding.appBarId.mainContentLayout.recyclerView.adapter = adapter

            adapter.onBoardItemClickListener(object : BoardListAdapter.OnBoardItemClick {
                override fun onItemClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentID)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            })
        } else {
            binding.appBarId.mainContentLayout.recyclerView.visibility = View.GONE
            binding.appBarId.mainContentLayout.noBoardsText.visibility = View.VISIBLE
        }


    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean) {
        hideProgressDialog()
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
                sharedPreferences.edit().clear().apply()
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