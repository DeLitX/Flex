package com.example.flex.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.flex.*
import com.example.flex.Activities.ForgotPass
import com.example.flex.Activities.MakePostActivity
import com.example.flex.Activities.SignIn
import com.example.flex.ViewModels.AccountViewModel

class CameraFragment : Fragment() {
    lateinit var v: View
    private lateinit var mViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_photo, container, false)

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        mViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(this.context, SignIn::class.java)
                startActivity(intent)
                activity?.finish()
            }
        })
        mViewModel.errorText.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Toast.makeText(this.context, it, Toast.LENGTH_LONG).show()
            }
        })
        addActionListener()
        return v
    }

    private fun addActionListener() {
        val btnLogout = v.findViewById<Button>(R.id.button_logout)
        val changePassBtn = v.findViewById<Button>(R.id.button_change_pass)
        val makePostBtn = v.findViewById<Button>(R.id.button_make_post)
        val testNotificationBtn = v.findViewById<Button>(R.id.button_test_notification)
        testNotificationBtn.setOnClickListener {
            mViewModel.testNotification()
        }
        makePostBtn.setOnClickListener {
            val intent = Intent(this.context, MakePostActivity::class.java)
            startActivity(intent)
        }
        btnLogout.setOnClickListener {
            mViewModel.logout()
        }
        changePassBtn.setOnClickListener {
            val intent = Intent(v.context, ForgotPass().javaClass)
            startActivity(intent)
        }

    }
}