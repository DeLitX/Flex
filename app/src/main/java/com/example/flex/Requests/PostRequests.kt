package com.example.flex.Requests

import com.example.flex.MainData
import com.example.flex.POJO.Comment
import com.example.flex.POJO.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy

class PostRequests(
    private val mPostRequestsInteraction: PostRequestsInteraction,
    private var csrftoken: String,
    private var sessionId: String
) {
    private val cookieManager = CookieManager()
    private val client: OkHttpClient


    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

    fun stopRequests() {
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == MainData.TAG_VIEW_ALL_POSTS_HOME ||
                call.request().tag() == MainData.TAG_VIEW_ALL_POSTS_ACCOUNT ||
                call.request().tag() == MainData.TAG_COMMENT ||
                call.request().tag() == MainData.TAG_LIKE ||
                call.request().tag() == MainData.TAG_UNLIKE
            ) {
                call.cancel()
            }
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == MainData.TAG_VIEW_ALL_POSTS_HOME ||
                call.request().tag() == MainData.TAG_VIEW_ALL_POSTS_ACCOUNT ||
                call.request().tag() == MainData.TAG_COMMENT ||
                call.request().tag() == MainData.TAG_LIKE ||
                call.request().tag() == MainData.TAG_UNLIKE
            ) {
                call.cancel()
            }
        }
    }

    fun unLikePost(post: Post) {
        val formBody = FormBody.Builder()
            .add("id", post.id.toString())
            .add("csrfmiddlewaretoken", csrftoken)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_UNLIKE)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.UNLIKE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    mPostRequestsInteraction.updatePost(post)
                } else if (response.code == MainData.ERR_403) {
                    makeUserSignIn()
                }
            }
        })
    }

    fun likePost(post: Post) {
        val formBody = FormBody.Builder()
            .add("id", post.id.toString())
            .add("csrfmiddlewaretoken", csrftoken)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_LIKE)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.LIKE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    mPostRequestsInteraction.updatePost(post)
                } else if (response.code == MainData.ERR_403) {
                    makeUserSignIn()
                }
            }
        })
    }

    fun commentPost(postId: Long, commentText: String) {
        val formBody = FormBody.Builder()
            .add("id", postId.toString())
            .add("comment", commentText)
            .add("csrfmiddlewaretoken", csrftoken)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_COMMENT)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.COMMENT}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {

                } else if (response.code == MainData.ERR_403) {
                    makeUserSignIn()
                }
            }
        })
    }

    fun viewAllPostsAccount(id: Long) {
        val formBody = FormBody.Builder()
            .add("id", id.toString())
            .add("csrfmiddlewaretoken", csrftoken)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_VIEW_ALL_POSTS_ACCOUNT)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.VIEW_ALL_POSTS}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
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
                            //val isSubscribed=jsonObject["isSubscribed"]
                            //val userName=jsonObject["name"]
                            val postsList = jsonObject["posts"]
                            val listOfPosts = mutableListOf<Post>()
                            if (postsList is JSONArray) {
                                val length = postsList.length()
                                for (i in 0 until length) {
                                    val value = postsList[i]
                                    if (value is JSONObject)
                                        listOfPosts.add(
                                            Post(
                                                id = value["post_id"].toString().toLong(),
                                                imageUrl = value["src"].toString(),
                                                date = value["date"].toString().toLong(),
                                                postText = value["description"].toString(),
                                                countOfFires = value["likes"].toString().toLong(),
                                                countOfComments = value["comments"].toString()
                                                    .toLong(),
                                                //imageUrlMini = value["src_mini"].toString(),
                                                isLiked = value["isLiked"].toString().toBoolean(),
                                                belongsTo = id
                                            )
                                        )
                                }
                            }
                            mPostRequestsInteraction.savePostsToDb(listOfPosts)
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                    makeUserSignIn()
                } else {

                }
            }
        })
    }

    fun viewAllPostsHome(lastId: Long) {
        val formBody = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_HOME)
            .addPathSegment(MainData.HOME)
            .addQueryParameter("id", lastId.toString())
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_VIEW_ALL_POSTS_HOME)
            .url(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
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
                            val postsList = jsonObject["posts"]
                            val listOfPosts = mutableListOf<Post>()
                            if (postsList is JSONArray) {
                                val length = postsList.length()
                                for (i in 0 until length) {
                                    val value = postsList[i]
                                    if (value is JSONObject)
                                        listOfPosts.add(
                                            Post(
                                                id = value["id"].toString().toLong(),
                                                imageUrl = value["src"].toString(),
                                                date = value["date"].toString().toLong(),
                                                postText = value["description"].toString(),
                                                countOfFires = value["likes"].toString()
                                                    .toLong(),
                                                countOfComments = value["comments"].toString()
                                                    .toLong(),
                                                isLiked = value["isLiked"].toString().toBoolean(),
                                                belongsTo = value["user_id"].toString().toLong(),
                                                showInFeed = true
                                            )
                                        )
                                }
                            }
                            mPostRequestsInteraction.savePostsToDb(listOfPosts)
                            mPostRequestsInteraction.setFeedRefreshState(false)
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                    makeUserSignIn()
                } else {

                }
            }
        })
    }

    fun viewCommentsToPost(postId: Long) {
        val formBody = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.VIEW_COMMENTS_TO_POST)
            .addQueryParameter("id", postId.toString())
            .build()
        val request = Request.Builder()
            .url(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
            .build()
        val call = client.newCall(request)

        val tempCoroutine = CoroutineScope(IO).launch {
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
                                val commentsList = jsonObject["comments"]
                                val listOfComments = mutableListOf<Comment>()
                                if (commentsList is JSONArray) {
                                    val length = commentsList.length()
                                    for (i in 0 until length) {
                                        val value = commentsList[i]
                                        if (value is JSONObject)
                                            listOfComments.add(
                                                Comment(
                                                    id = value["comment_id"].toString().toLong(),
                                                    userId = value["sender_id"].toString().toLong(),
                                                    text = value["description"].toString(),
                                                    timeSended = value["time"].toString()
                                                        .toLong(),
                                                    belongsToPost = postId
                                                )
                                            )
                                    }
                                }
                                mPostRequestsInteraction.saveCommentsToDb(listOfComments)
                            }
                        }
                    } else if (response.code == MainData.ERR_403) {
                        makeUserSignIn()
                    } else {

                    }
                }
            })
        }
    }

    private fun makeUserSignIn() {
        mPostRequestsInteraction.setMustSignIn(true)
    }

    interface PostRequestsInteraction {
        fun setMustSignIn(value: Boolean)
        fun savePostsToDb(posts: List<Post>)
        fun saveCommentsToDb(comments: List<Comment>)
        fun updatePost(post: Post)
        fun setFeedRefreshState(value: Boolean)
    }
}