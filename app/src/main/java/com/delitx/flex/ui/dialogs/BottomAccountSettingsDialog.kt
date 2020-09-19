package com.delitx.flex.ui.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.delitx.flex.R
import com.delitx.flex.ui.activities.SignIn
import com.delitx.flex.view_models.AccountViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.account_settings_bootom_dialog.*

class BottomAccountSettingsDialog():BottomSheetDialogFragment(){
    private lateinit var v:View
    private lateinit var mViewModel:AccountViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v=inflater.inflate(R.layout.account_settings_bootom_dialog,container,false)
        mViewModel=ViewModelProviders.of(this).get(AccountViewModel::class.java)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindFragment()
    }
    private fun bindFragment(){
        leave.setOnClickListener {
            mViewModel.logout()
            val intent = Intent(this.context, SignIn::class.java)
            startActivity(intent)
            activity?.finish()
            dismiss()
        }
    }
}