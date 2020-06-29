package com.example.flex.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.net.HttpCookie

class SignIn : AppCompatActivity() {
    private lateinit var mLogin: EditText
    private lateinit var mPassword: EditText
    private lateinit var mViewModel: AccountViewModel
    private lateinit var mUpdateBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        mViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        mViewModel.isMustSignIn.observe(this, Observer {
            if (it == false) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        setActionListener()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mLogin.setText("")
        mPassword.setText("")
    }

    private fun setActionListener() {
        val signInButton = findViewById<Button>(R.id.sign_in_button)
        mUpdateBar = findViewById(R.id.login_update_circle)
        mViewModel.isLoginUpdating.observe(this, Observer {
            mUpdateBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
            mUpdateBar.isIndeterminate = it
            signInButton.isEnabled = !it
        })
        mLogin = findViewById(R.id.login)
        mPassword = findViewById(R.id.password)
        val dontAcc = findViewById<TextView>(R.id.dont_acc)
        dontAcc.setOnClickListener {
            val intent = Intent(this, Registration().javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        signInButton.setOnClickListener {
            if (mPassword.text.toString().trim() != "" && mLogin.text.toString().trim() != "") {
                mViewModel.login(mLogin.text.toString(), mPassword.text.toString())
            } else
                Toast.makeText(this, "try again", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun setCookies(cookies: List<HttpCookie>, id: Long) {
        val sharedPreferences = getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(MainData.CRSFTOKEN, cookies[0].value)
        editor.putString(MainData.SESSION_ID, cookies[1].value)
        editor.putLong(MainData.YOUR_ID, id)
        editor.apply()
        withContext(Main) {
            val intent = Intent(applicationContext, MainActivity().javaClass)
            startActivity(intent)
            finish()
        }
    }
}
