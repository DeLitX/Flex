package com.example.flex.Fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.flex.*
import com.example.flex.Activities.ForgotPass
import com.example.flex.Activities.MakePostActivity
import com.example.flex.Activities.Registration
import com.example.flex.Activities.SignIn

class CameraFragment : Fragment() {
    lateinit var v: View
    private lateinit var mViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_photo, container, false)
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 201
            )
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        mViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(this.context, SignIn::class.java)
                startActivity(intent)
                activity?.finish()
            }
        })
        addActionListener()
        return v
    }

    fun addActionListener() {
        val btnRegistration = v.findViewById<Button>(R.id.button_registration)
        val btnLogIn = v.findViewById<Button>(R.id.button_login)
        val btnLogout = v.findViewById<Button>(R.id.button_logout)
        val btnCheckLog = v.findViewById<Button>(R.id.button_checkLog)
        val changePassBtn = v.findViewById<Button>(R.id.button_change_pass)
        val makePostBtn = v.findViewById<Button>(R.id.button_make_post)
        makePostBtn.setOnClickListener {
            val intent = Intent(this.context, MakePostActivity::class.java)
            startActivity(intent)
        }
        if (this.context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this.activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.CAMERA),
                100
            )
        }
        btnCheckLog.setOnClickListener {
            mViewModel.checkLog()
        }
        btnLogout.setOnClickListener {
            mViewModel.logout()
        }
        changePassBtn.setOnClickListener {
            val intent = Intent(v.context, ForgotPass().javaClass)
            startActivity(intent)
        }
        btnRegistration.setOnClickListener {
            val intent = Intent(v.context, Registration().javaClass)
            startActivity(intent)
        }
        btnLogIn.setOnClickListener {
            val intent = Intent(v.context, SignIn().javaClass)
            startActivity(intent)
        }

    }
}