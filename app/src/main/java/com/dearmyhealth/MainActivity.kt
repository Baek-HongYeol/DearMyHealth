package com.dearmyhealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dearmyhealth.activities.LoginActivity
import com.dearmyhealth.activities.ui.BottomSheetAccountDialogFragment
import com.dearmyhealth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName

    private lateinit var binding : ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var menu: Menu? = null
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navController: NavController

    private var backPressedTime: Long = 0
    private val onBackpressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedKey()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        while(Session.currentUser == null){
//            intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        initView()
        observeViewModel()
        this.onBackPressedDispatcher.addCallback(onBackpressedCallback)
    }
    private fun initView() {
        val toolbar : Toolbar = binding.appbar.toolbar
        setSupportActionBar(toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        binding.navigationView.getHeaderView(0)
            ?.findViewById<TextView>(R.id.change_account)?.setOnClickListener {
                val bottomSHeet = BottomSheetAccountDialogFragment()
                bottomSHeet.show(supportFragmentManager, bottomSHeet.tag)
        }
    }
    private fun observeViewModel() {
        viewModel.isLoggedIn.observe(this) { value ->
            setVisibleLoginMenu(!value)
        }
    }

    override fun onResume() {
        viewModel.checkSession()
        super.onResume()
    }

    fun onBackPressedKey() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        else if(System.currentTimeMillis() > backPressedTime+2000) {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        else if(System.currentTimeMillis() <= backPressedTime + 2000) {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.activity_home_toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_drawer_menu -> {
                drawerLayout.openDrawer(GravityCompat.END)}
        }
        return super.onOptionsItemSelected(item)
    }
    fun setVisibleLoginMenu(isShow:Boolean) {
        binding.navigationView.getHeaderView(0).isVisible = isShow
        binding.navigationView.menu.findItem(R.id.drawer_login).isVisible = isShow
    }
}