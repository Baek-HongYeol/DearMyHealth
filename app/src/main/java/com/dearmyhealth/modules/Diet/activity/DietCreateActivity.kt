package com.dearmyhealth.modules.Diet.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dearmyhealth.R
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.databinding.ActivityDietCreateBinding
import com.dearmyhealth.databinding.ViewFoodSearchBinding
import com.dearmyhealth.modules.Diet.ui.DialogListAdapter
import com.dearmyhealth.modules.Diet.ui.DietDetailItemView
import com.dearmyhealth.modules.Diet.viewmodel.DietCreateViewModel
import com.dearmyhealth.modules.login.ui.afterTextChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import splitties.alertdialog.alertDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class DietCreateActivity : AppCompatActivity(){
    private val TAG = this.javaClass.simpleName

    private lateinit var binding : ActivityDietCreateBinding
    private lateinit var viewModel: DietCreateViewModel
    private lateinit var dietPreview: DietDetailItemView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDietCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼 만들기
        setSupportActionBar(binding.toolbar1)
        val toolbar = supportActionBar
        toolbar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this,
            DietCreateViewModel.Factory(application)
        )[DietCreateViewModel::class.java]

        observeViewModel()

        // 검색 팝업창 띄우기
        binding.foodSearchButton.setOnClickListener {
            showSearchDialog()
        }

        // 식단 미리보기 뷰
        dietPreview = DietDetailItemView(this, binding.dietPreview)


        // 식단 정보 수정 패널 초기화
        setEditingElements()
    }

    private fun observeViewModel() {
        viewModel.dietResult.observe(this) { res ->
            val result = res ?: return@observe
            // 로딩창 닫기
            binding.loading.visibility = View.GONE
            // 결과 안내
            if ((result.error ?: 0) > 0)
                Log.e(TAG, "dietResult failed: ${result.error}")
            else {
                Toast.makeText(this, "식단 데이터를 입력했습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 미리보기 뷰 - 식단 이름 변경 (음식 이름 혹은 커스텀)
        viewModel.targetName.observe(this) { name ->
            // diet preview의 식품 이름 부분 변경
            dietPreview.setName(name)
            if (binding.dietEditingName.text.isEmpty())
                binding.dietEditingName.setText(name)
        }

        // 식단 이름 자동 완성 (by 음식 지정)
        viewModel.targetFood.observe(this) { food ->
            if(food == null) return@observe
            if(binding.dietEditingName.text.isEmpty())
                binding.dietEditingName.setText(food.식품명)
        }

        // 미리보기 뷰 - 아침/점심/저녁 부분 반영
        viewModel.targetType.observe(this) { type ->
            dietPreview.setType(type)

            // 입력 레이아웃에도 반영
            binding.autoCompleteTextView.listSelection = Diet.MealType.values().indexOf(type)
        }

        // 미리보기 뷰 - 음식에 따른 영양소 정보 생성
        viewModel.targetNutrition.observe(this) { nuts ->
            dietPreview.generateNutrientChips(nuts)
        }

        // 미리보기 뷰 - 날짜/시간 변경
        viewModel.targetDateTime.observe(this) { cal ->
            dietPreview.setTime(cal)

            // 입력 레이아웃에도 반영
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdfDate.timeZone = TimeZone.getDefault()
            sdfTime.timeZone = sdfDate.timeZone
            val time = sdfTime.format(cal.time)
            val date = sdfDate.format(cal.time)
            binding.dietEditingTime.text = time
            binding.dietEditingDate.text = date
        }

        viewModel.targetAmount.observe(this) { amount ->
            dietPreview.generateNutrientChips(viewModel.targetNutrition.value, amount)
        }
    }

    /** 식단의 validation 검증
     *  식단 이름이 지정이 안되었다면 저장 불가. */
    private fun checkValidation(): Boolean{
        if(viewModel.targetName.value == null){
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("식단 이름을 설정하세요.")
                .show()
            return false
        }
        if(binding.dietEditingAmount.error != null){
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("섭취량을 입력하세요.")
                .show()
            return false
        }
        return true
    }


    private fun searchFood(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.searchFood(keyword)
        }
    }

    /** 음식 검색 결과 창 설정
     *
     * viewModel의 foodSearchResult를 관찰하여 결과를 recyclerView에 반영.
     * adapter에 onItemClickListener를 전달.
     * 음식 선택 시 검색 창 닫기.
     */
    private fun initSearchResultRV(binding: ViewFoodSearchBinding, alertDialog: AlertDialog) {
        val recyclerView = binding.foodSearchResultRV
        recyclerView.layoutManager = LinearLayoutManager(this)
        viewModel.foodSearchResult.observe(this) { result ->
            if(result.success != null) {
                if (result.success.isEmpty()) {
                    binding.foodSearchResultRV.visibility = View.GONE
                    binding.noFoodDataTV.visibility = View.VISIBLE
                }
                else {
                    Log.d(TAG, "result size: ${result.success.size}")
                    binding.noFoodDataTV.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = DialogListAdapter(result.success) { v, pos ->
                        viewModel.setFood(pos)
                        alertDialog.cancel()
                    }
                }
            }
        }
    }

    /** 음식 검색 Dialog 생성 */
    private fun showSearchDialog() {

        val binding = ViewFoodSearchBinding.inflate(layoutInflater)
        val edtx = binding.searchFoodEdtx

        edtx.setOnEditorActionListener { textView, i, _ ->
            when(i) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    if(textView.text.trim() == "")
                        return@setOnEditorActionListener true
                    searchFood(textView.text.toString())
                }
            }
            return@setOnEditorActionListener true
        }
        val dialog = alertDialog {
            setTitle("음식 검색")
            setView(binding.root)
            setOnDismissListener {
                viewModel.foodSearchResult.removeObservers(this@DietCreateActivity)
            }
            create()
        }
        //  recyclerView의 onItemClickListener가 dialog를 닫게 하기 위해서
        //  dialog를 먼저 생성해 접근하도록 전달.
        initSearchResultRV(binding, dialog)
        dialog.show()
    }

    /** Diet 정보 수정 패널 초기화
     *
     * Diet Type Enum의 목록을 토대로 선택 목록을 생성.
     * position을 통해 viewModel에서 타입을 지정.
     */
    @SuppressLint("SetTextI18n")
    private fun setEditingElements() {

        // 식단 타입 목록에서 선택
        val typeArray = Diet.MealType.values().map { type -> type.displayName }
        val adapter = ArrayAdapter(this, R.layout.view_single_text, typeArray)
        binding.autoCompleteTextView.setAdapter(adapter)
        binding.autoCompleteTextView.setOnItemClickListener { _, _, i, _ ->
            viewModel.setType(i)
        }

        // 이름 설정 버튼
        binding.dietEditingNameSubmit.setOnClickListener {
            val text = binding.dietEditingName.text.toString().trim()
            if(text.isBlank()) return@setOnClickListener
            viewModel.setName(text)
        }

        // 날짜 선택과 적용
        binding.dietEditingDateLL.setOnClickListener {
            val now: Calendar = Calendar.getInstance()
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                var date = "$year-"
                date += if (month <9) "0${month+1}" else "${month+1}"
                date += if (day < 10) "-0$day" else "-$day"
                binding.dietEditingDate.text = date
                viewModel.setDate(year, month, day)
            }
            DatePickerDialog(
                this@DietCreateActivity, listener,
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // 시간 선택과 적용
        binding.dietEditingTimeLL.setOnClickListener {
            val now: Calendar = Calendar.getInstance()
            val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                var time = if(hour<10) "0$hour:" else "$hour:"
                time += if(minute<10) "0$minute" else "$minute"
                binding.dietEditingTime.text = time
                viewModel.setTime(hour, minute)
            }
            TimePickerDialog(this, listener,
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true
            ).show()
        }
        binding.dietEditingAmount.afterTextChanged { text: String? ->
            try {
                val value = text?.toFloat() ?:0f
                if (value<=0)
                    binding.dietEditingAmount.error = getString(R.string.positive_value_required)
                else {
                    binding.dietEditingAmount.error = null
                    viewModel.setAmount(value)
                }
            } catch(e: NumberFormatException){
                binding.dietEditingAmount.error = getString(R.string.positive_value_required)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_diet_create, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_diet_save -> {
                if(!checkValidation()) return true
                binding.loading.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.save()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}