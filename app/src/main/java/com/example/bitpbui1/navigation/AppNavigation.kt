package com.example.bitpbui1.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitpbui1.ui.screens.DishDetailScreen
import com.example.bitpbui1.ui.screens.DishFormScreen
import com.example.bitpbui1.ui.screens.DishListScreen
import com.example.bitpbui1.ui.viewmodel.DishViewModel

object Screen {
    const val DISH_LIST = "dish_list"
    const val DISH_DETAIL = "dish_detail/{dishId}"
    const val DISH_ADD = "dish_add"
    const val DISH_EDIT = "dish_edit/{dishId}"

    fun createDetailRoute(dishId: Long) = "dish_detail/$dishId"
    fun createEditRoute(dishId: Long) = "dish_edit/$dishId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: DishViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.DISH_LIST
    ) {
        composable(Screen.DISH_LIST) {
            DishListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { dishId ->
                    navController.navigate(Screen.createDetailRoute(dishId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.DISH_ADD)
                },
                onNavigateToEdit = { dishId ->
                    navController.navigate(Screen.createEditRoute(dishId))
                }
            )
        }

        composable(
            route = Screen.DISH_DETAIL,
            arguments = listOf(
                navArgument("dishId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val dishId = backStackEntry.arguments?.getLong("dishId") ?: 0L
            DishDetailScreen(
                dishId = dishId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.createEditRoute(id))
                }
            )
        }

        composable(Screen.DISH_ADD) {
            DishFormScreen(
                dishId = 0L,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DISH_EDIT,
            arguments = listOf(
                navArgument("dishId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val dishId = backStackEntry.arguments?.getLong("dishId") ?: 0L
            DishFormScreen(
                dishId = dishId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
