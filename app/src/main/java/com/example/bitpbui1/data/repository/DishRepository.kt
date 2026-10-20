package com.example.bitpbui1.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.bitpbui1.data.db.DatabaseHelper
import com.example.bitpbui1.data.model.Dish
import com.example.bitpbui1.data.model.DishCategory
import com.example.bitpbui1.data.model.DishImage
import com.example.bitpbui1.data.validation.DishValidation
import com.example.bitpbui1.data.validation.ValidationResult

class DishRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // ================= DISH CATEGORY =================
    fun getAllCategories(): List<DishCategory> {
        val list = mutableListOf<DishCategory>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CATEGORY,
            null, null, null, null, null,
            "${DatabaseHelper.COL_CAT_NAME} ASC"
        )
        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)
            val nameIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME)
            val descIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_DESC)
            while (c.moveToNext()) {
                list.add(
                    DishCategory(
                        id = c.getLong(idIndex),
                        name = c.getString(nameIndex),
                        description = c.getString(descIndex)
                    )
                )
            }
        }
        return list
    }

    fun getCategoryById(categoryId: Long): DishCategory? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CATEGORY,
            null,
            "${DatabaseHelper.COL_CAT_ID} = ?",
            arrayOf(categoryId.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)
                val nameIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME)
                val descIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_DESC)
                return DishCategory(
                    id = c.getLong(idIndex),
                    name = c.getString(nameIndex),
                    description = c.getString(descIndex)
                )
            }
        }
        return null
    }

    fun deleteCategory(categoryId: Long): Result<Boolean> {
        val db = dbHelper.writableDatabase
        // 4.4 Ràng buộc: Không cho xóa loại món đang được sử dụng
        val cursor = db.query(
            DatabaseHelper.TABLE_DISH,
            arrayOf(DatabaseHelper.COL_DISH_ID),
            "${DatabaseHelper.COL_DISH_CATEGORY_ID} = ?",
            arrayOf(categoryId.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.count > 0) {
                return Result.failure(Exception("err_category_in_use"))
            }
        }

        val rows = db.delete(
            DatabaseHelper.TABLE_CATEGORY,
            "${DatabaseHelper.COL_CAT_ID} = ?",
            arrayOf(categoryId.toString())
        )
        return if (rows > 0) Result.success(true) else Result.failure(Exception("err_category_not_found"))
    }

    // ================= DISH =================
    fun getAllDishes(): List<Dish> {
        val list = mutableListOf<Dish>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_DISH,
            null,
            "${DatabaseHelper.COL_DISH_SYNC_STATUS} != 3", // Exclude pending deleted items
            null, null, null,
            "${DatabaseHelper.COL_DISH_ID} DESC"
        )
        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)
            val nameIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_NAME)
            val priceIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PRICE)
            val prepTimeIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PREP_TIME)
            val availIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_IS_AVAILABLE)
            val descIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_DESC)
            val catIdIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_CATEGORY_ID)

            while (c.moveToNext()) {
                val dishId = c.getLong(idIndex)
                val images = getImagesForDish(dishId)
                list.add(
                    Dish(
                        id = dishId,
                        name = c.getString(nameIndex),
                        price = c.getDouble(priceIndex),
                        preparationTime = c.getInt(prepTimeIndex),
                        isAvailable = c.getInt(availIndex) == 1,
                        description = c.getString(descIndex),
                        dishCategoryId = c.getLong(catIdIndex),
                        images = images
                    )
                )
            }
        }
        return list
    }

    fun getDishById(dishId: Long): Dish? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_DISH,
            null,
            "${DatabaseHelper.COL_DISH_ID} = ?",
            arrayOf(dishId.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)
                val nameIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_NAME)
                val priceIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PRICE)
                val prepTimeIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PREP_TIME)
                val availIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_IS_AVAILABLE)
                val descIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_DESC)
                val catIdIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_CATEGORY_ID)

                val images = getImagesForDish(dishId)
                return Dish(
                    id = c.getLong(idIndex),
                    name = c.getString(nameIndex),
                    price = c.getDouble(priceIndex),
                    preparationTime = c.getInt(prepTimeIndex),
                    isAvailable = c.getInt(availIndex) == 1,
                    description = c.getString(descIndex),
                    dishCategoryId = c.getLong(catIdIndex),
                    images = images
                )
            }
        }
        return null
    }

    fun isDishNameDuplicate(name: String, categoryId: Long, excludeDishId: Long = 0): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_DISH,
            arrayOf(DatabaseHelper.COL_DISH_ID),
            "LOWER(${DatabaseHelper.COL_DISH_NAME}) = LOWER(?) AND ${DatabaseHelper.COL_DISH_CATEGORY_ID} = ? AND ${DatabaseHelper.COL_DISH_ID} != ?",
            arrayOf(name.trim(), categoryId.toString(), excludeDishId.toString()),
            null, null, null
        )
        cursor.use { c ->
            return c.count > 0
        }
    }

    fun saveDish(
        dishId: Long = 0,
        name: String,
        price: Double?,
        preparationTime: Int?,
        isAvailable: Boolean,
        description: String,
        categoryId: Long,
        imageUrls: List<String>,
        isSyncing: Boolean = false
    ): Result<Long> {
        val categories = getAllCategories().map { it.id }
        val isDuplicate = isDishNameDuplicate(name, categoryId, excludeDishId = dishId)

        val validation = DishValidation.validateDish(
            name = name,
            price = price,
            preparationTime = preparationTime,
            categoryId = categoryId,
            existingCategories = categories,
            isDuplicateNameInCategory = isDuplicate
        )

        if (validation is ValidationResult.Error) {
            return Result.failure(Exception(validation.messageKey))
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val syncStatus = if (isSyncing) 0 else if (dishId == 0L) 1 else 2

            val values = ContentValues().apply {
                put(DatabaseHelper.COL_DISH_NAME, name.trim())
                put(DatabaseHelper.COL_DISH_PRICE, price!!)
                put(DatabaseHelper.COL_DISH_PREP_TIME, preparationTime!!)
                put(DatabaseHelper.COL_DISH_IS_AVAILABLE, if (isAvailable) 1 else 0)
                put(DatabaseHelper.COL_DISH_DESC, description.trim())
                put(DatabaseHelper.COL_DISH_CATEGORY_ID, categoryId)
                put(DatabaseHelper.COL_DISH_SYNC_STATUS, syncStatus)
            }

            val resultDishId: Long
            if (dishId == 0L) {
                // Insert New Dish
                resultDishId = db.insert(DatabaseHelper.TABLE_DISH, null, values)
                if (resultDishId == -1L) {
                    throw Exception("err_save_failed")
                }
                // Insert Images
                imageUrls.forEachIndexed { index, url ->
                    val imgValues = ContentValues().apply {
                        put(DatabaseHelper.COL_IMG_DISH_ID, resultDishId)
                        put(DatabaseHelper.COL_IMG_URL, url.trim())
                        put(DatabaseHelper.COL_IMG_IS_THUMBNAIL, if (index == 0) 1 else 0)
                    }
                    db.insert(DatabaseHelper.TABLE_IMAGE, null, imgValues)
                }
            } else {
                // Update Existing Dish
                resultDishId = dishId
                val rows = db.update(
                    DatabaseHelper.TABLE_DISH,
                    values,
                    "${DatabaseHelper.COL_DISH_ID} = ?",
                    arrayOf(dishId.toString())
                )
                if (rows <= 0) {
                    throw Exception("err_dish_not_found")
                }
            }

            db.setTransactionSuccessful()
            return Result.success(resultDishId)
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            db.endTransaction()
        }
    }

    fun deleteDish(dishId: Long): Result<Boolean> {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Check if item has a MockAPI ID
            val cursor = db.query(
                DatabaseHelper.TABLE_DISH,
                arrayOf(DatabaseHelper.COL_DISH_MOCK_API_ID),
                "${DatabaseHelper.COL_DISH_ID} = ?",
                arrayOf(dishId.toString()),
                null, null, null
            )
            var mockApiId = ""
            cursor.use { c ->
                if (c.moveToFirst()) {
                    mockApiId = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_MOCK_API_ID))
                }
            }

            if (mockApiId.isNotBlank()) {
                // Mark for pending deletion sync
                val values = ContentValues().apply {
                    put(DatabaseHelper.COL_DISH_SYNC_STATUS, 3) // PENDING_DELETE
                }
                db.update(
                    DatabaseHelper.TABLE_DISH,
                    values,
                    "${DatabaseHelper.COL_DISH_ID} = ?",
                    arrayOf(dishId.toString())
                )
            } else {
                // Delete locally right away
                db.delete(
                    DatabaseHelper.TABLE_IMAGE,
                    "${DatabaseHelper.COL_IMG_DISH_ID} = ?",
                    arrayOf(dishId.toString())
                )
                db.delete(
                    DatabaseHelper.TABLE_DISH,
                    "${DatabaseHelper.COL_DISH_ID} = ?",
                    arrayOf(dishId.toString())
                )
            }
            db.setTransactionSuccessful()
            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            db.endTransaction()
        }
    }

    // ================= DISH IMAGE =================
    fun getImagesForDish(dishId: Long): List<DishImage> {
        val list = mutableListOf<DishImage>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_IMAGE,
            null,
            "${DatabaseHelper.COL_IMG_DISH_ID} = ?",
            arrayOf(dishId.toString()),
            null, null,
            "${DatabaseHelper.COL_IMG_IS_THUMBNAIL} DESC, ${DatabaseHelper.COL_IMG_ID} ASC"
        )
        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_IMG_ID)
            val dishIdIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_IMG_DISH_ID)
            val urlIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_IMG_URL)
            val isThumbIndex = c.getColumnIndexOrThrow(DatabaseHelper.COL_IMG_IS_THUMBNAIL)

            while (c.moveToNext()) {
                list.add(
                    DishImage(
                        id = c.getLong(idIndex),
                        dishId = c.getLong(dishIdIndex),
                        imageUrl = c.getString(urlIndex),
                        isThumbnail = c.getInt(isThumbIndex) == 1
                    )
                )
            }
        }
        return list
    }

    fun addImageToDish(dishId: Long, imageUrl: String): Result<Boolean> {
        if (imageUrl.isBlank()) {
            return Result.failure(Exception("err_image_url_empty"))
        }
        val currentImages = getImagesForDish(dishId)
        val isFirstImage = currentImages.isEmpty()

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_IMG_DISH_ID, dishId)
            put(DatabaseHelper.COL_IMG_URL, imageUrl.trim())
            put(DatabaseHelper.COL_IMG_IS_THUMBNAIL, if (isFirstImage) 1 else 0)
        }
        val id = db.insert(DatabaseHelper.TABLE_IMAGE, null, values)
        return if (id != -1L) Result.success(true) else Result.failure(Exception("err_add_image_failed"))
    }

    fun setThumbnailImage(dishId: Long, selectedImageId: Long): Result<Boolean> {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Reset all thumbnails for this dish
            val resetValues = ContentValues().apply {
                put(DatabaseHelper.COL_IMG_IS_THUMBNAIL, 0)
            }
            db.update(
                DatabaseHelper.TABLE_IMAGE,
                resetValues,
                "${DatabaseHelper.COL_IMG_DISH_ID} = ?",
                arrayOf(dishId.toString())
            )

            // Set chosen image as thumbnail
            val setValues = ContentValues().apply {
                put(DatabaseHelper.COL_IMG_IS_THUMBNAIL, 1)
            }
            db.update(
                DatabaseHelper.TABLE_IMAGE,
                setValues,
                "${DatabaseHelper.COL_IMG_ID} = ?",
                arrayOf(selectedImageId.toString())
            )

            db.setTransactionSuccessful()
            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            db.endTransaction()
        }
    }

    fun deleteImage(imageId: Long, dishId: Long): Result<Boolean> {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val rows = db.delete(
                DatabaseHelper.TABLE_IMAGE,
                "${DatabaseHelper.COL_IMG_ID} = ?",
                arrayOf(imageId.toString())
            )

            // Check if deleted image was thumbnail
            val remainingImages = getImagesForDish(dishId)
            if (remainingImages.isNotEmpty() && remainingImages.none { it.isThumbnail }) {
                // Set first remaining image as thumbnail
                val firstImage = remainingImages.first()
                val setValues = ContentValues().apply {
                    put(DatabaseHelper.COL_IMG_IS_THUMBNAIL, 1)
                }
                db.update(
                    DatabaseHelper.TABLE_IMAGE,
                    setValues,
                    "${DatabaseHelper.COL_IMG_ID} = ?",
                    arrayOf(firstImage.id.toString())
                )
            }

            db.setTransactionSuccessful()
            return if (rows > 0) Result.success(true) else Result.failure(Exception("err_image_not_found"))
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            db.endTransaction()
        }
    }
}
