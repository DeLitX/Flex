package com.example.flex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.User

class SearchViewModel(private val app: Application):AndroidViewModel(app) {
    private val mRepository:Repository= Repository(app)
    val isMustSignIn:LiveData<Boolean?>
    val searchResult: LiveData<List<User>>
    val isSearchUpdates:LiveData<Boolean>
    init {
        searchResult= mRepository.searchResult
        isMustSignIn=mRepository.isMustSignIn
        isSearchUpdates=mRepository.isSearchUpdating
    }
    fun search(query:String){
        mRepository.search(query)
    }
}