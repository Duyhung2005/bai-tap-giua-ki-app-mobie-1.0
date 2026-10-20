package com.example.bitpbui1.data.sync

import android.content.ContentValues
import android.content.Context
import com.example.bitpbui1.data.api.MockApiClient
import com.example.bitpbui1.data.db.DatabaseHelper
import com.example.bitpbui1.data.model.Dish
import com.example.bitpbui1.data.repository.DishRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(
    context: Context,
    private val repository: DishRepository = DishRepository(context),
    private val mockApiClient: MockApiClient = MockApiClient()
) {
    private val dbHelper = DatabaseHelper(context)

    companion object {
        const val SYNC_STATUS_SYNCED = 0
        const val SYNC_STATUS_PENDING_INSERT = 1
        const val SYNC_STATUS_PENDING_UPDATE = 2
        const val SYNC_STATUS_PENDING_DELETE = 3
    }

    suspend fun performAutoSync(): Result<Int> = withContext(Dispatchers.IO) {
        var syncedCount = 0
        val db = dbHelper.writableDatabase

        try {
            // 1. Sync Pending Insertions (Offline created dishes)
            val pendingInsertsCursor = db.query(
                DatabaseHelper.TABLE_DISH,
                null,
                "${DatabaseHelper.COL_DISH_SYNC_STATUS} = ?",
                arrayOf(SYNC_STATUS_PENDING_INSERT.toString()),
                null, null, null
            )
            pendingInsertsCursor.use { c ->
                val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)
                while (c.moveToNext()) {
                    val localId = c.getLong(idIndex)
                    val dish = repository.getDishById(localId)
                    if (dish != null) {
                        val result = mockApiClient.createRemoteDish(dish)
                        if (result.isSuccess) {
                            val remoteId = result.getOrNull() ?: ""
                            val values = ContentValues().apply {
                                put(DatabaseHelper.COL_DISH_SYNC_STATUS, SYNC_STATUS_SYNCED)
                                put(DatabaseHelper.COL_DISH_MOCK_API_ID, remoteId)
                            }
                            db.update(
                                DatabaseHelper.TABLE_DISH,
                                values,
                                "${DatabaseHelper.COL_DISH_ID} = ?",
                                arrayOf(localId.toString())
                            )
                            syncedCount++
                        }
                    }
                }
            }

            // 2. Sync Pending Updates (Offline edited dishes)
            val pendingUpdatesCursor = db.query(
                DatabaseHelper.TABLE_DISH,
                null,
                "${DatabaseHelper.COL_DISH_SYNC_STATUS} = ?",
                arrayOf(SYNC_STATUS_PENDING_UPDATE.toString()),
                null, null, null
            )
            pendingUpdatesCursor.use { c ->
                val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)
                val mockIdIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_MOCK_API_ID)
                while (c.moveToNext()) {
                    val localId = c.getLong(idIndex)
                    val mockApiId = c.getString(mockIdIndex)
                    val dish = repository.getDishById(localId)

                    if (dish != null) {
                        val result = if (mockApiId.isNotBlank()) {
                            mockApiClient.updateRemoteDish(mockApiId, dish)
                        } else {
                            val res = mockApiClient.createRemoteDish(dish)
                            if (res.isSuccess) {
                                val newRemoteId = res.getOrNull() ?: ""
                                val v = ContentValues().apply {
                                    put(DatabaseHelper.COL_DISH_MOCK_API_ID, newRemoteId)
                                }
                                db.update(
                                    DatabaseHelper.TABLE_DISH,
                                    v,
                                    "${DatabaseHelper.COL_DISH_ID} = ?",
                                    arrayOf(localId.toString())
                                )
                            }
                            Result.success(true)
                        }

                        if (result.isSuccess) {
                            val values = ContentValues().apply {
                                put(DatabaseHelper.COL_DISH_SYNC_STATUS, SYNC_STATUS_SYNCED)
                            }
                            db.update(
                                DatabaseHelper.TABLE_DISH,
                                values,
                                "${DatabaseHelper.COL_DISH_ID} = ?",
                                arrayOf(localId.toString())
                            )
                            syncedCount++
                        }
                    }
                }
            }

            // 3. Sync Pending Deletions (Offline deleted dishes)
            val pendingDeletesCursor = db.query(
                DatabaseHelper.TABLE_DISH,
                null,
                "${DatabaseHelper.COL_DISH_SYNC_STATUS} = ?",
                arrayOf(SYNC_STATUS_PENDING_DELETE.toString()),
                null, null, null
            )
            pendingDeletesCursor.use { c ->
                val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)
                val mockIdIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_MOCK_API_ID)
                while (c.moveToNext()) {
                    val localId = c.getLong(idIndex)
                    val mockApiId = c.getString(mockIdIndex)

                    if (mockApiId.isNotBlank()) {
                        mockApiClient.deleteRemoteDish(mockApiId)
                    }

                    // Delete permanently locally
                    db.delete(
                        DatabaseHelper.TABLE_IMAGE,
                        "${DatabaseHelper.COL_IMG_DISH_ID} = ?",
                        arrayOf(localId.toString())
                    )
                    db.delete(
                        DatabaseHelper.TABLE_DISH,
                        "${DatabaseHelper.COL_DISH_ID} = ?",
                        arrayOf(localId.toString())
                    )
                    syncedCount++
                }
            }

            // 4. Fetch Remote dishes and insert any missing items (Consistency & No Duplicates)
            val remoteResult = mockApiClient.fetchRemoteDishes()
            if (remoteResult.isSuccess) {
                val remoteList = remoteResult.getOrNull() ?: emptyList()
                val categories = repository.getAllCategories()
                val defaultCatId = categories.firstOrNull()?.id ?: 1L

                remoteList.forEach { json ->
                    val name = json.optString("BIT230184_Name", "")
                    val price = json.optDouble("BIT230184_Price", 0.0)
                    val prepTime = json.optInt("BIT230184_PreparationTime", 10)
                    val isAvailable = json.optBoolean("BIT230184_IsAvailable", true)
                    val desc = json.optString("BIT230184_Description", "")
                    val catId = json.optLong("BIT230184_DishCategoryId", defaultCatId)

                    if (name.isNotBlank() && !repository.isDishNameDuplicate(name, catId)) {
                        repository.saveDish(
                            dishId = 0,
                            name = name,
                            price = price,
                            preparationTime = prepTime,
                            isAvailable = isAvailable,
                            description = desc,
                            categoryId = catId,
                            imageUrls = emptyList(),
                            isSyncing = true
                        )
                    }
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
