package com.shoppi.myfirstday

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.e
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.shoppi.myfirstday.models.GlobalApplication
import com.shoppi.myfirstday.models.LoginRetrofitBuilder
import com.shoppi.myfirstday.models.User
import okhttp3.ResponseBody
import org.apache.commons.lang3.exception.ExceptionUtils.getMessage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.AccessController.getContext

class LoginActivity : AppCompatActivity() {

    private lateinit var btnkakaologin: Button
    private lateinit var btnPass: Button
    private lateinit var btngooglelogin: Button
    private lateinit var googleSignInResultLauncher: ActivityResultLauncher<Intent>

    var userid: String = ""
    var acToken: String = ""
    var refreshToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnPass = findViewById(R.id.btn_pass)
        btnkakaologin = findViewById(R.id.btn_kakao_login)
        btngooglelogin = findViewById(R.id.btn_google_login)

        btnPass.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnkakaologin.setOnClickListener {
            Toast.makeText(this, "카카오 버튼 클릭", Toast.LENGTH_LONG).show()
            kakaoLogin()
            kakaoIsToken()
            tokenInfo()
            // kakaoLogout()
        }

        googleSignInResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

        btngooglelogin.setOnClickListener {
            val mGoogleSignInClient = googleLogin()
            val signInTent: Intent = mGoogleSignInClient.signInIntent
            googleSignInResultLauncher.launch(signInTent)
        }

    }


    // 서버 데이터 전송
    private fun Login(user: User) {

        val res = LoginRetrofitBuilder()
        val call = res.loginApi.getLoginResponse(user)

        call.enqueue(object : Callback<String> { // 비동기 방식 통신 메소드
            override fun onResponse( // 통신에 성공한 경우
                call: Call<String>,
                response: Response<String>
            ) {
                val resBody = response.body()

                if (resBody != null && response.isSuccessful) { // 응답 잘 받은 경우
                    Log.i(TAG, "$userid, $acToken, $refreshToken")

                    // 서버에서 보낸 데이터 받기
                    Log.d("RESPONSE: ", resBody.toString())
                    Log.e("Header Data : ", response.headers()["Authorization"].toString())

                    // 헤더의 인가값 -> 어떤 요청이던지 헤더에 붙여서 보내기
//                    response.headers()["Authorization"]?.let {
//                        // Log.d("RESPONSE2", it)
//                        // Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
//
//                        val sharedValue = GlobalApplication.sSharedPreferences.edit()
//                        sharedValue.putString("Authorization", it)
//                        sharedValue.apply()
//                    }


                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("RESPONSE", "FAILURE")
                    Log.d("ERROR", "$response")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // 통신에 실패한 경우
                t.localizedMessage?.let { Log.d("CONNECTION FAILURE: ", it) }
            }
        })

    }




    private fun kakaoLogin() {
        // 카카오 계정으로 로그인 공동 callback 구성
        // 카카오톡으로 로그인 할 수 없어, 카카오 계정으로 로그인할 경우 사용
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->

            if (error != null) {

                Log.e(TAG, "카카오 계정으로 로그인 실패", error)

            } else if (token != null) {
                Log.i(TAG, "카카오 계정으로 로그인 성공 ${token.accessToken}")

                // 서버에 데이터 전송
                acToken = token.accessToken
                refreshToken = token.refreshToken

                val user = User()
                user.userId = userid
                user.accessToken = acToken
                user.refreshToken = refreshToken

                Log.d(
                    "BUTTON CLICKED",
                    "id: " + user.userId + ", acToken: " + user.accessToken + ",refresh: " + user.refreshToken
                )
                Login(user)

                // 로그인 성공시, mainActivity로 intent
                startActivity(Intent(this, MainActivity::class.java))
            }

        }

        // 카카오톡이 설치된 경우, 카카오톡으로 로그인 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {

            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->

                if (error != null) {
                    Log.e(TAG, "로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도없이 로그인 취소 처리
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoAccount
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)

                } else if (token != null) {
                    Log.i(TAG, "로그인 성공 ${token.accessToken}")
                    // 로그인 성공시, mainActivity로 intent
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun kakaoIsToken() {

        // 토큰 존재 확인: 토큰을 이미 가지고 있으면 다시 로그인할 필요가 없음.
        // 따라서, 토큰 존재여부에 따라 처리가 필요하다
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        //로그인 필요
                        // kakaoLogin()

                    } else {
                        //기타 에러
                    }
                } else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                }
            }
        } else {
            //로그인 필요
            // kakaoLogin()
        }
    }

    private fun tokenInfo() {
        // 토큰 정보 보기
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {

                Log.e(TAG, "토큰 정보 보기 실패", error)

            } else if (tokenInfo != null) {
                Log.i(
                    TAG, "토큰 정보보기 성공" +
                            "\n회원번호 : ${tokenInfo.id}" +
                            "\n만료시간 : ${tokenInfo.expiresIn} 초"
                )

                userid = tokenInfo.id.toString()
            }
        }
    }

    private fun kakaoLogout() {
        // 로그 아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
            } else {
                Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }

    }

    // 구글 로그인
    private fun googleLogin(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(getString(R.string.GoogleLoginClientID))
            .requestEmail()  // 이메일 주소 요청
            //   .requestScopes(Scope("https://www.googleapis.com/auth/pubsub"))
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // 이미 로그인 되었는지 확인
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            Log.e("Google account", "로그인 안되있음")
        } else {
            Log.e("Google account", "로그인 완료된 상태")
        }

        return mGoogleSignInClient

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val googletoken = account?.idToken.toString()
            val googletokenAuth = account?.serverAuthCode.toString()

            Log.e("Google account", email)
            Log.e("Google account", googletoken)
            Log.e("Google account", googletokenAuth)

        } catch (e: ApiException) {
            Log.e("Google account", "signInResult:failed Code = " + e.statusCode)
        }
    }

}