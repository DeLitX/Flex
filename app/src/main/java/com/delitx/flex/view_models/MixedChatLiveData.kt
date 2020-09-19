package com.delitx.flex.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.delitx.flex.pojo.BaseChatMessage

class MixedChatLiveData(liveDatas: List<LiveData<out List<BaseChatMessage>>>) :
    MediatorLiveData<List<BaseChatMessage>>() {
    private var mList = mutableListOf<BaseChatMessage>()

    init {
        for (i in liveDatas) {
            super.addSource(i) {
                mList = mergeLists(mList, it)
                value = mList
            }
        }
    }

    private fun mergeLists(
        list1: MutableList<BaseChatMessage>,
        list2: List<BaseChatMessage>
    ): MutableList<BaseChatMessage> {
        val result = mutableListOf<BaseChatMessage>()
        var a = 0
        var b = 0
        while (a < list1.size && b < list2.size) {
            if (list1[a].time > list2[b].time) {
                result.add(list1[a])
                a++
            } else if(list1[a].time<list2[b].time){
                result.add(list2[b])
                b++
            }else{
                if(list1[a].byUser==list2[b].byUser){
                    result.add(list2[b])
                    list1.removeAt(a)
                    b++
                }
            }
        }
        if (a < list1.size) {
            while (a < list1.size) {
                result.add(list1[a])
                a++
            }
        }
        if (b < list2.size) {
            while (b < list2.size) {
                result.add(list2[b])
                b++
            }
        }
        return result
    }
}