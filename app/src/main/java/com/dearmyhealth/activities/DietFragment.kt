package com.dearmyhealth.activities

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dearmyhealth.R
import com.dearmyhealth.modules.Diet.DietViewModel

class DietFragment : Fragment() {

    companion object {
        fun newInstance() = DietFragment()
    }
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: DietViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "DietFragment Created!")
        return inflater.inflate(R.layout.fragment_diet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[DietViewModel::class.java]
        // TODO: Use the ViewModel
    }

}