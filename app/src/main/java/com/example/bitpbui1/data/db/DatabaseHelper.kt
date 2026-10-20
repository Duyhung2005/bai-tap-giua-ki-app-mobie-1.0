package com.example.bitpbui1.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bitpbui1.data.model.Dish
import com.example.bitpbui1.data.model.DishCategory
import com.example.bitpbui1.data.model.DishImage

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "BIT230184_DishManager.db"
        const val DATABASE_VERSION = 1

        // MSSV Prefix
        private const val MSSV = "BIT230184_"

        // Table Names
        const val TABLE_CATEGORY = "${MSSV}DishCategory"
        const val TABLE_DISH = "${MSSV}Dish"
        const val TABLE_IMAGE = "${MSSV}DishImage"

        // Column Names - DishCategory
        const val COL_CAT_ID = "${MSSV}Id"
        const val COL_CAT_NAME = "${MSSV}Name"
        const val COL_CAT_DESC = "${MSSV}Description"

        // Column Names - Dish
        const val COL_DISH_ID = "${MSSV}Id"
        const val COL_DISH_NAME = "${MSSV}Name"
        const val COL_DISH_PRICE = "${MSSV}Price"
        const val COL_DISH_PREP_TIME = "${MSSV}PreparationTime"
        const val COL_DISH_IS_AVAILABLE = "${MSSV}IsAvailable"
        const val COL_DISH_DESC = "${MSSV}Description"
        const val COL_DISH_CATEGORY_ID = "${MSSV}DishCategoryId"
        const val COL_DISH_SYNC_STATUS = "${MSSV}SyncStatus"
        const val COL_DISH_MOCK_API_ID = "${MSSV}MockApiId"

        // Column Names - DishImage
        const val COL_IMG_ID = "${MSSV}Id"
        const val COL_IMG_DISH_ID = "${MSSV}DishId"
        const val COL_IMG_URL = "${MSSV}ImageUrl"
        const val COL_IMG_IS_THUMBNAIL = "${MSSV}IsThumbnail"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCategoryTable = """
            CREATE TABLE $TABLE_CATEGORY (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT NOT NULL,
                $COL_CAT_DESC TEXT NOT NULL
            )
        """.trimIndent()

        val createDishTable = """
            CREATE TABLE $TABLE_DISH (
                $COL_DISH_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DISH_NAME TEXT NOT NULL,
                $COL_DISH_PRICE REAL NOT NULL,
                $COL_DISH_PREP_TIME INTEGER NOT NULL,
                $COL_DISH_IS_AVAILABLE INTEGER NOT NULL,
                $COL_DISH_DESC TEXT NOT NULL,
                $COL_DISH_CATEGORY_ID INTEGER NOT NULL,
                $COL_DISH_SYNC_STATUS INTEGER DEFAULT 0,
                $COL_DISH_MOCK_API_ID TEXT DEFAULT '',
                FOREIGN KEY($COL_DISH_CATEGORY_ID) REFERENCES $TABLE_CATEGORY($COL_CAT_ID)
            )
        """.trimIndent()

        val createImageTable = """
            CREATE TABLE $TABLE_IMAGE (
                $COL_IMG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_IMG_DISH_ID INTEGER NOT NULL,
                $COL_IMG_URL TEXT NOT NULL,
                $COL_IMG_IS_THUMBNAIL INTEGER NOT NULL,
                FOREIGN KEY($COL_IMG_DISH_ID) REFERENCES $TABLE_DISH($COL_DISH_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createCategoryTable)
        db.execSQL(createDishTable)
        db.execSQL(createImageTable)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DISH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORY")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // 03 Categories
        val cat1Id = insertCategoryInternal(db, "Món khai vị", "Các món ăn nhẹ kích thích vị giác trước bữa chính")
        val cat2Id = insertCategoryInternal(db, "Món chính", "Các món ăn đượm vị cung cấp dinh dưỡng chính")
        val cat3Id = insertCategoryInternal(db, "Món tráng miệng", "Các món bánh, chè, kem ngọt nhẹ kết thúc bữa ăn")

        // 05 Dishes
        // Dish 1
        val dish1Id = insertDishInternal(
            db,
            "Phở Bò Tái Nạm",
            55000.0,
            12, // Quick
            true,
            "Phở bò truyền thống với nước dùng hầm xương ngọt thanh, thịt bò tươi ngon",
            cat2Id
        )
        insertImageInternal(db, dish1Id, "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800", true)
        insertImageInternal(db, dish1Id, "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800", false)

        // Dish 2
        val dish2Id = insertDishInternal(
            db,
            "Bún Chả Hà Nội",
            60000.0,
            20, // Regular
            true,
            "Chả nướng than hoa thơm lừng ăn kèm bún tươi, rau sống và nước chấm tỏi ớt",
            cat2Id
        )
        insertImageInternal(db, dish2Id, "https://images.unsplash.com/photo-1541544741938-0af808871cc0?w=800", true)
        insertImageInternal(db, dish2Id, "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=800", false)

        // Dish 3
        val dish3Id = insertDishInternal(
            db,
            "Gỏi Cuốn Tôm Thịt",
            35000.0,
            10, // Quick
            true,
            "Tôm tươi, thịt luộc, bún và rau thơm cuốn bánh tráng tươi mát",
            cat1Id
        )
        insertImageInternal(db, dish3Id, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=800", true)
        insertImageInternal(db, dish3Id, "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800", false)

        // Dish 4
        val dish4Id = insertDishInternal(
            db,
            "Nem Rán Hà Nội",
            40000.0,
            15, // Quick
            false, // Unavailable (Ngừng bán)
            "Nem rán giòn rụm nhân thịt, miến, mộc nhĩ chuẩn vị truyền thống",
            cat1Id
        )
        insertImageInternal(db, dish4Id, "https://images.unsplash.com/photo-1617093727343-374698b1b08d?w=800", true)
        insertImageInternal(db, dish4Id, "https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=800", false)

        // Dish 5
        val dish5Id = insertDishInternal(
            db,
            "Chè Ba Màu",
            25000.0,
            8, // Quick
            true,
            "Chè truyền thống với đậu đỏ, đậu xanh, thạch lá dứa và nước cốt dừa béo ngậy",
            cat3Id
        )
        insertImageInternal(db, dish5Id, "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800", true)
        insertImageInternal(db, dish5Id, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=800", false)
    }

    private fun insertCategoryInternal(db: SQLiteDatabase, name: String, desc: String): Long {
        val values = ContentValues().apply {
            put(COL_CAT_NAME, name)
            put(COL_CAT_DESC, desc)
        }
        return db.insert(TABLE_CATEGORY, null, values)
    }

    private fun insertDishInternal(
        db: SQLiteDatabase,
        name: String,
        price: Double,
        prepTime: Int,
        isAvailable: Boolean,
        desc: String,
        categoryId: Long
    ): Long {
        val values = ContentValues().apply {
            put(COL_DISH_NAME, name)
            put(COL_DISH_PRICE, price)
            put(COL_DISH_PREP_TIME, prepTime)
            put(COL_DISH_IS_AVAILABLE, if (isAvailable) 1 else 0)
            put(COL_DISH_DESC, desc)
            put(COL_DISH_CATEGORY_ID, categoryId)
        }
        return db.insert(TABLE_DISH, null, values)
    }

    private fun insertImageInternal(
        db: SQLiteDatabase,
        dishId: Long,
        imageUrl: String,
        isThumbnail: Boolean
    ): Long {
        val values = ContentValues().apply {
            put(COL_IMG_DISH_ID, dishId)
            put(COL_IMG_URL, imageUrl)
            put(COL_IMG_IS_THUMBNAIL, if (isThumbnail) 1 else 0)
        }
        return db.insert(TABLE_IMAGE, null, values)
    }
}
