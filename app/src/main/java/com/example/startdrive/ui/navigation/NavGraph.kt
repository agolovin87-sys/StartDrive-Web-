package com.example.startdrive.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.startdrive.ui.auth.AuthViewModel
import com.example.startdrive.ui.auth.LoginScreen
import com.example.startdrive.ui.auth.PendingApprovalScreen
import com.example.startdrive.ui.auth.RegisterScreen
import com.example.startdrive.ui.panels.AdminPanel
import com.example.startdrive.ui.panels.CadetPanel
import com.example.startdrive.ui.panels.InstructorPanel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object PendingApproval : Screen("pending_approval")
    data object Admin : Screen("admin")
    data object Instructor : Screen("instructor")
    data object Cadet : Screen("cadet")
}

@Composable
fun StartDriveNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.uiState.collectAsState()
    val userId by authViewModel.currentUserId.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                uiState = authState,
                onSignIn = { email, pass, stayInApp -> authViewModel.signIn(email, pass, stayInApp) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onClearError = { authViewModel.clearError() },
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                uiState = authState,
                onRegister = { name, email, phone, pass, role ->
                    authViewModel.register(name, email, phone, pass, role)
                },
                onNavigateBack = { navController.popBackStack() },
                onClearError = { authViewModel.clearError() },
            )
        }
        composable(Screen.Admin.route) {
            val user = authState.user
            if (user != null) {
                AdminPanel(
                    currentUser = user,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
        composable(Screen.Instructor.route) {
            val user = authState.user
            if (user != null) {
                InstructorPanel(
                    currentUser = user,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    onRefreshUser = { authViewModel.refreshUser() },
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
        composable(Screen.Cadet.route) {
            val user = authState.user
            if (user != null) {
                CadetPanel(
                    currentUser = user,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    onRefreshUser = { authViewModel.refreshUser() },
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
        composable(Screen.PendingApproval.route) {
            PendingApprovalScreen(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                onCheckAgain = { authViewModel.refreshUser() },
            )
        }
    }

    androidx.compose.runtime.LaunchedEffect(userId, authState.user) {
        if (userId == null) return@LaunchedEffect
        val user = authViewModel.uiState.value.user ?: return@LaunchedEffect
        when (user.role) {
            "admin" -> navController.navigate(Screen.Admin.route) { popUpTo(0) { inclusive = true } }
            "instructor" -> if (user.isActive) navController.navigate(Screen.Instructor.route) { popUpTo(0) { inclusive = true } }
                else navController.navigate(Screen.PendingApproval.route) { popUpTo(0) { inclusive = true } }
            "cadet" -> if (user.isActive) navController.navigate(Screen.Cadet.route) { popUpTo(0) { inclusive = true } }
                else navController.navigate(Screen.PendingApproval.route) { popUpTo(0) { inclusive = true } }
        }
    }
}
