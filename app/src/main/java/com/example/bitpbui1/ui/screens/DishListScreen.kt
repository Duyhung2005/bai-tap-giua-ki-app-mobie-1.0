package com.example.bitpbui1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bitpbui1.data.model.Dish
import com.example.bitpbui1.i18n.AppLanguage
import com.example.bitpbui1.i18n.Strings
import com.example.bitpbui1.ui.viewmodel.DishViewModel
import com.example.bitpbui1.ui.viewmodel.SortOption
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishListScreen(
    viewModel: DishViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val dishes by viewModel.filteredDishes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val selectedAvail by viewModel.selectedAvailability.collectAsState()
    val minPrice by viewModel.minPriceText.collectAsState()
    val maxPrice by viewModel.maxPriceText.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    var showFilterPanel by remember { mutableStateOf(false) }
    var dishToDelete by remember { mutableStateOf<Dish?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msgKey ->
            snackbarHostState.showSnackbar(Strings.get(msgKey, lang))
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Strings.get("app_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = Strings.get("student_info", lang),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Language Switcher Toggle
                    TextButton(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.EN) "🌐 EN" else "🌐 VI",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(Strings.get("btn_add_dish", lang)) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            // Network Connectivity Status Banner (Section 5)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOnline) Strings.get("online_status", lang) else Strings.get("offline_status", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar & Filter Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text(Strings.get("search_hint", lang)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showFilterPanel = !showFilterPanel },
                    modifier = Modifier
                        .background(
                            if (showFilterPanel || selectedCategory != null || selectedAvail != null || minPrice.isNotEmpty() || maxPrice.isNotEmpty())
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (showFilterPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable Filter & Sort Panel
            AnimatedVisibility(visible = showFilterPanel) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.get("filter_category", lang) + " & " + Strings.get("filter_status", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            TextButton(onClick = { viewModel.resetFilters() }) {
                                Text(Strings.get("filter_reset", lang))
                            }
                        }

                        // Category Filter Dropdown
                        var catExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = !catExpanded }
                        ) {
                            OutlinedTextField(
                                value = categories.firstOrNull { it.id == selectedCategory }?.name ?: Strings.get("all_categories", lang),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(Strings.get("filter_category", lang)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = catExpanded,
                                onDismissRequest = { catExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(Strings.get("all_categories", lang)) },
                                    onClick = {
                                        viewModel.onCategoryFilterChanged(null)
                                        catExpanded = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            viewModel.onCategoryFilterChanged(cat.id)
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Filter (All / Available / Unavailable)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedAvail == null,
                                onClick = { viewModel.onAvailabilityFilterChanged(null) },
                                label = { Text(Strings.get("status_all", lang)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedAvail == true,
                                onClick = { viewModel.onAvailabilityFilterChanged(true) },
                                label = { Text(Strings.get("status_available", lang)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedAvail == false,
                                onClick = { viewModel.onAvailabilityFilterChanged(false) },
                                label = { Text(Strings.get("status_unavailable", lang)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Price Range Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = minPrice,
                                onValueChange = { viewModel.onPriceRangeChanged(it, maxPrice) },
                                label = { Text(Strings.get("price_min", lang)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxPrice,
                                onValueChange = { viewModel.onPriceRangeChanged(minPrice, it) },
                                label = { Text(Strings.get("price_max", lang)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sort Dropdown
                        var sortExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sortExpanded,
                            onExpandedChange = { sortExpanded = !sortExpanded }
                        ) {
                            OutlinedTextField(
                                value = when (sortOption) {
                                    SortOption.DEFAULT -> Strings.get("sort_default", lang)
                                    SortOption.PRICE_ASC -> Strings.get("sort_price_asc", lang)
                                    SortOption.PRICE_DESC -> Strings.get("sort_price_desc", lang)
                                    SortOption.PREP_TIME_ASC -> Strings.get("sort_prep_time_asc", lang)
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(Strings.get("sort_by", lang)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(Strings.get("sort_default", lang)) },
                                    onClick = {
                                        viewModel.onSortOptionChanged(SortOption.DEFAULT)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(Strings.get("sort_price_asc", lang)) },
                                    onClick = {
                                        viewModel.onSortOptionChanged(SortOption.PRICE_ASC)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(Strings.get("sort_price_desc", lang)) },
                                    onClick = {
                                        viewModel.onSortOptionChanged(SortOption.PRICE_DESC)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(Strings.get("sort_prep_time_asc", lang)) },
                                    onClick = {
                                        viewModel.onSortOptionChanged(SortOption.PREP_TIME_ASC)
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dish Count Header
            Text(
                text = "${dishes.size} ${Strings.get("screen_dish_list", lang).lowercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (dishes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.get("empty_dish_list", lang),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(dishes, key = { it.id }) { dish ->
                        DishItemCard(
                            dish = dish,
                            categoryName = viewModel.getCategoryName(dish.dishCategoryId),
                            lang = lang,
                            onClick = { onNavigateToDetail(dish.id) },
                            onEdit = { onNavigateToEdit(dish.id) },
                            onDelete = { dishToDelete = dish }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    dishToDelete?.let { dish ->
        AlertDialog(
            onDismissRequest = { dishToDelete = null },
            title = { Text(Strings.get("dialog_delete_dish_title", lang)) },
            text = { Text("${Strings.get("dialog_delete_dish_msg", lang)}\n\n\"${dish.name}\"") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDish(dish.id)
                        dishToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Strings.get("btn_delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { dishToDelete = null }) {
                    Text(Strings.get("btn_cancel", lang))
                }
            }
        )
    }
}

@Composable
fun DishItemCard(
    dish: Dish,
    categoryName: String,
    lang: AppLanguage,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val formattedPrice = "${numberFormat.format(dish.price)} VNĐ"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                val thumbUrl = dish.thumbnailImage?.imageUrl
                if (!thumbUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = thumbUrl,
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
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Dish Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dish.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = categoryName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formattedPrice,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Prep Time Tag: Quick / Regular (4.3)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (dish.isQuickPreparation) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = if (dish.isQuickPreparation)
                                "⚡ ${Strings.get("quick_prep", lang)} (${dish.preparationTime}m)"
                            else
                                "⏳ ${Strings.get("regular_prep", lang)} (${dish.preparationTime}m)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dish.isQuickPreparation) Color(0xFF2E7D32) else Color(0xFF1565C0),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Availability Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (dish.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = if (dish.isAvailable) Strings.get("available_badge", lang) else Strings.get("unavailable_badge", lang),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dish.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Actions: Edit & Delete
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
