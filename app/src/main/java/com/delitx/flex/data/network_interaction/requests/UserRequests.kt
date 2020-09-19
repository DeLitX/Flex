package com.delitx.flex.data.network_interaction.requests

import com.delitx.flex.MainData
import com.delitx.flex.pojo.Post
import com.delitx.flex.pojo.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

class UserRequests(
    private val mUserRequestsInteraction: UserRequestsInteraction,
    private val csrftoken: String,
    private val sessionId: String
) : BaseRequestFunctionality() {
    fun testNotification() {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment("test_fcm")
            .build()
        val request = Request.Builder().url(urlHttp)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (true) {

                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val body = response.body?.string()
                        if (body != null) {
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {
                    mUserRequestsInteraction.setErrorText(response.body?.string())
                }
            }
        })
    }

    fun viewAcc(userId: Long, actualUser: User?) {
        val formBody = FormBody.Builder()
            .add("id", userId.toString())
            .add("csrfmiddlewaretoken", csrftoken)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.VIEW_ACC}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=${csrftoken}; sessionid=${sessionId}")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val body = response.body?.string()
                        if (body != null) {
                            val jsonObject = JSONObject(body)
                            val keys = jsonObject.keys()
                            val idOfUser = jsonObject["isMyUser"]
                            val postsList = jsonObject["posts"]
                            val nameOfUser = jsonObject["user_name"]
                            val isSubscribed = jsonObject["isSubscribed"]
                            val listOfPosts = mutableListOf<Post>()
                            if (postsList is JSONArray) {
                                val length = postsList.length()
                                for (i in 0 until length) {
                                    val value = postsList[i]
                                    if (value is JSONObject) {
                                        listOfPosts.add(
                                            Post(
                                                id = value["post_id"].toString().toLong(),
                                                imageUrlMini = value["src_mini"].toString(),
                                                imageUrl = value["src"].toString(),
                                                date = value["date"].toString().toLong(),
                                                postText = value["description"].toString(),
                                                belongsTo = userId
                                            )
                                        )
                                    }
                                }
                            }
                            mUserRequestsInteraction.savePostsToDb(
                                listOfPosts,
                                idOfUser.toString().toLong()
                            )
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    suspend fun refreshUsersByIds(ids: List<Long>) {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.USERNAME_LIST)
            .addQueryParameter("id_list", longsListToJsonIdList(ids))
            .build()
        val request = Request.Builder().url(urlHttp)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        if (response.isSuccessful) {
            val body = response.body
            if (body != null) {
                try {
                    val bodyString = body.string()
                    val jsonObject = JSONObject(bodyString)
                    val jsonArray = jsonObject["username_list"]
                    if (jsonArray is JSONArray) {
                        val users: MutableList<User> = mutableListOf()
                        val length = jsonArray.length()
                        for (i in 0 until length) {
                            val value = jsonArray.get(i)
                            if (value is JSONObject) {
                                users.add(
                                    User(
                                        id = ids[i],
                                        name = value["username"].toString(),
                                        imageUrl = value["ava_src"].toString()
                                    )
                                )
                            }
                        }
                        //I know that here already saved users are being overrided by this dummies
                        mUserRequestsInteraction.saveUsersToDB(users)
                    }
                } catch (e: Exception) {
                    mUserRequestsInteraction.setErrorText(e.toString())
                }
            }
        }
    }

    fun viewFollowing() {
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.VIEW_SUBSCRIBES}")
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=${csrftoken}; sessionid=${sessionId}")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val body = response.body?.string()
                        if (body != null) {
                            val jsonObject = JSONObject(body)
                            val jsonArray = jsonObject["response"]
                            if (jsonArray is JSONArray) {
                                val listOfUsers = mutableListOf<User>()
                                val length = jsonArray.length()
                                for (i in 0 until length) {
                                    val value = jsonArray[i]
                                    if (value is JSONObject) {
                                        listOfUsers.add(
                                            User(
                                                id = value["id"].toString().toLong(),
                                                name = value["username"].toString(),
                                                imageUrl = if (value["ava_src"].toString() != "None") value["ava_src"].toString() else "",
                                                isSubscribed = true
                                            )
                                        )
                                    }
                                }
                                mUserRequestsInteraction.saveUsersToDB(listOfUsers)
                            }
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    suspend fun viewUserInformationAndSaveToDb(id: Long) {
        val urlHttp = if (id == 0.toLong()) {
            HttpUrl.Builder().scheme("https")
                .host(MainData.BASE_URL)
                .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
                .addPathSegment(MainData.VIEW_INFORMATION_USER)
                .build()
        } else {
            HttpUrl.Builder().scheme("https")
                .host(MainData.BASE_URL)
                .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
                .addPathSegment(MainData.VIEW_INFORMATION_USER)
                .addQueryParameter("id", id.toString())
                .build()
        }
        val request = Request.Builder().url(urlHttp)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        if (response.isSuccessful) {
            val body = response.body?.string()
            if (body != null) {
                val jsonObject = JSONObject(body)
                val keys = jsonObject.keys()
                val nameOfUser = jsonObject["user_name"]
                val followingCount = jsonObject["i_follower"]
                val imageUrl = jsonObject["ava_src"]
                val followersCount = jsonObject["followed"]
                val isSubscribed = jsonObject["isSubscribed"]
                val user = User(id = id)
                user.followersCount = followersCount.toString().toLong()
                user.followingCount = followingCount.toString().toLong()
                user.name = nameOfUser.toString()
                user.imageUrl = imageUrl.toString()
                user.isSubscribed = isSubscribed.toString().toBoolean()
                mUserRequestsInteraction.updateUserInDb(user)
            }
        } else if (response.code == MainData.ERR_403) {
            mUserRequestsInteraction.setMustSignIn(true)
        } else {

        }
    }


    suspend fun viewUserInformation(user: User) {
        val temp =
            HttpUrl.Builder().scheme("https")
                .host(MainData.BASE_URL)
                .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
                .addPathSegment(MainData.VIEW_INFORMATION_USER)
        if (user.id != 0L) {
            temp.addQueryParameter("id", user.id.toString())
        }
        val urlHttp = temp.build()
        val request = Request.Builder().url(urlHttp)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        if (response.isSuccessful) {
            val body = response.body?.string()
            if (body != null) {
                val jsonObject = JSONObject(body)
                val keys = jsonObject.keys()
                val nameOfUser = jsonObject["user_name"]
                val followingCount = jsonObject["i_follower"]
                val imageUrl = jsonObject["ava_src"]
                val followersCount = jsonObject["followed"]
                val isSubscribed = jsonObject["isSubscribed"]
                user.followersCount = followersCount.toString().toLong()
                user.followingCount = followingCount.toString().toLong()
                user.name = nameOfUser.toString()
                user.imageUrl = imageUrl.toString()
                user.isSubscribed = isSubscribed.toString().toBoolean()
                mUserRequestsInteraction.updateUserInDb(user)
            }

        } else if (response.code == MainData.ERR_403) {
            mUserRequestsInteraction.setMustSignIn(true)
        } else {

        }
    }

    fun follow(userId: Long) {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.FOLLOW)
            .addQueryParameter("id", userId.toString())
            .build()
        val request = Request.Builder().url(urlHttp)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val isLogin = response.header("isLogin", "true")
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    fun unfollow(userId: Long) {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.UNFOLLOW)
            .addQueryParameter("id", userId.toString())
            .build()
        val request = Request.Builder().url(urlHttp)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val isLogin = response.header("isLogin", "true")
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    interface UserRequestsInteraction {
        fun setFollowingCount(userId: Long, count: Long)
        fun setFollowersCount(userId: Long, count: Long)
        fun follow(userId: Long)
        fun unfollow(userId: Long)
        fun setMustSignIn(value: Boolean)
        fun savePostsToDb(posts: List<Post>, idOfUser: Long)
        fun updateUserInDb(user: User)
        fun saveUsersToDB(users: List<User>)
        fun setErrorText(text: String?)
    }

}