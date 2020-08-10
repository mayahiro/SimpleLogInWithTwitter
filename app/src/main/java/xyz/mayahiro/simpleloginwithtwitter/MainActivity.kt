package xyz.mayahiro.simpleloginwithtwitter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import xyz.mayahiro.simpleloginwithtwitter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val KEY_REQUEST_CODE_LOG_IN_WITH_TWITTER = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.useWebViewButton.setOnClickListener {
            startActivityForResult(
                LogInWithTwitterActivity.createIntent(this, "__YOUR_CONSUMER_KEY__", "__YOUR_CONSUMER_SECRET__", "__YOUR_CALLBACK_URL__"),
                KEY_REQUEST_CODE_LOG_IN_WITH_TWITTER
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == KEY_REQUEST_CODE_LOG_IN_WITH_TWITTER) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    Log.d("result:oauthToken", it.getStringExtra(LogInWithTwitterActivity.KEY_RESULT_EXTRA_OAUTH_TOKEN) ?: "")
                    Log.d("result:oauthTokenSecret", it.getStringExtra(LogInWithTwitterActivity.KEY_RESULT_EXTRA_OAUTH_TOKEN_SECRET) ?: "")
                    Log.d("result:userId", it.getStringExtra(LogInWithTwitterActivity.KEY_RESULT_EXTRA_USER_ID) ?: "")
                    Log.d("result:screenName", it.getStringExtra(LogInWithTwitterActivity.KEY_RESULT_EXTRA_SCREEN_NAME) ?: "")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
