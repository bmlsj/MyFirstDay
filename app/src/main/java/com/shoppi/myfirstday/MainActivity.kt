package com.shoppi.myfirstday

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.shoppi.myfirstday.databinding.ActivityMainBinding
import com.shoppi.myfirstday.models.MainRetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO("서버 데이터 받는 함수")
        // 서버 데이터가 있으면 -> 표현

        // 서버 통신
        retrofitBuilder()
        retrofitBuilder2()

        // 진행 완료 fragment 구현
        val fragmentCompletedEvent = FragmentCompletedEvent()

        // 진행 완료 탭
        binding.btnEventCompleted.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main_zone, fragmentCompletedEvent)
                .addToBackStack(null)
                .commit()
        }

        // 진행 예정 main zone에서 이벤트 추가
        binding.btnFragmentPlus.setOnClickListener {
            CustomDialogFragment()
                .show(supportFragmentManager, "CustomDialog")
        }

        // 이벤트 추가 버튼
        binding.btnPlus.setOnClickListener {
            CustomDialogFragment()
                .show(supportFragmentManager, "customDialog")
        }

        // TODO("툴바에 메뉴 버튼 생성")
        // binding.layoutMainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        binding.layoutMainDrawer.open()

    }

    private fun retrofitBuilder() {

        // 진행 완료 데이터 1개
        val res = MainRetrofitBuilder()
        val call = res.dataApi.getDoneEvents()

        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    Log.d("RESPONSE_MAIN: ", response.body().toString())

                    /*
                response.body()?.let {
                    it.forEachIndexed { index ->
                        Log.d("DATA","[$index] date = $invitationEvents")
                    }
                } */
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // 통신에 실패한 경우
                t.localizedMessage?.let { Log.d("CONNECTION FAILURE MAIN: ", it) }
            }
        })
    }

    private fun retrofitBuilder2() {

        // 진행 예정 데이터 3개
        val res = MainRetrofitBuilder()
        val call = res.dataApi.getProgressingEvents()

        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    Log.d("RESPONSE_MAIN: ", response.body().toString())

                    /*
                response.body()?.let {
                    it.forEachIndexed { index ->
                        Log.d("DATA","[$index] date = $invitationEvents")
                    }
                } */
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // 통신에 실패한 경우
                t.localizedMessage?.let { Log.d("CONNECTION FAILURE MAIN: ", it) }
            }
        })
    }


}