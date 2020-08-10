package xyz.mayahiro.simpleloginwithtwitter

import okio.ByteString.Companion.toByteString
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.TreeMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

object TwitterOAuth {
    private const val REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token"
    private const val AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize"
    private const val ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token"

    private const val OAUTH_PARAM_CALLBACK = "oauth_callback"
    private const val OAUTH_PARAM_CONSUMER_KEY = "oauth_consumer_key"
    private const val OAUTH_PARAM_NONCE = "oauth_nonce"
    private const val OAUTH_PARAM_SIGNATURE_METHOD = "oauth_signature_method"
    private const val OAUTH_PARAM_SIGNATURE = "oauth_signature"
    private const val OAUTH_PARAM_TIMESTAMP = "oauth_timestamp"
    private const val OAUTH_PARAM_VERSION = "oauth_version"

    private const val OAUTH_SIGNATURE_METHOD = "HMAC-SHA1"
    private const val OAUTH_VERSION = "1.0"

    private val RAND = SecureRandom()

    fun getRequestTokenAuthHeader(consumerKey: String, consumerSecret: String, callbackUrl: String): String {
        val params: TreeMap<String, String> = TreeMap()

        params[OAUTH_PARAM_CALLBACK] = percentEncode(callbackUrl)
        params[OAUTH_PARAM_CONSUMER_KEY] = percentEncode(consumerKey)
        params[OAUTH_PARAM_NONCE] = getNonce()
        params[OAUTH_PARAM_SIGNATURE_METHOD] = OAUTH_SIGNATURE_METHOD
        params[OAUTH_PARAM_TIMESTAMP] = getTimestamp()
        params[OAUTH_PARAM_VERSION] = OAUTH_VERSION

        val signatureBase = constructSignatureBase(params)

        params[OAUTH_PARAM_SIGNATURE] = percentEncode(calculateSignature(signatureBase, consumerSecret))

        return params.map { "${it.key}=\"${it.value}\"" }.joinToString(",", "OAuth ")
    }

    fun getRequestTokenUrl(): String = REQUEST_TOKEN_URL

    fun getAuthorizeUrl(oauthToken: String): String = "${AUTHORIZE_URL}?oauth_token=${oauthToken}"

    fun getAccessTokenUrl(params: String): String = ACCESS_TOKEN_URL + params

    fun getOAuthToken(params: String): String {
        val splitParams = params.split("&")
        var token = ""

        splitParams.forEach {
            val splitParam = it.split("=")
            if (splitParam[0] == "oauth_token") {
                token = splitParam[1]
            }
        }

        return token
    }

    fun getAccessTokenResponse(response: String): AccessTokenResponse {
        var oauthToken = ""
        var oauthTokenSecret = ""
        var userId = ""
        var screenName = ""

        response.split("&").forEach {
            val splitParam = it.split("=")
            if (splitParam.size >= 2) {
                when (splitParam[0]) {
                    "oauth_token" -> oauthToken = splitParam[1]
                    "oauth_token_secret" -> oauthTokenSecret = splitParam[1]
                    "user_id" -> userId = splitParam[1]
                    "screen_name" -> screenName = splitParam[1]
                    else -> {
                        // none
                    }
                }
            }
        }

        return AccessTokenResponse(oauthToken, oauthTokenSecret, userId, screenName)
    }

    private fun getTimestamp() = (System.currentTimeMillis() / 1000).toString()
    private fun getNonce() = System.nanoTime().toString() + abs(RAND.nextLong())

    private fun constructSignatureBase(params: TreeMap<String, String>): String = "POST&${percentEncode(REQUEST_TOKEN_URL)}&${getEncodedQueryParams(params)}"

    private fun calculateSignature(signatureBase: String, consumerSecret: String, tokenSecret: String? = null): String {
        return try {
            val signatureBaseBytes = signatureBase.toByteArray(Charsets.UTF_8)
            val encodedTokenSecret = if (tokenSecret.isNullOrEmpty()) "" else percentEncode(tokenSecret)
            val keyBytes = "${percentEncode(consumerSecret)}&${encodedTokenSecret}".toByteArray(Charsets.UTF_8)

            val secretKey = SecretKeySpec(keyBytes, "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(secretKey)

            val signatureBytes = mac.doFinal(signatureBaseBytes)

            signatureBytes.toByteString(0, signatureBytes.size).base64()
        } catch (e: Exception) {
            ""
        }
    }

    private fun percentEncode(s: String): String {
        val sb = StringBuilder()
        val encoded = URLEncoder.encode(s, "UTF-8")
        val encodedLength = encoded.length

        var i = 0
        while (i < encodedLength) {
            when (val c = encoded[i]) {
                '*' -> sb.append("%2A")
                '+' -> sb.append("%20")
                else -> {
                    if (
                        c == '%' &&
                        (i + 2) < encodedLength &&
                        encoded[i + 1] == '7' &&
                        encoded[i + 2] == 'E'
                    ) {
                        sb.append('~')
                        i++
                    } else {
                        sb.append(c)
                    }
                }
            }
            i++
        }

        return sb.toString()
    }

    private fun getEncodedQueryParams(params: TreeMap<String, String>): String {
        val paramsBuf = StringBuilder()
        val numParams = params.size

        params.entries.forEachIndexed { index, entry ->
            paramsBuf.append(entry.key)
                .append("%3D")
                .append(percentEncode(entry.value))

            if (index < (numParams - 1)) {
                paramsBuf.append("%26")
            }
        }

        return paramsBuf.toString()
    }

    data class AccessTokenResponse(
        val oauthToken: String,
        val oauthTokenSecret: String,
        val userId: String,
        val screenName: String
    )
}
