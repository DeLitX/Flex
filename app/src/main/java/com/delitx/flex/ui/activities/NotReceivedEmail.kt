package com.delitx.flex.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.delitx.flex.R
import kotlinx.android.synthetic.main.activity_not_received_email.*

class NotReceivedEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_received_email)
        bindActivity()
    }
    private fun bindActivity(){
        resend_email.setOnClickListener {
            if(resend_email_text.text.toString().contains("@gmail.com")){
                email_must_finish.visibility= View.GONE
            }else{
                email_must_finish.visibility= View.GONE
            }
        }
    }
}