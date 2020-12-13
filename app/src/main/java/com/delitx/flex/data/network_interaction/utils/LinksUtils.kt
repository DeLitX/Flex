package com.delitx.flex.data.network_interaction.utils

class LinksUtils {
    companion object{
        fun comparePhotoLinks(link1:String,link2:String):Boolean{
            return link1.split("?")[0]==
                    link2.split("?")[0]
        }
    }
}