package com.delitx.flex.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import com.delitx.flex.pojo.User

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