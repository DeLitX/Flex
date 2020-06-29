package com.example.flex.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.AccountViewModel
import com.example.flex.R

class Registration : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private lateinit var mLogin: EditText
    private lateinit var mPassword: EditText
    private lateinit var mRepeatPassword: EditText
    private lateinit var mViewModel: AccountViewModel
    private lateinit var mUpdateBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)
        mViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        setActionListener()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mEmail.setText("")
        mLogin.setText("")
        mPassword.setText("")
        mRepeatPassword.setText("")
    }

    private fun setActionListener() {
        mEmail = findViewById(R.id.email)
        mLogin = findViewById(R.id.login)
        mPassword = findViewById(R.id.password)
        mRepeatPassword = findViewById(R.id.repeat_password)
        mUpdateBar = findViewById(R.id.register_update_circle)
        val signUp = findViewById<Button>(R.id.sign_up_button)
        val haveAcc = findViewById<TextView>(R.id.have_acc)
        mViewModel.isRegisterUpdating.observe(this, Observer {
            mUpdateBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
            signUp.isEnabled = !it
            mUpdateBar.isIndeterminate = it
        })
        haveAcc.setOnClickListener {
            val intent = Intent(this, SignIn().javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        signUp.setOnClickListener {
            if (mPassword.text.toString().trim() == mRepeatPassword.text.toString().trim() &&
                mLogin.text.toString().trim() != "" && mEmail.text.toString().contains("@gmail.com")
            ) {
                mViewModel.register(
                    email = mEmail.text.toString(),
                    login = mLogin.text.toString(),
                    password = mPassword.text.toString()
                )
                Toast.makeText(
                    this,
                    "We sent a letter on your Email.Please follow the link and then sign in",
                    Toast.LENGTH_LONG
                ).show()
            } else
                Toast.makeText(this, "try again", Toast.LENGTH_LONG).show()
        }
    }
}