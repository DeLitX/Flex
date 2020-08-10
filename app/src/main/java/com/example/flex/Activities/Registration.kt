package com.example.flex.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.Enums.RequestEnum
import com.example.flex.ViewModels.AccountViewModel
import com.example.flex.R

class Registration : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private lateinit var mLogin: EditText
    private lateinit var mPassword: EditText
    private lateinit var mRepeatPassword: EditText
    private lateinit var mViewModel: AccountViewModel
    private lateinit var mUpdateBar: ProgressBar
    private lateinit var mTextBelowRegistration: TextView
    private lateinit var mSignUp: Button
    private lateinit var mNotReceivedEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)
        mViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        bindActivity()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mEmail.setText("")
        mLogin.setText("")
        mPassword.setText("")
        mRepeatPassword.setText("")
    }

    private fun bindActivity() {
        mEmail = findViewById(R.id.email)
        mLogin = findViewById(R.id.login)
        mPassword = findViewById(R.id.password)
        mRepeatPassword = findViewById(R.id.repeat_password)
        mUpdateBar = findViewById(R.id.register_update_circle)
        mUpdateBar.isIndeterminate = true
        mTextBelowRegistration = findViewById(R.id.text_below_registration)
        mSignUp = findViewById(R.id.sign_up_button)
        val haveAcc = findViewById<TextView>(R.id.have_acc)
        mNotReceivedEmail = findViewById(R.id.not_received_email)
        mViewModel.resendEmailStatus.observe(this, Observer {
            mUpdateBar.visibility = if (it == RequestEnum.IN_PROCESS) {
                View.VISIBLE
            } else {
                View.GONE
            }
            enableRegister(it != RequestEnum.IN_PROCESS)
            if (it == RequestEnum.FAILED) {
                Toast.makeText(this, getString(R.string.failed_resend_email), Toast.LENGTH_LONG)
                    .show()
            }
        })
        mNotReceivedEmail.setOnClickListener {
            mViewModel.resendEmail(mEmail.text.trim().toString())

        }
        mViewModel.isRegisterUpdating.observe(this, Observer {
            mUpdateBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
            enableRegister(!it)
        })
        mViewModel.isRegistSucceed.observe(this, Observer {
            if (it == false) {
                mTextBelowRegistration.text = getString(R.string.smth_went_wrong)
            } else if (it == true) {
                mTextBelowRegistration.text = getString(R.string.we_sent_letter)
            } else {
                mTextBelowRegistration.text = ""
            }
        })
        haveAcc.setOnClickListener {
            val intent = Intent(this, SignIn().javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        mSignUp.setOnClickListener {
            if (mPassword.text.toString().trim() == mRepeatPassword.text.toString().trim() &&
                mLogin.text.toString().trim() != "" && mEmail.text.toString().contains("@gmail.com")
            ) {
                mViewModel.register(
                    email = mEmail.text.toString(),
                    login = mLogin.text.toString(),
                    password = mPassword.text.toString()
                )

            } else if (mPassword.text.toString().trim() != mRepeatPassword.text.toString().trim()) {
                mTextBelowRegistration.text = getString(R.string.passwords_different)
            } else if (mLogin.text.toString().trim() == "") {
                mTextBelowRegistration.text = getString(R.string.nick_nust_not_empty)
            } else {
                mTextBelowRegistration.text = getString(R.string.email_must_finish)
            }
        }
    }

    fun enableRegister(value: Boolean) {
        mSignUp.isEnabled = value
        mEmail.isEnabled = value
        mLogin.isEnabled = value
        mPassword.isEnabled = value
        mRepeatPassword.isEnabled = value
        mNotReceivedEmail.isEnabled = value
    }
}