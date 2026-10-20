package com.example.bitpbui1.i18n

object Strings {
    fun get(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.EN -> enMap[key] ?: key
            AppLanguage.VI -> viMap[key] ?: key
        }
    }

    private val enMap = mapOf(
        // App Title & Headers
        "app_title" to "Dish Management",
        "student_info" to "BÙI DUY HÙNG - BIT230184",
        "search_hint" to "Search dish by name...",
        "filter_category" to "Category",
        "all_categories" to "All Categories",
        "filter_status" to "Status",
        "status_all" to "All Status",
        "status_available" to "Selling",
        "status_unavailable" to "Stopped",
        "sort_by" to "Sort By",
        "sort_default" to "Default",
        "sort_price_asc" to "Price: Low to High",
        "sort_price_desc" to "Price: High to Low",
        "sort_prep_time_asc" to "Prep Time: Low to High",
        "price_min" to "Min Price",
        "price_max" to "Max Price",
        "price_range" to "Price Range",
        "filter_apply" to "Apply Filters",
        "filter_reset" to "Reset",

        // Tags & Badges
        "quick_prep" to "Quick",
        "regular_prep" to "Regular",
        "available_badge" to "Available",
        "unavailable_badge" to "Stopped",
        "thumbnail_label" to "Main Thumbnail",

        // Actions & Buttons
        "btn_add_dish" to "Add Dish",
        "btn_edit" to "Edit",
        "btn_delete" to "Delete",
        "btn_save" to "Save Dish",
        "btn_cancel" to "Cancel",
        "btn_back" to "Back",
        "btn_add_image" to "Add Image URL",
        "btn_set_thumbnail" to "Set as Thumbnail",
        "btn_delete_image" to "Remove Image",
        "btn_confirm" to "Confirm",

        // Screens Titles
        "screen_dish_list" to "Dishes List",
        "screen_dish_detail" to "Dish Detail",
        "screen_add_dish" to "Create New Dish",
        "screen_edit_dish" to "Edit Dish",

        // Form Fields
        "lbl_dish_name" to "Dish Name *",
        "lbl_price" to "Price (VND) *",
        "lbl_prep_time" to "Preparation Time (Mins) *",
        "lbl_category" to "Category *",
        "lbl_status" to "Selling Status",
        "lbl_description" to "Description",
        "lbl_image_urls" to "Image URLs",
        "lbl_enter_image_url" to "Enter Image URL",

        // Dialogs
        "dialog_delete_dish_title" to "Delete Dish",
        "dialog_delete_dish_msg" to "Are you sure you want to delete this dish? This action cannot be undone.",
        "dialog_delete_image_title" to "Delete Image",
        "dialog_delete_image_msg" to "Are you sure you want to remove this image?",

        // Empty states
        "empty_dish_list" to "No dishes found matching your search criteria.",
        "empty_images" to "No images available for this dish.",

        // Validation & Errors
        "online_status" to "Online - Auto-Sync Enabled",
        "offline_status" to "Offline Mode - Saved Locally",
        "msg_synced" to "Auto-synced with MockAPI!",
        "err_name_empty" to "Dish name cannot be empty.",
        "err_price_invalid" to "Price must be greater than 0.",
        "err_prep_time_invalid" to "Preparation time must be greater than 0.",
        "err_category_invalid" to "Please select a valid category.",
        "err_name_duplicate" to "A dish with this name already exists in this category.",
        "err_category_in_use" to "Cannot delete a category that is currently assigned to dishes.",
        "err_dish_not_found" to "Dish not found or has been deleted.",
        "err_price_range_invalid" to "Min price cannot be greater than max price.",
        "err_image_url_empty" to "Image URL cannot be empty.",
        "msg_dish_saved" to "Dish saved successfully!",
        "msg_dish_deleted" to "Dish deleted successfully!",
        "msg_thumbnail_updated" to "Thumbnail image updated!"
    )

    private val viMap = mapOf(
        // App Title & Headers
        "app_title" to "Quản Lý Món Ăn",
        "student_info" to "BÙI DUY HÙNG - BIT230184",
        "search_hint" to "Tìm món theo tên...",
        "filter_category" to "Loại món",
        "all_categories" to "Tất cả loại món",
        "filter_status" to "Trạng thái",
        "status_all" to "Tất cả trạng thái",
        "status_available" to "Đang bán",
        "status_unavailable" to "Ngừng bán",
        "sort_by" to "Sắp xếp",
        "sort_default" to "Mặc định",
        "sort_price_asc" to "Giá: Thấp đến Cao",
        "sort_price_desc" to "Giá: Cao đến Thấp",
        "sort_prep_time_asc" to "Thời gian chế biến: Nhanh nhất",
        "price_min" to "Giá từ",
        "price_max" to "Giá đến",
        "price_range" to "Khoảng giá",
        "filter_apply" to "Áp dụng lọc",
        "filter_reset" to "Đặt lại",

        // Tags & Badges
        "quick_prep" to "Nhanh",
        "regular_prep" to "Thông thường",
        "available_badge" to "Đang bán",
        "unavailable_badge" to "Ngừng bán",
        "thumbnail_label" to "Ảnh đại diện",

        // Actions & Buttons
        "btn_add_dish" to "Thêm món",
        "btn_edit" to "Sửa",
        "btn_delete" to "Xóa",
        "btn_save" to "Lưu món ăn",
        "btn_cancel" to "Hủy",
        "btn_back" to "Quay lại",
        "btn_add_image" to "Thêm URL ảnh",
        "btn_set_thumbnail" to "Đặt làm ảnh đại diện",
        "btn_delete_image" to "Xóa ảnh",
        "btn_confirm" to "Xác nhận",

        // Screens Titles
        "screen_dish_list" to "Danh Sách Món Ăn",
        "screen_dish_detail" to "Chi Tiết Món Ăn",
        "screen_add_dish" to "Thêm Món Ăn Mới",
        "screen_edit_dish" to "Chỉnh Sửa Món Ăn",

        // Form Fields
        "lbl_dish_name" to "Tên món ăn *",
        "lbl_price" to "Giá bán (VNĐ) *",
        "lbl_prep_time" to "Thời gian chế biến (Phút) *",
        "lbl_category" to "Loại món ăn *",
        "lbl_status" to "Trạng thái kinh doanh",
        "lbl_description" to "Mô tả chi tiết",
        "lbl_image_urls" to "Danh sách URL ảnh",
        "lbl_enter_image_url" to "Nhập đường dẫn URL ảnh",

        // Dialogs
        "dialog_delete_dish_title" to "Xác nhận xóa",
        "dialog_delete_dish_msg" to "Bạn có chắc chắn muốn xóa món ăn này? Hành động này không thể hoàn tác.",
        "dialog_delete_image_title" to "Xóa hình ảnh",
        "dialog_delete_image_msg" to "Bạn có chắc chắn muốn xóa ảnh này?",

        // Empty states
        "empty_dish_list" to "Không tìm thấy món ăn phù hợp với điều kiện tìm kiếm/lọc.",
        "empty_images" to "Món ăn chưa có hình ảnh nào.",

        // Validation & Errors
        "online_status" to "Đang kết nối - Tự động đồng bộ",
        "offline_status" to "Chế độ ngoại tuyến - Đã lưu nội bộ",
        "msg_synced" to "Đã tự động đồng bộ với MockAPI!",
        "err_name_empty" to "Tên món ăn không được để trống.",
        "err_price_invalid" to "Giá bán phải lớn hơn 0.",
        "err_prep_time_invalid" to "Thời gian chế biến phải lớn hơn 0.",
        "err_category_invalid" to "Vui lòng chọn loại món ăn hợp lệ.",
        "err_name_duplicate" to "Tên món ăn đã tồn tại trong cùng loại món này.",
        "err_category_in_use" to "Không thể xóa loại món đang có món ăn sử dụng.",
        "err_dish_not_found" to "Món ăn không tồn tại hoặc đã bị xóa.",
        "err_price_range_invalid" to "Khoảng giá không hợp lệ (Giá tối thiểu lớn hơn giá tối đa).",
        "err_image_url_empty" to "Đường dẫn URL ảnh không được rỗng.",
        "msg_dish_saved" to "Lưu món ăn thành công!",
        "msg_dish_deleted" to "Đã xóa món ăn thành công!",
        "msg_thumbnail_updated" to "Đã cập nhật ảnh đại diện!"
    )
}
