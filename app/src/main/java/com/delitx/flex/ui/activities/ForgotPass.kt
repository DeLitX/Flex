package com.delitx.flex.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delitx.flex.view_models.AccountViewModel
import com.delitx.flex.R
import com.delitx.flex.enums_.RequestEnum
import kotlinx.android.synthetic.main.activity_change_password.*

class ForgotPass : AppCompatActivity() {
    private lateinit var mEmailText: EditText
    private lateinit var mNewPass: EditText
    private lateinit var mEmailCode: EditText
    private lateinit var mForgotPassBtn: Button
    private lateinit var mNewPassBtn: Button
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mViewModel: AccountViewModel
    private var mProgressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        mViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        mViewModel.isPasswordCanBeChanged.observe(this, Observer {
            if (it == true) {
                onForgotPass()
            }
        })
        mViewModel.forgotPassStatus.observe(this) {
            if (mProgressBar != null) {
                if (it == RequestEnum.IN_PROCESS) {
                    mProgressBar!!.visibility=View.VISIBLE
                    mProgressBar!!.isIndeterminate=true
                } else if (it == RequestEnum.FAILED) {
                    mProgressBar!!.visibility=View.GONE
                    Toast.makeText(this,R.string.failed_password,Toast.LENGTH_LONG).show()
                } else if (it == RequestEnum.FAILED) {
                    mProgressBar!!.visibility=View.GONE
                    finish()
                }
            }
        }
        mSharedPreferences = getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
        if (mSharedPreferences.getBoolean("isEnabled", false)) {
            onForgotPass()
            mEmailCode.setText(mSharedPreferences.getString("email code", ""))
            mNewPass.setText(mSharedPreferences.getString("new password", ""))
        }
        setActionListener()
    }

    private fun setActionListener() {
        mEmailText = findViewById(R.id.resend_email_text)
        mForgotPassBtn = findViewById(R.id.resend_email)
        mEmailCode = findViewById(R.id.change_pass_code)
        mNewPass = findViewById(R.id.change_pass_pass)
        mNewPassBtn = findViewById(R.id.change_pass_button)
        mProgressBar = requst_state
        mForgotPassBtn.setOnClickListener {
            if (mEmailText.text.toString().contains("@gmail.com")) {
                email_must_finish.visibility = View.GONE
                mViewModel.forgotPassword(mEmailText.text.toString())
            } else {
                email_must_finish.visibility = View.VISIBLE
            }
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