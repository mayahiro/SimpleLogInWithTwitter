package xyz.mayahiro.simpleloginwithtwitter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

typealias LogInWithTwitterOAuthParamsListener = (params: String) -> Unit

class LogInWithTwitterWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : WebView(context, attrs, defStyle) {
    private var callbackUrl: String? = null
    private var listener: LogInWithTwitterOAuthParamsListener? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (callbackUrl != null && request?.url?.toString() != null) {
                    val url = request.url.toString()
                    if (url.startsWith(callbackUrl!!)) {
                        listener?.invoke(url.removePrefix(callbackUrl!!))
                        visibility = View.INVISIBLE
                        return true
                    }
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    fun setCallbackUrl(callbackUrl: String) {
        this.callbackUrl = callbackUrl
    }

    fun setLogInWithTwitterOAuthParamsListener(listener: LogInWithTwitterOAuthParamsListener) {
        this.listener = listener
    }
}
