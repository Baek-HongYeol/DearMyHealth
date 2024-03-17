package com.dearmyhealth.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.dearmyhealth.R

class DeptFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dept, container, false)
        val bpmButton = view.findViewById<ImageButton>(R.id.bpmButton)
        bpmButton.setOnClickListener { openFragment(BpmFragment()) }
        val tempButton = view.findViewById<ImageButton>(R.id.tempButton)
        tempButton.setOnClickListener { openFragment(TempFragment()) }
        val weightButton = view.findViewById<ImageButton>(R.id.weightButton)
        weightButton.setOnClickListener { openFragment(WeightFragment()) }
        val stepButton = view.findViewById<ImageButton>(R.id.Buttonstep)
        stepButton.setOnClickListener { openFragment(StepFragment()) }
        return view
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
