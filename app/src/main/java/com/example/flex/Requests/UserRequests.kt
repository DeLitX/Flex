package com.example.flex.Requests

import com.example.flex.MainData
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy

class UserRequests(
    private val mUserRequestsInteraction: UserRequestsInteraction,
    private val csrftoken: String,
    private val sessionId: String
) {
    private val client: OkHttpClient
    private val cookieManager = CookieManager()

    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

    fun viewAcc(userId: Long, actualUser: User?) {
        val formBody = FormBody.Builder()
            .add("id", userId.toString())
            .add("csrfmiddlewaretoken", csrftoken)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_VIEW_ACC)
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
                            /*var user: User = if (actualUser != null) {
                                User(
                                    id = idOfUser.toString().toLong(),
                                    name = nameOfUser.toString(),
                                    followingCount = actualUser.followingCount,
                                    followersCount = actualUser.followersCount,
                                    imageUrl = actualUser.imageUrl,
                                    isSubscribed = isSubscribed.toString().toBoolean()
                                )
                            } else {
                                User(
                                    id = idOfUser.toString().toLong(),
                                    isSubscribed = isSubscribed.toString().toBoolean(),
                                    name = nameOfUser.toString()
                                )
                            }*/
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    fun viewUserInformation(user: User) {
        val urlHttp = if (user.id == 0.toLong()) {
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
                .addQueryParameter("id", user.id.toString())
                .build()
        }
        val request = Request.Builder().url(urlHttp)
            .tag(MainData.TAG_GET_FOLLOWING_COUNT)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if(true){

                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
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
                    }
                } else if (response.code == MainData.ERR_403) {
                    mUserRequestsInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    fun follow(userId: Long) {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.FOLLOW)
            .addQueryParameter("id", userId.toString())
            .build()
        val request = Request.Builder().url(urlHttp)
            .tag(MainData.TAG_FOLLOW)
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
                    mUserRequestsInteraction.follow(userId)
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
            .tag(MainData.TAG_FOLLOW)
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
                    mUserRequestsInteraction.unfollow(userId)
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
        fun unfollow(userId:Long)
        fun setMustSignIn(value:Boolean)
        fun savePostsToDb(posts: List<Post>, idOfUser: Long)
        fun updateUserInDb(user: User)
    }

}