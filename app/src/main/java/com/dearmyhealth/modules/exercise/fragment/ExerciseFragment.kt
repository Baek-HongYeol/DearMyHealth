package com.dearmyhealth.modules.exercise.fragment

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
import com.dearmyhealth.databinding.FragmentExerciseBinding
import com.dearmyhealth.modules.exercise.viewmodel.ExerciseSessionViewModel
import com.dearmyhealth.modules.healthconnect.HealthConnectAvailability
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseFragment: Fragment() {
    val TAG = javaClass.simpleName

    lateinit var binding: FragmentExerciseBinding
    lateinit var viewModel: ExerciseSessionViewModel

    private var availability: Boolean? = null
    private val requestPermissionsLauncher by lazy {
        registerForActivityResult(
            viewModel.permissionsLauncher
        ) { granted ->
            if (granted.containsAll(viewModel.permissions)) {
                Log.d(TAG, "2 ALL PERMISSIONS GRANTED")
                // Permissions successfully granted
            } else {
                // Lack of required permissions
                CoroutineScope(Dispatchers.IO).launch {
                    if(viewModel.checkPermission())
                        Log.d(TAG, "2 ALL PERMISSIONS GRANTED")
                    else {
                        Log.e(TAG, "2 LACKING PERMISSIONS")
                        withContext(Dispatchers.Main) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExerciseBinding.inflate(inflater)
        viewModel = ViewModelProvider(this,
            ExerciseSessionViewModel.Factory(requireActivity().application)
        )[ExerciseSessionViewModel::class.java]

        availability = checkAvailibility()

        // HealthConnectManager 설정
        requestPermissionsLauncher.run {  }

        if(availability!!) {
            CoroutineScope(Dispatchers.IO).launch {
                if(!viewModel.checkPermission()) {
                    Log.d(TAG, "requestPermissionLauncher")
                    requestPermissionsLauncher.launch(viewModel.permissions)
                }
            }
        }

        val fragment1 = ExerciseDetailFragment()
        val fragment2 = ExerciseStatsFragment()

        childFragmentManager.beginTransaction()
            .replace(R.id.exercise_container, fragment1).commit()
        val tabs: TabLayout = binding.exerciseTabs
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position

                var selected: Fragment? = null
                if (position==0) selected = fragment1
                else if (position==1) selected = fragment2

                if (selected != null) {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.exercise_container, selected).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }



    /***** permission ******/

    fun checkAvailibility(): Boolean {
        val availability = viewModel.availability.value
        when (availability) {
            HealthConnectAvailability.NOT_INSTALLED -> {
                AlertDialog.Builder(context)
                    .setMessage("헬스 커넥트가 설치되어 있지 않습니다.\n설치하기 위해 스토어로 가시겠습니까?")
                    .setNegativeButton(R.string.cancel) { _, _, -> findNavController().popBackStack() }
                    .setPositiveButton("이동") { _, _ ->
                        // Optionally redirect to package installer to find a provider, for example:
                        requireContext().startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setPackage("com.android.vending")
                                data = Uri.parse("market://details")
                                    .buildUpon()
                                    .appendQueryParameter(
                                        "id",
                                        getString(R.string.health_connect_package)
                                    )
                                    .appendQueryParameter("url", "healthconnect://onboarding")
                                    .build()
                                putExtra("overlay", true)
                                putExtra("callerId", requireContext().packageName)
                            }
                        )
                        return@setPositiveButton
                    }.show()
                return false
            }

            HealthConnectAvailability.NOT_SUPPORTED -> {
                AlertDialog.Builder(requireContext())
                    .setMessage("헬스 커넥트가 지원되지 않는 기기입니다.")
                    .show()
                findNavController().popBackStack()
                return false
            }

            HealthConnectAvailability.INSTALLED -> return true
            null -> return false
        }
    }

}