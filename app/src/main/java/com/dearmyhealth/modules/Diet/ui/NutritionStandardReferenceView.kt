package com.dearmyhealth.modules.Diet.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.dearmyhealth.R
import com.dearmyhealth.databinding.ViewNutritionStandardReferenceBinding
import com.dearmyhealth.databinding.ViewSingleImageBinding

class NutritionStandardReferenceView(@get:JvmName("getAdapterContext") var context: Context): ConstraintLayout(context) {
    val binding = ViewNutritionStandardReferenceBinding.inflate(LayoutInflater.from(context), this, true)
    val pager = binding.viewPager
    val indicator = binding.pageIndicator

    init {
        pager.adapter = VPagerAdapter(context, listOf(
            R.drawable.energy_proper_ratio,
            R.drawable.carbohydrate_standard,
            R.drawable.protein_standard
        ))
        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        pager.offscreenPageLimit = 3

        // indicator 설정
        indicator.count = 3         // 전체 3
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                indicator.selection = position
            }
        })

    }


}

class VPagerAdapter(val context:Context, @DrawableRes private var images: List<Int>) : RecyclerView.Adapter<VPagerAdapter.ImageViewHolder>(){
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView: ConstraintLayout = LayoutInflater.from(parent.context).inflate(R.layout.view_single_image, parent, false) as ConstraintLayout
        return ImageViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (images.isNotEmpty()) images.size else 1
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        images[position].let{
            //holder.imageView.setImageResource(it)
            Glide.with(context)
                .load(it)
                .fitCenter()
                .into(holder.imageView)
        }
    }


}