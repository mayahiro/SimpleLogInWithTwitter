package xyz.mayahiro.simpleloginwithtwitter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import xyz.mayahiro.simpleloginwithtwitter.databinding.ActivityLogInWithTwitterBinding

class LogInWithTwitterActivity : AppCompatActivity() {
    companion object {
        private const val KEY_EXTRA_CONSUMER_KEY = "key_extra_consumer_key"
        private const val KEY_EXTRA_CONSUMER_SECRET = "key_extra_consumer_secret"
        private const val KEY_EXTRA_CALLBACK_URL = "key_extra_callback_url"

        const val KEY_RESULT_EXTRA_OAUTH_TOKEN = "key_result_extra_oauth_token"
        const val KEY_RESULT_EXTRA_OAUTH_TOKEN_SECRET = "key_result_extra_oauth_token_secret"
        const val KEY_RESULT_EXTRA_USER_ID = "key_result_extra_user_id"
        const val KEY_RESULT_EXTRA_SCREEN_NAME = "key_result_extra_screen_name"

        fun createIntent(context: Context, consumerKey: String, consumerSecret: String, callbackUrl: String) =
            Intent(context, LogInWithTwitterActivity::class.java).also {
                it.putExtra(KEY_EXTRA_CONSUMER_KEY, consumerKey)
                it.putExtra(KEY_EXTRA_CONSUMER_SECRET, consumerSecret)
                it.putExtra(KEY_EXTRA_CALLBACK_URL, callbackUrl)
            }
    }

    private val consumerKey: String by lazy {
        intent.getStringExtra(KEY_EXTRA_CONSUMER_KEY) ?: ""
    }

    private val consumerSecret: String by lazy {
        intent.getStringExtra(KEY_EXTRA_CONSUMER_SECRET) ?: ""
    }

    private val callbackUrl: String by lazy {
        intent.getStringExtra(KEY_EXTRA_CALLBACK_URL) ?: ""
    }

    private val viewModel: LogInWithTwitterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLogInWithTwitterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.let {
            it.setCallbackUrl(callbackUrl)
            it.setLogInWithTwitterOAuthParamsListener {
                viewModel.getAccessToken(it)
            }
        }

        viewModel.oauthToken.observe(this, Observer {
            binding.webView.loadUrl(TwitterOAuth.getAuthorizeUrl(it))
        })

        viewModel.accessToken.observe(this, Observer { response ->
            val intent = Intent().also {
                it.putExtra(KEY_RESULT_EXTRA_OAUTH_TOKEN, response.oauthToken)
                it.putExtra(KEY_RESULT_EXTRA_OAUTH_TOKEN_SECRET, response.oauthTokenSecret)
                it.putExtra(KEY_RESULT_EXTRA_USER_ID, response.userId)
                it.putExtra(KEY_RESULT_EXTRA_SCREEN_NAME, response.screenName)
            }
            setResult(RESULT_OK, intent)
            finish()
        })

        viewModel.getRequestToken(consumerKey, consumerSecret, callbackUrl, TwitterOAuth.getRequestTokenUrl())
    }
}
