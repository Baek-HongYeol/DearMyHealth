package com.dearmyhealth.activities

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dearmyhealth.activities.ui.DialogListAdapter
import com.dearmyhealth.activities.ui.DialogListItem
import com.dearmyhealth.databinding.FragmentDietBinding
import com.dearmyhealth.modules.Diet.DietViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DietFragment : Fragment() {

    companion object {
        fun newInstance() = DietFragment()
    }
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: DietViewModel
    private lateinit var binding : FragmentDietBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "DietFragment Created!")
        viewModel = ViewModelProvider(this,
                DietViewModel.Factory(this.requireContext())
            )[DietViewModel::class.java]
        binding = FragmentDietBinding.inflate(layoutInflater)

        binding.foodSearchButton.setOnClickListener {
            val edtx = EditText(requireContext())
            AlertDialog.Builder(requireContext())
                .setTitle("음식 검색")
                .setView(edtx)
                .setNegativeButton("취소"){ _, _ ->

                }
                .setPositiveButton("검색") {_, _ ->
                    searchFood(edtx.text.toString())
                }.show()
        }


        viewModel.list.observe(this.viewLifecycleOwner) { value ->
            Log.d("DietFragment", "size: ${value.size}")
        }

        return binding.root
    }

    fun observeViewModel() {

    }

    private fun searchFood(name: String, listener: OnClickListener? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            var result = ArrayList<DialogListItem>()
            result.addAll(viewModel.searchFood(name).map { v -> DialogListItem(v) })
            withContext(Dispatchers.Main) {
                val recyclerView = RecyclerView(requireContext())
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = DialogListAdapter(result, listener)
                var layoutParams = WindowManager.LayoutParams()
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                AlertDialog.Builder(requireContext())
                    .setTitle("검색 결과")
                    .setView(recyclerView)
                    .setPositiveButton("닫기") { _, _ -> }
                    .show()
            }
        }
    }

}