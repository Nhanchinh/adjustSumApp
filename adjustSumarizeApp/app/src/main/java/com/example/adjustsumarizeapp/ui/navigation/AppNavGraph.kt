package com.example.adjustsumarizeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.adjustsumarizeapp.ui.screen.home.HomeScreen
import com.example.adjustsumarizeapp.ui.screen.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    
    fun homeWithArgs(userName: String) = "home?userName=$userName"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(route = Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen
        composable(
            route = "${Routes.HOME}?userName={userName}",
            arguments = listOf(
                navArgument("userName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            HomeScreen()
        }
        
        // Simple Home route without arguments
        composable(route = Routes.HOME) {
            HomeScreen()
        }
    }
}
