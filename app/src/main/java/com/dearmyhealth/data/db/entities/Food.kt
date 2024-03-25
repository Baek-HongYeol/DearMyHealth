package com.dearmyhealth.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dearmyhealth.modules.Diet.model.Nutrients

@Entity(tableName = "food")
data class Food(
    val NO : Int,
    val SAMPLE_ID : String,
    @PrimaryKey val code : String,
    val DB_Group : String,
    val 상용제품 : String,
    val 식품명 : String,
    val 연도 : Int,
    @ColumnInfo(name = "제조사/유통사") val 제조사 : String,
    val 식품대분류 : String,
    val 식품상세분류 : String,
    @ColumnInfo(name = "1회제공량") val onetime제공량 : Int,
    val 내용량_단위 : String?,
    val 총내용량g : String,
    val 총내용량mL : String,
    val 에너지kcal : Int,
    val 수분g : String,
    val 단백질g : String,
    val 지방g : String,
    val 탄수화물g : String,
    val 총당류g : Int,
    val 포도당g : String,
    val 과당g : String,
    val 당알콜g : String,
    val 에리스리톨g : String,
    val 총식이섬유g : String,
    val 칼슘mg : String,
    val 철mg : String,
    val 마그네슘mg : String,
    val 인mg : String,
    val 칼륨mg : String,
    val 나트륨mg : Int,
    val 아연mg : String,
    val 구리mg : String,
    val 구리microg : String,
    val 망간mg : String,
    val 망간microg : String,
    val 셀레늄microg : String,
    val 요오드microg : String,
    val 염소microg : String,
    val 비타민A_microRE : String,
    val 베타카로틴microg : String,
    val 비타민Dmicrog : String,
    val 비타민D3microg : String,
    val 비타민D1microg : String,
    val 비타민Emicrog : String,
    val 비타민Emicroga_TE : String,
    val 비타민Kmg : String,
    val 비타민Kmicrog : String,
    val 비타민K1microg : String,
    val 비타민K2microg : String,
    val 비타민B1mg : String,
    val 비타민B1microg : String,
    val 비타민B2mg : String,
    val 비타민B2microg : String,
    val 나이아신mgNE : String,
    val 판토텐산mg : String,
    val 판토텐산microg : String,
    val 비타민B6mg : String,
    val 비타민B6microg : String,
    val 비오틴microg : String,
    val 엽산DFEmicrog : String,
    val 비타민B12mg : String,
    val 비타민B12microg : String,
    val 비타민Cg : String,
    val 비타민Cmg : String,
    val 콜린mg : String,
    val 류신mg : String,
    val 트립토판mg : String,
    val 히스티딘mg : String,
    val 아르기닌mg : String,
    val 시스테인mg : String,
    val 프롤린mg : String,
    val 타우린mg : String,
    val 콜레스테롤g : String,
    val 콜레스테롤mg : String,
    val 총포화지방산g : String,
    val 리놀레산g : String,
    val 알파리놀렌산mg : String,
    val 감마리놀렌산mg : String,
    val 아라키돈산mg : String,
    val 에이코사펜타에노산mg : String,
    val 도코사헥사에노산mg : String,
    val EPA와DHA의합mg : String,
    val 오메가3지방산g : String,
    val 트랜스지방산g : Double,
    val 총불포화지방산g : String,
    val 회분g : String,
    val 카페인mg : String,
    val 성분표출처 : String,
    val 발행기관 : String,
) {
    fun toNutrients(): Nutrients {
        var nuts = Nutrients(0, 0).apply {
            calories = 에너지kcal.toDouble()
            nutrients = mutableMapOf()
            if( 탄수화물g != "-")
                nutrients[Nutrients.Names.carbohydrate] = 탄수화물g.toDouble()
            if( 단백질g != "-")
                nutrients[Nutrients.Names.protein] = 단백질g.toDouble()
            if( 지방g != "-")
                nutrients[Nutrients.Names.fat] = 지방g.toDouble()

        }

        return nuts
    }
    companion object {
        fun convertNutStringToDouble(prev: String): Double? {
            if (prev == "-") return null
            return prev.toDoubleOrNull()
        }
    }
}