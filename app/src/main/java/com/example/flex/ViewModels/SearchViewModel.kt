package com.example.flex.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.User
import com.example.flex.Repository

class SearchViewModel(private val app: Application):BaseViewModel(app) {
    val searchResult: LiveData<List<User>>
    val isSearchUpdates:LiveData<Boolean>
    init {
        searchResult= mRepository.searchResult
        isSearchUpdates=mRepository.isSearchUpdating
    }
    fun search(query:String){
        mRepository.search(query)
    }
}