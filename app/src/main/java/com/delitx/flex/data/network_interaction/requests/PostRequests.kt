package com.delitx.flex.data.network_interaction.requests

import com.delitx.flex.MainData
import com.delitx.flex.data.network_interaction.exceptions.UnsuccessfulRequestException
import com.delitx.flex.data.network_interaction.exceptions.UserNotLoginedException
import com.delitx.flex.pojo.Comment
import com.delitx.flex.pojo.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException

class PostRequests(
    private var csrftoken: String,
    private var sessionId: String
) : BaseRequestFunctionality() {

    suspend fun unLikePost(post: Post) {
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
        try {
            val response = call.execute()
            if (response.isSuccessful) {

            } else if (response.code == MainData.ERR_403) {
                throw UserNotLoginedException()
            } else {
                throw UnsuccessfulRequestException()
            }
        } catch (e: SocketTimeoutException) {
            throw UnsuccessfulRequestException()
        }
    }

    suspend fun likePost(post: Post) {
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
        try {
            val response = call.execute()
            if (response.isSuccessful) {

            } else if (response.code == MainData.ERR_403) {
                throw UserNotLoginedException()
            } else {
                throw UnsuccessfulRequestException()
            }
        } catch (e: SocketTimeoutException) {
            throw UnsuccessfulRequestException()
        }
    }

    suspend fun commentPost(postId: Long, commentText: String) {
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
        try {
            val response = call.execute()
            if (response.isSuccessful) {

            } else if (response.code == MainData.ERR_403) {
                throw UserNotLoginedException()
            } else {
                throw UnsuccessfulRequestException()
            }
        } catch (e: SocketTimeoutException) {
            throw UnsuccessfulRequestException()
        }
    }

    suspend fun viewAllPostsAccount(id: Long): List<Post> {
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
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val listOfPosts = mutableListOf<Post>()
                    withContext(Default) {
                        val jsonObject = JSONObject(body)
                        val keys = jsonObject.keys()
                        val idOfUser = jsonObject["isMyUser"]
                        //val isSubscribed=jsonObject["isSubscribed"]
                        //val userName=jsonObject["name"]
                        val postsList = jsonObject["posts"]
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
                    }
                    return listOfPosts
                }
            } else if (response.code == MainData.ERR_403) {
                throw UserNotLoginedException()
            }
            throw UnsuccessfulRequestException()
        } catch (e: SocketTimeoutException) {
            throw UnsuccessfulRequestException()
        }
    }

    suspend fun viewAllPostsHome(lastId: Long): List<Post> {
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
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val listOfPosts = mutableListOf<Post>()
                    withContext(Default) {
                        val jsonObject = JSONObject(body)
                        val keys = jsonObject.keys()
                        val postsList = jsonObject["posts"]
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
                    }
                    return listOfPosts
                }
            } else if (response.code == MainData.ERR_403) {
                throw UserNotLoginedException()
            }
            throw UnsuccessfulRequestException()
        } catch (e: SocketTimeoutException) {
            throw UnsuccessfulRequestException()
        }
    }

    suspend fun viewCommentsToPost(postId: Long): List<Comment> {
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
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val listOfComments = mutableListOf<Comment>()
                    withContext(Default) {
                        val jsonObject = JSONObject(body)
                        val keys = jsonObject.keys()
                        val commentsList = jsonObject["comments"]
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
                    }
                    return listOfComments
                }
            } else if (response.code == MainData.ERR_403) {
                throw UserNotLoginedException()
            }
            throw UnsuccessfulRequestException()
        } catch (e: SocketTimeoutException) {
            throw UnsuccessfulRequestException()
        }
    }

}