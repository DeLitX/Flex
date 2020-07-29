package com.example.flex.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.Fragments.*
import com.example.flex.MainData
import com.example.flex.POJO.User
import com.example.flex.R
import com.example.flex.ViewModels.AccountViewModel
import com.example.flex.ViewModels.BaseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ChatActivity.ChatInteraction {
    var account = MainUserAccountFragment()
    var home = HomeFragment()
    var tv = TvFragment()
    var map = MapFragment()
    var camera = CameraFragment()
    lateinit var bnv: BottomNavigationView
        private set
    lateinit var mViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val action = intent?.action
        val data = intent?.data
        val sharedPreferences = getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
        val sessionId = sharedPreferences.getString(MainData.SESSION_ID, "")
        val csrftoken = sharedPreferences.getString(MainData.CRSFTOKEN, "")
        val id:Long = intent.getLongExtra(MainData.EXTRA_GO_TO_USER_ID, 0L)
        if (sessionId == "" || csrftoken == "") {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
            finish()
        }
        mViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)

        if (id == 0L) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, home, "fragment_tag")
                .commit()
        }else{
            CoroutineScope(IO).launch {
                val user:User?=mViewModel.getUserValueFromDB(id)
                if(user!=null){
                    withContext(Main){
                     goToUser(user)
                    }
                }
            }
        }
        setActionListener()
    }

    private fun setActionListener() {
        bnv = findViewById(R.id.bottom_bar)
        bnv.setOnNavigationItemSelectedListener { menuItem ->
            //here code in case of choosing item in bottom bar
            var isAddToBackStack = true
            val selectedFragment: Fragment =
                when (menuItem.itemId) {
                    R.id.action_account -> {
                        isAddToBackStack = true
                        if (supportFragmentManager.findFragmentByTag("fragment_tag") == account) {
                            isAddToBackStack = false
                        }
                        account
                    }
                    R.id.action_camera -> {
                        isAddToBackStack = true
                        if (supportFragmentManager.findFragmentByTag("fragment_tag") == camera) {
                            isAddToBackStack = false
                        }
                        camera
                    }
                    R.id.action_home -> {
                        if (supportFragmentManager.findFragmentByTag("fragment_tag") == home) {
                            home.scrollToBeginning()
                        }
                        home
                    }
                    R.id.action_map -> {
                        isAddToBackStack = true
                        if (supportFragmentManager.findFragmentByTag("fragment_tag") == map) {
                            isAddToBackStack = false
                        }
                        map
                    }
                    R.id.action_tv -> {
                        isAddToBackStack = true
                        if (supportFragmentManager.findFragmentByTag("fragment_tag") == tv) {
                            isAddToBackStack = false
                        }
                        tv
                    }
                    else -> supportFragmentManager.findFragmentById(R.id.frame_container)!!
                }
            val fragmentManager = supportFragmentManager.beginTransaction()
            fragmentManager.replace(R.id.frame_container, selectedFragment, "fragment_tag")
            if (isAddToBackStack) fragmentManager.addToBackStack(null)
            fragmentManager.commit()
            true
        }
    }

    override fun goToUser(user: User) {
        if (user.name != "") {
            val sharedPreferences =
                getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
            if (user.id == sharedPreferences.getLong(MainData.YOUR_ID, 0)) {
                val fragment = MainUserAccountFragment()
                fragment.mUser = user
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).addToBackStack(null).commit()
            } else {
                val fragment = AccountFragment()
                fragment.mUser = user
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment, "fragment_tag").addToBackStack(null)
                    .commit()
            }
        }
        mViewModel.setGoToUser(null)
    }
}
