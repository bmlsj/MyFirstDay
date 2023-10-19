package com.shoppi.myfirstday

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.shoppi.myfirstday.databinding.LayoutDialogfragmentBinding
import com.shoppi.myfirstday.models.Event
import com.shoppi.myfirstday.models.LoginRetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate


class CustomDialogFragment : DialogFragment() {

    private var _binding: LayoutDialogfragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutDialogfragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        // 스피너 기능
        // 1. 이벤트 타입
        spinnerEvent()

        // 2. 모임 시간
        spinAmpm()
        spinHour()
        spinMinute()

        // 피커 최소 최대 지정
        setMinMaxPicker()

        // 확인 버튼
        binding.btnOk.setOnClickListener {

            val entType = binding.spinnerEvent.selectedItem.toString()
            // TODO("호스트가 둘 이상일 경우가 있음 => json 형식으로 전송")
            // 호스트가 둘 이상으로 받는 이벤트를 생성하는 다이어로그 다시 구현 => for문으로 배열이 1이상일 경우
            // 일단 하드코딩으로 구현
            val hostName = "{\"host\" : \"" + binding.editHostName.text.toString() + "\"}"
            val location = binding.editLocation.text.toString()

            val npYearVal = binding.npYear.value
            val npMonthVal = binding.npMonth.value
            val npDayVal = binding.npDay.value

            val tpAmPm = binding.spinnerTimeAmpm.selectedItem.toString()
            val tpHour = binding.spinnerTimeHour.selectedItem.toString()
            val tpMinute = binding.spinnerTimeMinute.selectedItem.toString()

            val npEventDate = if (npMonthVal < 10 && npDayVal >= 10) {  // 1-9월이면서 10-31일 경우
                if (tpAmPm == "AM") {
                    "${npYearVal}-0${npMonthVal}-${npDayVal}T${tpHour}:${tpMinute}:00"
                } else {
                    "${npYearVal}-0${npMonthVal}-${npDayVal}T${tpHour.toInt() + 12}:${tpMinute}:00"
                }
            } else if (npMonthVal < 10) {  // 10월 이전일 때: 1 - 9월
                if (tpAmPm == "AM") {
                    "${npYearVal}-0${npMonthVal}-0${npDayVal}T${tpHour}:${tpMinute}:00"
                } else if (tpAmPm == "PM" && tpHour != "12"){  // 오후이면서 12시가 아닐때면 +12시간 추가
                    "${npYearVal}-0${npMonthVal}-0${npDayVal}T${tpHour.toInt() + 12}:${tpMinute}:00"
                } else {   // 오후이면서 12시면 그대로 입력
                    "${npYearVal}-0${npMonthVal}-0${npDayVal}T${tpHour}:${tpMinute}:00"
                }
            } else {  // 10월 이상일 때: 10 - 12월
                if (tpAmPm == "AM") {
                    "${npYearVal}-${npMonthVal}-${npDayVal}T${tpHour}:${tpMinute}:00"
                } else if(tpAmPm == "PM" && tpHour != "12"){
                    "${npYearVal}-${npMonthVal}-${npDayVal}T${tpHour.toInt() + 12}:${tpMinute}:00"
                } else {
                    "${npYearVal}-${npMonthVal}-${npDayVal}T${tpHour}:${tpMinute}:00"
                }
            }

            // Log.d("보낼 데이터 값", "$npEventDate, $entType, $hostName, $location")

            // 데이터를 서버에 전송
            val event = Event()
            event.type = entType
            event.host = hostName
            event.datetime = npEventDate
            event.location = location

            eventServer(event)
            // Log.i(TAG,"서버 전송 값: ${event.type}, ${event.host}, ${event.datetime}, ${event.location}")

            // 화면 전환
            dismiss()
        }

        // 취소 버튼
        binding.btnCancell.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun eventServer(event: Event) {

        val res = LoginRetrofitBuilder()
        val call = res.loginApi.getCreateEvent(event)

        call.enqueue(object : Callback<String> {

            // 통신 성공
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.isSuccessful) {
                    Log.d("RESPONSE: ", response.body().toString())
                    Log.i(
                        TAG,
                        "서버 전송 성공: ${event.type}, ${event.host}, ${event.datetime}, ${event.location}"
                    )

                } else {
                    Log.d("RESPONSE: ", "실패")
                }
            }

            // 통신 실패
            override fun onFailure(call: Call<String>, t: Throwable) {
                t.localizedMessage?.let {
                    Log.d("Connection Failure: ", it)
                }
            }
        })
    }

    // numberpicker 최솟값, 최댓값
    private fun setMinMaxPicker() {
        binding.npYear.let {
            it.minValue = LocalDate.now().year
            it.maxValue = 2100
            it.wrapSelectorWheel = true
        }

        binding.npMonth.let {
            it.minValue = 1
            it.maxValue = 12
            it.wrapSelectorWheel = true
        }

        binding.npDay.let {
            it.minValue = 1
            it.maxValue = 31
            it.wrapSelectorWheel = true
        }
    }
    
    // 시간 스피너
    private fun spinAmpm() {

        val ampm = resources.getStringArray(R.array.spinner_time)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, ampm)
        binding.spinnerTimeAmpm.adapter = adapter

    }

    private fun spinHour() {

        val spinner = binding.spinnerTimeHour

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_hours,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun spinMinute() {

        val minuteData = arrayOfNulls<String>(60)
        for (i in 0..59) {
            if (i < 10) {
                minuteData[i] = "0$i"
            } else {
                minuteData[i] = "$i"
            }
        }

        val spinner = binding.spinnerTimeMinute
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            minuteData
        )
        spinner.adapter = adapter

    }

    private fun spinnerEvent() {

        val spinner = binding.spinnerEvent

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.event_spinner_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /* dialog 사이즈 조절
    fun Context.dialogResize(dialog: CustomDialogFragment, width: Float, height: Float) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT < 30) {
            val display = display
            val size = Point()

            if (display != null) {
            }

            val window = dialog.window

            val x = (size.x * width).toInt()
            val y = (size.y * height).toInt()

            window?.setLayout(x, y)

        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            val window = dialog.window
            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()

            window?.setLayout(x, y)
        }

    }
    
    
    override fun onResume() {
        super.onResume()
        context?.dialogResize(this@CustomDialogFragment, 0.9f, 0.9f)
    } */
}