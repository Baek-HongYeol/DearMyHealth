package com.dearmyhealth.api

import com.google.gson.annotations.SerializedName

// 전체 응답을 나타내는 DTO
data class DurItemResponse(
    @SerializedName("response") val response: Response
)

data class Response(
    @SerializedName("header") val header: Header,
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
    //DUR 품목정보조회
    @SerializedName("ITEM_SEQ") val itemSeq: String, //품목기준코드
    @SerializedName("ITEM_NAME") val itemName: String, //픔목명
    @SerializedName("ENTP_NAME") val entpName: String, //업체명
    @SerializedName("ITEM_PERMIT_DATE") val itemPermitDate: String,
    @SerializedName("ETC_OTC_CODE") val etcOtcCode: String,
    @SerializedName("CLASS_NO") val classNo: String,
    @SerializedName("CHART") val chart: String,
    @SerializedName("BAR_CODE") val barCode: String,
    @SerializedName("MATERIAL_NAME") val materialName: String,
    @SerializedName("EE_DOC_ID") val eeDocId: String,
    @SerializedName("UD_DOC_ID") val udDocID: String,
    @SerializedName("NB_DOC_ID") val nbDocId: String,
    @SerializedName("INSERT_FILE") val insertFile: String,
    @SerializedName("STORAGE_METHOD") val storageMethod: String,
    @SerializedName("VALID_TERM") val validTerm: String,
    @SerializedName("REEXAM_TARGET") val reexamTarget: String,
    @SerializedName("REEXAM_DATE") val reexamDate: String,
    @SerializedName("PACK_UNIT") val packUnit: String,
    @SerializedName("EDI_CODE") val ediCode: String,
    @SerializedName("CANCEL_DATE") val cancelDate: String,
    @SerializedName("CANCEL_NAME") val cancelName: String,
    @SerializedName("TYPE_CODE") val typeCode: String,
    @SerializedName("TYPE_NAME") val typeName: String,
    @SerializedName("CHANGE_DATE") val changeDate: String,
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
