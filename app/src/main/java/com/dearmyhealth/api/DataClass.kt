package com.dearmyhealth.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

// 전체 응답을 나타내는 DTO
data class DurItem(
    @Expose
    @SerializedName("response") val response: Response
)

data class Response(
    @Expose
    @SerializedName("header") val header: Header,
    @Expose
    @SerializedName("body") val body: Body
)

data class Header(
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg") val resultMsg: String
)

data class Body(
    @SerializedName("items") val items: List<Item>,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("totalCount") val totalCount: Int
)

// 실제 품목 정보를 담는 DTO
data class Item(
    //
    @SerializedName("FORM_CODE") val fromCode: String,
    @SerializedName("FORM_NAME") val fromName: String,
    @SerializedName("MIXTURE_DUR_SEQ") val mixtureDurSeq: String,
    @SerializedName("MIXTURE_MIX") val mixtureMix : String,
    @SerializedName("MIXTURE_INGR_CODE") val mixtureIngrCode: String,
    @SerializedName("MIXTURE_INGR_KOR_NAME") val IngrKorName: String,
    @SerializedName("MIXTURE_INGR_ENG_NAME") val IngrEngName: String,
    @SerializedName("MIXTURE_ITEM_SEQ") val MixtureItemSeq: String,
    @SerializedName("MIXTURE_ITEM_NAME") val mixtureItemName: String,
    @SerializedName("MIXTURE_ENTP_NAME") val mixtureEntpName: String,
    @SerializedName("MIXTURE_FORM_CODE") val mixtureFromCode: String,
    @SerializedName("MIXTURE_ETC_OTC_CODE") val mixtureEtcOtcCode: String,
    @SerializedName("MIXTURE_CLASS_CODE") val mixtureClassCode: String,
    @SerializedName("MIXTURE_FORM_NAME") val mixtureFormName: String,
    @SerializedName("MIXTURE_ETC_OTC_NAME") val mixtureEtcOtcName: String,
    @SerializedName("MIXTURE_CLASS_NAME") val mixtureClassName: String,
    @SerializedName("MIXTURE_MAIN_INGR") val mixtureMainIngr: String,
    @SerializedName("MIXTURE_ITEM_PERMIT_DATE") val mixtureItemPermit: String,
    @SerializedName("MIXTURE_CHART") val mixtureChart: String,
    @SerializedName("MIXTURE_CHANGE_DATE") val mixtureChangeDate: String,
    //DUR 품목정보조회
    @SerializedName("ITEM_SEQ") val itemSeq: String, //품목기준코드
    @SerializedName("ITEM_NAME") val itemName: String, //픔목명
    @SerializedName("ENTP_NAME") val entpName: String, //업체명
    @SerializedName("CHART") val chart: String,//성상
    @SerializedName("MATERIAL_NAME") val materialName: String,//원료성분
    @SerializedName("NB_DOC_ID") val nbDocId: String,//주의사항
    @SerializedName("STORAGE_METHOD") val storageMethod: String,//저장방법
    @SerializedName("VALID_TERM") val validTerm: String,///유효기간
    @SerializedName("TYPE_CODE") val typeCode: String,//유형코드
    @SerializedName("TYPE_NAME") val typeName: String,//DUR유형

    /*@SerializedName("ITEM_PERMIT_DATE") val itemPermitDate: String,
    @SerializedName("ETC_OTC_CODE") val etcOtcCode: String,
    @SerializedName("CLASS_NO") val classNo: String,
    @SerializedName("BAR_CODE") val barCode: String,
    @SerializedName("EE_DOC_ID") val eeDocId: String,
    @SerializedName("UD_DOC_ID") val udDocID: String,
    @SerializedName("INSERT_FILE") val insertFile: String,
    @SerializedName("REEXAM_TARGET") val reexamTarget: String,
    @SerializedName("REEXAM_DATE") val reexamDate: String,
    @SerializedName("PACK_UNIT") val packUnit: String,
    @SerializedName("EDI_CODE") val ediCode: String,
    @SerializedName("CANCEL_DATE") val cancelDate: String,
    @SerializedName("CANCEL_NAME") val cancelName: String,
    @SerializedName("CHANGE_DATE") val changeDate: String,*/

    //노인정보조회
    @SerializedName("CLASS_CODE") val classCode: String,
    @SerializedName("MIX_TYPE") val mixType: String,
    @SerializedName("INGR_CODE") val ingrCode: String,
    @SerializedName("INGR_ENG_NAME") val ingrEngName: String,
    @SerializedName("INGR_NAME") val ingrName: String,
    @SerializedName("MIX_INGR") val mixIngr: String,
    @SerializedName("FORM_NAME") val formName: String,
    @SerializedName("CLASS_NAME") val className: String,
    @SerializedName("ETC_OTC_NAME") val etcOtcName: String,
    @SerializedName("MAIN_INGR") val mainIngr: String,
    @SerializedName("NOTIFICATION_DATE") val notificationDate: String,
    @SerializedName("PROHBT_CONTENT") val prohibitContent: String,
    @SerializedName("REMARK") val remark: String,
    @SerializedName("INGR_ENG_NAME_FULL") val ingrEngNameFull: String,

    //효능군 중복 정보조회
    @SerializedName("DUR_SEQ") val durSeq: String,
    @SerializedName("EFFECT_NAME") val effectName: String,
    @SerializedName("FORM_CODE_NAME") val formCodeName: String,
    @SerializedName("MIX") val mix: String,
    @SerializedName("FORM_CODE") val formCode: String,
    @SerializedName("BIZRNO") val bizrno: String,
    @SerializedName("SERS_NAME") val sersNAme: String,
)
