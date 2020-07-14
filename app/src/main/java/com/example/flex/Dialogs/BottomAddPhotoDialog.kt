package com.example.flex.Dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.flex.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomAddPhotoDialog(private val mPhotoInteraction:PhotoInteraction):BottomSheetDialogFragment(){
    private lateinit var v:View
    private lateinit var mTakePhoto:TextView
    private lateinit var mGetPhoto:TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v=inflater.inflate(R.layout.get_photo_bottom_sheet,container,false)
        bindFragment()
        return v
    }
    private fun bindFragment(){
        mTakePhoto=v.findViewById(R.id.take_photo)
        mGetPhoto=v.findViewById(R.id.get_photo)
        mTakePhoto.setOnClickListener {
            mPhotoInteraction.takeImage()
            dismiss()
        }
        mGetPhoto.setOnClickListener {
            mPhotoInteraction.getImage()
            dismiss()
        }
    }
    interface PhotoInteraction{
        fun takeImage()
        fun getImage()
    }
}