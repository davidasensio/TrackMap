package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.data.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

object NetworkHelper {
    suspend fun <T : Any> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                Result.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> Result.Error(true) // Network Error
                    is HttpException -> Result.Error(
                        false,
                        throwable.code(),
                        getErrorBody(throwable)
                    ) // GenericError
                    else -> Result.Error(false, null, null)
                }
            }
        }
    }

    private fun getErrorBody(throwable: HttpException): String? {
        return try {
            throwable.response()?.errorBody()?.source().let {
                it.toString()
            }
        } catch (e: Exception) {
            null
        }
    }
}
