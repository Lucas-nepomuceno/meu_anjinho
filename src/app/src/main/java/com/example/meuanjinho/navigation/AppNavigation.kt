package com.example.meuanjinho.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.meuanjinho.R
import com.example.meuanjinho.database.AppDatabase
import com.example.meuanjinho.screens.AdicionarRegistroScreen
import com.example.meuanjinho.screens.CriancasScreen
import com.example.meuanjinho.screens.HomeScreen
import com.example.meuanjinho.screens.RegistrosScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    db: AppDatabase
) {

    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.home_24),
                            contentDescription = null
                        )
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == "registros",
                    onClick = { navController.navigate("registros") },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.list_alt_24),
                            contentDescription = null
                        )
                    },
                    label = { Text("Registros") }
                )

                NavigationBarItem(
                    selected = currentRoute == "criancas",
                    onClick = { navController.navigate("criancas") },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.child_care_24),
                            contentDescription = null
                        )
                    },
                    label = { Text("Crianças") }
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("home") {
                HomeScreen(navController)
            }

            composable("registros") {
                RegistrosScreen()
            }

            composable("criancas") {
                CriancasScreen(db = db)
            }

            composable("adicionar_registro") {
                AdicionarRegistroScreen(navController)
            }

        }
    }
}