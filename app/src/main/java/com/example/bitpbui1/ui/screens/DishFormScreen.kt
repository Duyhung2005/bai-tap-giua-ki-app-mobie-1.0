package com.example.bitpbui1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitpbui1.i18n.AppLanguage
import com.example.bitpbui1.i18n.Strings
import com.example.bitpbui1.ui.viewmodel.DishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishFormScreen(
    dishId: Long = 0,
    viewModel: DishViewModel,
    onNavigateBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val existingDish = remember(dishId) {
        if (dishId > 0) viewModel.getDishById(dishId) else null
    }

    var nameInput by remember { mutableStateOf(existingDish?.name ?: "") }
    var priceInput by remember { mutableStateOf(existingDish?.price?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var prepTimeInput by remember { mutableStateOf(existingDish?.preparationTime?.toString() ?: "") }
    var isAvailableInput by remember { mutableStateOf(existingDish?.isAvailable ?: true) }
    var descriptionInput by remember { mutableStateOf(existingDish?.description ?: "") }
    var selectedCategoryId by remember { mutableLongStateOf(existingDish?.dishCategoryId ?: (categories.firstOrNull()?.id ?: 0L)) }

    val imageUrlsList = remember {
        mutableStateListOf<String>().apply {
            if (existingDish != null && existingDish.images.isNotEmpty()) {
                addAll(existingDish.images.map { it.imageUrl })
            } else {
                add("") // Start with one empty input field
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msgKey ->
            snackbarHostState.showSnackbar(Strings.get(msgKey, lang))
            viewModel.clearUserMessage()
        }
    }

    val isEditMode = dishId > 0
    val screenTitle = if (isEditMode) Strings.get("screen_edit_dish", lang) else Strings.get("screen_add_dish", lang)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (lang == AppLanguage.EN) "🌐 EN" else "🌐 VI",
                            fontWeight = FontWeight.Bold
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Name Field
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(Strings.get("lbl_dish_name", lang)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                val currentCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: ""
                OutlinedTextField(
                    value = currentCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(Strings.get("lbl_category", lang)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId = cat.id
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Price Field
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it },
                label = { Text(Strings.get("lbl_price", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Preparation Time Field
            OutlinedTextField(
                value = prepTimeInput,
                onValueChange = { prepTimeInput = it },
                label = { Text(Strings.get("lbl_prep_time", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Availability Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Strings.get("lbl_status", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isAvailableInput) Strings.get("status_available", lang) else Strings.get("status_unavailable", lang),
                            fontSize = 12.sp,
                            color = if (isAvailableInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Switch(
                        checked = isAvailableInput,
                        onCheckedChange = { isAvailableInput = it }
                    )
                }
            }

            // Description Field
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text(Strings.get("lbl_description", lang)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Image URLs List Inputs (For New Dish or Adding Images)
            if (!isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("lbl_image_urls", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = { imageUrlsList.add("") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add image input")
                    }
                }

                imageUrlsList.forEachIndexed { index, url ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { imageUrlsList[index] = it },
                            label = { Text("${Strings.get("lbl_enter_image_url", lang)} ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        if (imageUrlsList.size > 1) {
                            IconButton(onClick = { imageUrlsList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove url field", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val validUrls = imageUrlsList.map { it.trim() }.filter { it.isNotEmpty() }
                    val success = viewModel.saveDish(
                        dishId = dishId,
                        name = nameInput,
                        priceText = priceInput,
                        prepTimeText = prepTimeInput,
                        isAvailable = isAvailableInput,
                        description = descriptionInput,
                        categoryId = selectedCategoryId,
                        imageUrls = validUrls
                    )
                    if (success) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = Strings.get("btn_save", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
