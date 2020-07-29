package com.example.flex.Activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.ViewModels.AccountViewModel
import com.example.flex.R

class ForgotPass : AppCompatActivity() {
    private lateinit var mEmailText: EditText
    private lateinit var mNewPass: EditText
    private lateinit var mEmailCode: EditText
    private lateinit var mForgotPassBtn: Button
    private lateinit var mNewPassBtn: Button
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mViewModel: AccountViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)
        mViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        mViewModel.isPasswordCanBeChanged.observe(this, Observer {
            if (it==true) {
                onForgotPass()
            }
        })
        mSharedPreferences = getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
        if (mSharedPreferences.getBoolean("isEnabled", false)) {
            onForgotPass()
            mEmailCode.setText(mSharedPreferences.getString("email code", ""))
            mNewPass.setText(mSharedPreferences.getString("new password", ""))
        }
        setActionListener()
    }

    private fun setActionListener() {
        mEmailText = findViewById(R.id.forgot_pass_email)
        mForgotPassBtn = findViewById(R.id.forgot_pass_button)
        mEmailCode = findViewById(R.id.change_pass_code)
        mNewPass = findViewById(R.id.change_pass_pass)
        mNewPassBtn = findViewById(R.id.change_pass_button)
        mForgotPassBtn.setOnClickListener {
            mViewModel.forgotPassword(mEmailText.text.toString())
        }
        mNewPassBtn.setOnClickListener {
            mViewModel.changePassword(
                email = mEmailText.text.toString(),
                newPassword = mNewPass.text.toString(),
                checkCode = mEmailCode.text.toString()
            )
        }
    }

    private fun onForgotPass() {
        val editor = mSharedPreferences.edit()
        editor.putBoolean("isEnabled", true)
        editor.apply()
        mEmailCode.visibility = View.VISIBLE
        mNewPass.visibility = View.VISIBLE
        mNewPassBtn.visibility = View.VISIBLE
    }
}