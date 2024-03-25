package com.dearmyhealth.modules.Diet.model

import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Diet

data class DietCreateResult (
    val success: Result.Success<Diet>? = null,
    val error: Int? = null
)