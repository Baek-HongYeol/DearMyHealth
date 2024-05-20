package com.dearmyhealth.modules.bpm

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDeptBinding
import com.dearmyhealth.modules.healthconnect.HealthConnectAvailability
import com.dearmyhealth.modules.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class DeptFragment : Fragment() {
    val TAG = javaClass.simpleName
    lateinit var binding: FragmentDeptBinding
    lateinit var healthConnectManager: HealthConnectManager
    lateinit var viewModel: VitalViewModel

    private val bpmFragment by lazy { BpmFragment() }
    private val tempFragment by lazy { TempFragment() }
    private val weightFragment by lazy { WeightFragment() }
    private val stepFragment by lazy { StepFragment() }

    private val requestPermissionsLauncher by lazy {
        registerForActivityResult(
            healthConnectManager.requestPermissionsActivityContract()
        ) { granted ->
            if (granted.containsAll(viewModel.permissions)) {
                Log.d(TAG, "1 ALL PERMISSIONS GRANTED")
                // Permissions successfully granted
                CoroutineScope(Dispatchers.Default).launch {
                    viewModel.preloadSteps(requireActivity().applicationContext)
                }
            } else {
                // Lack of required permissions
                Log.e(TAG, "1 LACKING PERMISSIONS")
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeptBinding.inflate(inflater)
        val view = binding.root

        healthConnectManager = HealthConnectManager(requireActivity().applicationContext)
        healthConnectManager.checkAvailability()


        // viewModel 설정
        viewModel = ViewModelProvider(this,
            VitalViewModelFactory(healthConnectManager)
        )[VitalViewModel::class.java]

        checkAvailability()

        // 첫화면 설정
        openFragment(bpmFragment)

        val bpmButton = binding.bpmButton
        bpmButton.setOnClickListener { openFragment(bpmFragment) }
        val tempButton = binding.tempButton
        tempButton.setOnClickListener { openFragment(tempFragment) }
        val weightButton = binding.weightButton
        weightButton.setOnClickListener { openFragment(weightFragment) }
        val stepButton = binding.Buttonstep
        stepButton.setOnClickListener { openFragment(stepFragment) }

        // HealthConnectManager 설정
        requestPermissionsLauncher.run {  }

        return view
    }


    private fun checkAvailability() {
        when (healthConnectManager.availability.value) {
            HealthConnectAvailability.NOT_INSTALLED ->
                AlertDialog.Builder(requireContext())
                    .setMessage("헬스 커넥트가 설치되어 있지 않습니다.\n설치하기 위해 스토어로 가시겠습니까?")
                    .setNegativeButton(R.string.cancel) { _, _, -> findNavController().popBackStack() }
                    .setPositiveButton("이동") { _, _ ->
                        // Optionally redirect to package installer to find a provider, for example:
                        requireContext().startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setPackage("com.android.vending")
                                data = Uri.parse("market://details")
                                    .buildUpon()
                                    .appendQueryParameter("id", getString(R.string.health_connect_package))
                                    .appendQueryParameter("url", "healthconnect://onboarding")
                                    .build()
                                putExtra("overlay", true)
                                putExtra("callerId", requireContext().packageName)
                            }
                        )
                        return@setPositiveButton
                    }.show()
            HealthConnectAvailability.NOT_SUPPORTED -> {
                AlertDialog.Builder(requireContext())
                    .setMessage("헬스 커넥트가 지원되지 않는 기기입니다.")
                    .show()
                findNavController().popBackStack()
            }
            else -> {
                CoroutineScope(Dispatchers.Default).launch { viewModel.checkPermission() }

                Log.d(TAG, "checkPermission: ${viewModel.permitted.value}")
                if(!(viewModel.permitted.value!!)) {
                    Log.d(TAG, "requestPermissionLauncher")
                    requestPermissionsLauncher.launch(viewModel.permissions)
                }
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        // 프래그먼트 트랜잭션을 시작
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // 대상 프래그먼트를 추가
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.setReorderingAllowed(true)

        // 트랜잭션을 커밋하여 변경 사항 확정
        fragmentTransaction.commit()
    }


    private fun fetchDataForDay(date: Instant, type: VitalType) {
        try {
            when(type) {
                VitalType.BPM -> {// 선택한 날짜의 심박수 데이터를 가져오는 비동기 작업
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.tryWithPermissionsCheck {
                            viewModel.readHeartRateRecordsForDay(date)
                        }
                    }
                }
                VitalType.STEPS -> {
                    val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
                    val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.tryWithPermissionsCheck {
                            viewModel.aggregateStepsIntoSlices(startOfDay, endOfDay, Duration.ofHours(1))
                        }
                    }
                }
                VitalType.TEMPERATURE -> TODO()
                VitalType.WEIGHT -> TODO()
            }

        }
        catch (e: Exception) {
            // 예외 처리
            // 데이터를 가져오는 동안 오류가 발생한 경우 처리할 내용을 여기에 추가하세요.
            Log.d(TAG, "WTF in fetchDataToday")

        }
        finally {
            // 그래프를 초기화합니다.

            // No data 표시를 합니다.

        }
    }


}
