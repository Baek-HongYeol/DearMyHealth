package com.dearmyhealth.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetAccountDialogFragment : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentBottomSheetDialogBinding.inflate(layoutInflater, container, false)
        val defaultView = layoutInflater.inflate(R.layout.view_account, null, false)
        binding.bottomSheetLL.addView(defaultView)
        return binding.root
    }
}