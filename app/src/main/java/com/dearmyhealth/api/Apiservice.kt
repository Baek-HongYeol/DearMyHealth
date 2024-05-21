package com.dearmyhealth.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
interface Apiservice {

    //DUR품목정보 조회
    @GET("/1471000/DURPrdlstInfoService03/getDurPrdlstInfoList03")
    fun getDurItemInfo(
        @Query("itemSeq") ItemSeq: String?,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("itemName") ItemName: String?,
        @Query("entpName") EntpName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?,
        @Query("bizrno") Bizrno: String?,
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String? //Dur 성분코드
    ): Call<DurResponse<List<Item>>>

    //병용금기
    @GET("/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03")
    fun getJointTabooInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?,
        @Query("bizrno") Bizrno: String?
    ):Call<DurResponse<List<Item>>>

    //노인주의
    @GET("/1471000/DURPrdlstInfoService03/getOdsnAtentInfoList03")
    fun getElderlyCautionInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String?,
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?
    ): Call<DurResponse<List<Item>>>

    //특정연령대금기
    @GET("/1471000/DURPrdlstInfoService03/getSpcifyAgrdeTabooInfoList03")
    fun getSpecificAgeTabooInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?
    ):Call<DurResponse<List<Item>>>

    //용량주의
    @GET("/1471000/DURPrdlstInfoService03/getCpctyAtentInfoList03")
    fun getCapacityAttentionInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?,
    ):Call<DurResponse<List<Item>>>

    //투여기간주의
    @GET("/1471000/DURPrdlstInfoService03/getMdctnPdAtentInfoList03")
    fun getPeriodCautionInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?,
    ):Call<DurResponse<List<Item>>>

    //효능군중복
    @GET("/1471000/DURPrdlstInfoService03/getEfcyDplctInfoList03")
    fun getEfficacyDuplicationInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?,
        @Query("bizrno") Bizrno: String?
    ):Call<DurResponse<List<Item>>>

    //서방정분할
    @GET("/1471000/DURPrdlstInfoService03/getSeobangjeongPartitnAtentInfoList03")
    fun getReleaseTabletSplittingInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("itemName") ItemName: String?,
        @Query("entpName") EntpName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?
    ):Call<DurResponse<List<Item>>>

    //임부금기
    @GET("/1471000/DURPrdlstInfoService03/getPwnmTabooInfoList03")
    fun getPregnancyCautionInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int?,
        @Query("numOfRows") NumOfRows: Int?,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String?,
        @Query("ingrCode") IngrCode: String?, //Dur 성분코드
        @Query("itemName") ItemName: String?,
        @Query("start_change_date") StartChangeDate: String?,
        @Query("end_change_date") EndChangeDate: String?,
    ):Call<DurResponse<List<Item>>>
}