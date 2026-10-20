package com.example.bitpbui1.data.api

import com.example.bitpbui1.data.model.Dish
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MockApiClient(
    private val baseUrl: String = "https://6723e71d2148783a0028a385.mockapi.io/api/v1/dishes"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun fetchRemoteDishes(): Result<List<JSONObject>> {
        return try {
            val request = Request.Builder().url(baseUrl).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(bodyString)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createRemoteDish(dish: Dish): Result<String> {
        return try {
            val json = JSONObject().apply {
                put("BIT230184_Name", dish.name)
                put("BIT230184_Price", dish.price)
                put("BIT230184_PreparationTime", dish.preparationTime)
                put("BIT230184_IsAvailable", dish.isAvailable)
                put("BIT230184_Description", dish.description)
                put("BIT230184_DishCategoryId", dish.dishCategoryId)
                val imgUrls = JSONArray()
                dish.images.forEach { imgUrls.put(it.imageUrl) }
                put("BIT230184_Images", imgUrls)
            }

            val body = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(baseUrl).post(body).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val resBody = response.body?.string() ?: "{}"
                    val resJson = JSONObject(resBody)
                    val remoteId = resJson.optString("id", "")
                    Result.success(remoteId)
                } else {
                    Result.failure(Exception("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateRemoteDish(remoteId: String, dish: Dish): Result<Boolean> {
        return try {
            val json = JSONObject().apply {
                put("BIT230184_Name", dish.name)
                put("BIT230184_Price", dish.price)
                put("BIT230184_PreparationTime", dish.preparationTime)
                put("BIT230184_IsAvailable", dish.isAvailable)
                put("BIT230184_Description", dish.description)
                put("BIT230184_DishCategoryId", dish.dishCategoryId)
            }

            val body = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url("$baseUrl/$remoteId").put(body).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteRemoteDish(remoteId: String): Result<Boolean> {
        return try {
            val request = Request.Builder().url("$baseUrl/$remoteId").delete().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
