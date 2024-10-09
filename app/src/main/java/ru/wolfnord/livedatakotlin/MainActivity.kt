package ru.wolfnord.livedatakotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.wolfnord.livedatakotlin.ui.theme.LiveDataKotlinTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveDataKotlinTheme {
                val database = AppDatabase.getDatabase(LocalContext.current)
                Display(database)
            }
        }
    }
}

suspend fun fetchUsers(database: AppDatabase): List<User>? {
    return withContext(Dispatchers.IO) {
        val response = RetrofitInstance.api.getUsers().execute()
        if (response.isSuccessful) {
            response.body()?.let { users ->
                database.userDao().insertUsers(users)
            }
            response.body()
        } else {
            null
        }
    }
}

@Composable
fun Display(database: AppDatabase) {
    var users by remember { mutableStateOf<List<User>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        users = fetchUsers(database)
        if (users == null) {
            users = database.userDao().getUsers()
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) { // Используем Box как контейнер
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            users?.let {
                LazyColumn {
                    items(it) { user ->
                        Text(text = "${user.name} - ${user.email}", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}
