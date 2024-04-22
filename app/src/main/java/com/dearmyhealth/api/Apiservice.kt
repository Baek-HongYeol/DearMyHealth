package com.dearmyhealth.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
interface Apiservice {

    //DUR품목정보 조회
    @GET("/1471000/DURPrdlstInfoService03/getDurPrdlstInfoList03")
    fun getDurItemInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("itemName") ItemName: String,
        @Query("entpName") EntpName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("bizrno") Bizrno: String,
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String //Dur 성분코드
    ): Response<DurItem>

    //병용금기
    @GET("/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03")
    fun getContraindicationInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("bizrno") Bizrno: String
    ):Response<DurItem>

    //노인주의
    @GET("/1471000/DURPrdlstInfoService03/getOdsnAtentInfoList03")
    fun getElderlyCautionInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String
    ):Response<DurItem>

    //특정연령대금기
    @GET("/1471000/DURPrdlstInfoService03/getSpcifyAgrdeTabooInfoList03")
    fun getAgeGroupContraindicationInfo(
        @Query("itemSeq") ItemSeq: String,
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String
    ):Response<DurItem>

    //용량주의
    @GET("/1471000/DURPrdlstInfoService03/getCpctyAtentInfoList03")
    fun getDosageCautionInfo(
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("itemSeq") ItemSeq: String
    ):Response<DurItem>

    //투여기간주의
    @GET("/1471000/DURPrdlstInfoService03/getMdctnPdAtentInfoList03")
    fun getPeriodCautionInfo(
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("itemSeq") ItemSeq: String
    ):Response<DurItem>

    //효능군중복
    @GET("/1471000/DURPrdlstInfoService03/getEfcyDplctInfoList03")
    fun getEfficacyGroupDuplicationInfo(
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("itemSeq") ItemSeq: String,
        @Query("bizrno") Bizrno: String
    ):Response<DurItem>

    //서방정분할
    @GET("/1471000/DURPrdlstInfoService03/getSeobangjeongPartitnAtentInfoList03")
    fun getReleaseTabletSplittingInfo(
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("itemName") ItemName: String,
        @Query("entpName") EntpName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("itemSeq") ItemSeq: String
    ):Response<DurItem>

    //임부금기
    @GET("/1471000/DURPrdlstInfoService03/getPwnmTabooInfoList03")
    fun getPregnancyCautionInfo(
        @Query("serviceKey") ServiceKey: String,
        @Query("pageNo") PageNo: Int=0,
        @Query("numOfRows") NumOfRows: Int =100,
        @Query("type") Type: String ="json",
        @Query("typeName") TypeName: String,
        @Query("ingrCode") IngrCode: String, //Dur 성분코드
        @Query("itemName") ItemName: String,
        @Query("start_change_date") StartChangeDate: String,
        @Query("end_change_date") EndChangeDate: String,
        @Query("itemSeq") ItemSeq: String
    ):Response<DurItem>
}