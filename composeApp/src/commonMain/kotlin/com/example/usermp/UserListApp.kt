package com.example.usermp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.usermp.shared.User
import com.example.usermp.shared.UsersPresenter
import com.example.usermp.shared.UsersUiState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.compose.koinInject

@Composable
fun UserListApp() {
    val presenter = koinInject<UsersPresenter>()
    val state by presenter.state.collectAsState()

    DisposableEffect(presenter) {
        onDispose {
            presenter.close()
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            UsersScreen(
                state = state,
                onRefresh = presenter::refresh,
            )
        }
    }
}

@Composable
private fun UsersScreen(
    state: UsersUiState,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Users",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }

        state.infoMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        when {
            state.isLoading && state.users.isEmpty() -> FullscreenMessage {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Loading users...")
            }

            state.errorMessage != null -> state.errorMessage?.let { errorMessage ->
                FullscreenMessage {
                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            state.isEmpty -> FullscreenMessage {
                Text("No users available.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.users,
                        key = { user -> user.id },
                    ) { user ->
                        UserCard(user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

@Composable
private fun UserCard(user: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                url = user.avatarUrl,
                fallback = initialsFor(user),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun Avatar(
    url: String,
    fallback: String,
) {
    val client = koinInject<HttpClient>()
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = url) {
        value = runCatching {
            val bytes = client.get(url).body<ByteArray>()
            decodeImageBitmap(bytes)
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fallback,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun initialsFor(user: User): String {
    return buildString {
        user.firstName.firstOrNull()?.let(::append)
        user.lastName.firstOrNull()?.let(::append)
    }
}
