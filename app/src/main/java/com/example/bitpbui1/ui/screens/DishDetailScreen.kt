package com.example.bitpbui1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bitpbui1.data.model.DishImage
import com.example.bitpbui1.i18n.AppLanguage
import com.example.bitpbui1.i18n.Strings
import com.example.bitpbui1.ui.viewmodel.DishViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    dishId: Long,
    viewModel: DishViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val dish = viewModel.getDishById(dishId)
    val categoryName = if (dish != null) viewModel.getCategoryName(dish.dishCategoryId) else ""

    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showAddImageDialog by remember { mutableStateOf(false) }
    var newImageUrlInput by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msgKey ->
            snackbarHostState.showSnackbar(Strings.get(msgKey, lang))
            viewModel.clearUserMessage()
        }
    }

    if (dish == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(Strings.get("screen_dish_detail", lang)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.get("err_dish_not_found", lang),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("screen_dish_detail", lang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Language Toggle
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (lang == AppLanguage.EN) "🌐 EN" else "🌐 VI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { onNavigateToEdit(dish.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Main Selected Image Preview (4.3)
            val currentImages = dish.images
            val safeIndex = if (selectedImageIndex in currentImages.indices) selectedImageIndex else 0
            val activeImage: DishImage? = currentImages.getOrNull(safeIndex)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            ) {
                if (activeImage != null && activeImage.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = activeImage.imageUrl,
                        contentDescription = dish.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                }

                // Main Thumbnail Badge on image preview
                if (activeImage?.isThumbnail == true) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Strings.get("thumbnail_label", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image Thumbnails Carousel & Add Image Button (4.3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Strings.get("lbl_image_urls", lang)} (${currentImages.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                TextButton(onClick = { showAddImageDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("btn_add_image", lang))
                }
            }

            if (currentImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentImages.size) { index ->
                        val img = currentImages[index]
                        val isSelected = index == safeIndex

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedImageIndex = index }
                        ) {
                            AsyncImage(
                                model = img.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (img.isThumbnail) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.Yellow,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }

                // Image Actions: Set Thumbnail / Delete Image
                activeImage?.let { img ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!img.isThumbnail) {
                            OutlinedButton(
                                onClick = { viewModel.setThumbnailImage(dish.id, img.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Strings.get("btn_set_thumbnail", lang), fontSize = 12.sp)
                            }
                        }

                        if (currentImages.size > 1) {
                            OutlinedButton(
                                onClick = { viewModel.deleteImage(img.id, dish.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Strings.get("btn_delete_image", lang), fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = Strings.get("empty_images", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Dish Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Category & Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = categoryName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Status Tag (Selling / Stopped)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (dish.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (dish.isAvailable) Strings.get("status_available", lang) else Strings.get("status_unavailable", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dish.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = dish.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val numberFormat = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
                    Text(
                        text = "${numberFormat.format(dish.price)} VNĐ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prep Time Tag (4.3 requirement)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (dish.isQuickPreparation) Icons.Default.ElectricBolt else Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (dish.isQuickPreparation) Color(0xFF2E7D32) else Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${Strings.get("lbl_prep_time", lang)}: ${dish.preparationTime} ${if (lang == AppLanguage.EN) "mins" else "phút"} ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (dish.isQuickPreparation) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = if (dish.isQuickPreparation) Strings.get("quick_prep", lang) else Strings.get("regular_prep", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dish.isQuickPreparation) Color(0xFF2E7D32) else Color(0xFF1565C0),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = Strings.get("lbl_description", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dish.description.ifBlank { "N/A" },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Add Image URL Dialog
    if (showAddImageDialog) {
        AlertDialog(
            onDismissRequest = { showAddImageDialog = false },
            title = { Text(Strings.get("btn_add_image", lang)) },
            text = {
                OutlinedTextField(
                    value = newImageUrlInput,
                    onValueChange = { newImageUrlInput = it },
                    label = { Text(Strings.get("lbl_enter_image_url", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newImageUrlInput.isNotBlank()) {
                            viewModel.addImageToDish(dish.id, newImageUrlInput)
                            newImageUrlInput = ""
                            showAddImageDialog = false
                        }
                    }
                ) {
                    Text(Strings.get("btn_confirm", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddImageDialog = false }) {
                    Text(Strings.get("btn_cancel", lang))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(Strings.get("dialog_delete_dish_title", lang)) },
            text = { Text(Strings.get("dialog_delete_dish_msg", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDish(dish.id)
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Strings.get("btn_delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(Strings.get("btn_cancel", lang))
                }
            }
        )
    }
}
