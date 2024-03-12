package com.dearmyhealth.modules.Dosage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDosageMainBinding

class DosageMainFragment: Fragment() {

    private lateinit var binding:FragmentDosageMainBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDosageMainBinding.inflate(layoutInflater)
        binding.backSq2.setOnClickListener{
            findNavController().navigate(R.id.action_dosagescreen_to_dosagesched)
        }
        return binding.root
    }
}