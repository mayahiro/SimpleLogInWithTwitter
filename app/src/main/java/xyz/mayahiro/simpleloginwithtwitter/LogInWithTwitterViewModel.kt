package xyz.mayahiro.simpleloginwithtwitter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LogInWithTwitterViewModel : ViewModel() {
    private val _oauthToken = MutableLiveData<String>()
    val oauthToken: LiveData<String>
        get() = _oauthToken

    private val _accessToken = MutableLiveData<TwitterOAuth.AccessTokenResponse>()
    val accessToken: LiveData<TwitterOAuth.AccessTokenResponse>
        get() = _accessToken

    private val client = OkHttpClient()

    fun getRequestToken(consumerKey: String, consumerSecret: String, callbackUrl: String, requestTokenUrl: String) {
        val request = Request.Builder()
            .url(requestTokenUrl)
            .post(ByteArray(0).toRequestBody())
            .header(
                "Authorization",
                TwitterOAuth.getRequestTokenAuthHeader(consumerKey, consumerSecret, callbackUrl)
            )
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            val response = client.newCall(request).execute().body?.string()
            response?.let {
                _oauthToken.postValue(TwitterOAuth.getOAuthToken(it))
            }
        }
    }

    fun getAccessToken(params: String) {
        val request = Request.Builder()
            .url(TwitterOAuth.getAccessTokenUrl(params))
            .post(ByteArray(0).toRequestBody())
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            val response = client.newCall(request).execute().body?.string()
            response?.let {
                _accessToken.postValue(TwitterOAuth.getAccessTokenResponse(it))
            }
        }
    }
}
