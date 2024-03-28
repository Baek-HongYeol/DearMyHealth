package com.dearmyhealth.modules.bpm

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDeptBinding
import com.dearmyhealth.modules.healthconnect.HealthConnectAvailability
import com.dearmyhealth.modules.healthconnect.HealthConnectManager

class DeptFragment : Fragment() {
    lateinit var binding: FragmentDeptBinding
    lateinit var healthConnectManager: HealthConnectManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeptBinding.inflate(inflater)
        val view = binding.root
        val bpmButton = binding.bpmButton
        bpmButton.setOnClickListener { openFragment(BpmFragment()) }
        val tempButton = binding.tempButton
        tempButton.setOnClickListener { openFragment(TempFragment()) }
        val weightButton = binding.weightButton
        weightButton.setOnClickListener { openFragment(WeightFragment()) }
        val stepButton = binding.Buttonstep
        stepButton.setOnClickListener { openFragment(StepFragment()) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        healthConnectManager = HealthConnectManager(requireContext().applicationContext)
        healthConnectManager.checkAvailability()

        checkAvailability()
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
            else -> return
        }
    }

    private fun openFragment(fragment: Fragment) {
        // 프래그먼트 트랜잭션을 시작
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // 대상 프래그먼트를 추가
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)

        // 트랜잭션을 커밋하여 변경 사항 확정
        fragmentTransaction.commit()
    }
}
