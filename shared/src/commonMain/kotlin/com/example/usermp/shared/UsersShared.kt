package com.example.usermp.shared

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.example.usermp.db.AppDatabase
import com.example.usermp.db.Users
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarUrl: String,
) {
    val fullName: String = "$firstName $lastName"
}

interface UsersApi {
    suspend fun fetchUsers(): List<User>
}

class DummyJsonUsersApi(
    private val httpClient: HttpClient,
) : UsersApi {
    override suspend fun fetchUsers(): List<User> {
        val response = httpClient.get(USERS_ENDPOINT).body<UsersResponseDto>()
        return response.users.map { dto ->
            User(
                id = dto.id,
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                avatarUrl = dto.image,
            )
        }
    }

    private companion object {
        const val USERS_ENDPOINT = "https://dummyjson.com/users"
    }
}

interface UserLocalDataSource {
    suspend fun getUsers(): List<User>
    suspend fun replaceUsers(users: List<User>)
}

class SqlDelightUserLocalDataSource(
    private val databaseProvider: DatabaseProvider,
    private val ioDispatcher: CoroutineDispatcher,
) : UserLocalDataSource {
    override suspend fun getUsers(): List<User> = withContext(ioDispatcher) {
        val queries = databaseProvider.getDatabase().usersQueries
        queries.selectAll().awaitAsList().map { cachedUser: Users ->
            User(
                id = cachedUser.id.toInt(),
                firstName = cachedUser.first_name,
                lastName = cachedUser.last_name,
                email = cachedUser.email,
                avatarUrl = cachedUser.avatar_url,
            )
        }
    }

    override suspend fun replaceUsers(users: List<User>) = withContext(ioDispatcher) {
        val queries = databaseProvider.getDatabase().usersQueries
        queries.deleteAll()
        users.forEach { user ->
            queries.insertUser(
                id = user.id.toLong(),
                first_name = user.firstName,
                last_name = user.lastName,
                email = user.email,
                avatar_url = user.avatarUrl,
            )
        }
    }
}

interface UsersRepository {
    suspend fun refresh(): UsersLoadResult
}

sealed interface UsersLoadResult {
    data class Data(
        val users: List<User>,
        val isFromCache: Boolean,
        val message: String? = null,
    ) : UsersLoadResult

    data class Error(
        val message: String,
    ) : UsersLoadResult
}

class DefaultUsersRepository(
    private val api: UsersApi,
    private val localDataSource: UserLocalDataSource,
) : UsersRepository {
    override suspend fun refresh(): UsersLoadResult {
        return try {
            val remoteUsers = api.fetchUsers()
            localDataSource.replaceUsers(remoteUsers)
            UsersLoadResult.Data(
                users = remoteUsers,
                isFromCache = false,
            )
        } catch (exception: Exception) {
            if (exception is CancellationException) {
                throw exception
            }

            val cachedUsers = localDataSource.getUsers()
            if (cachedUsers.isNotEmpty()) {
                UsersLoadResult.Data(
                    users = cachedUsers,
                    isFromCache = true,
                    message = "Showing cached users because the latest refresh failed.",
                )
            } else {
                UsersLoadResult.Error(
                    message = "Unable to load users right now. Please try again.",
                )
            }
        }
    }
}

data class UsersUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
) {
    val isEmpty: Boolean = !isLoading && users.isEmpty() && errorMessage == null
}

class UsersPresenter(
    private val repository: UsersRepository,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + coroutineDispatcher)
    private val _state = MutableStateFlow(UsersUiState())
    private var refreshJob: Job? = null

    val state: StateFlow<UsersUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = scope.launch {
            val previousUsers = _state.value.users
            _state.value = UsersUiState(
                isLoading = true,
                users = previousUsers,
            )

            _state.value = when (val result = repository.refresh()) {
                is UsersLoadResult.Data -> UsersUiState(
                    isLoading = false,
                    users = result.users,
                    infoMessage = result.message,
                )

                is UsersLoadResult.Error -> UsersUiState(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}

class DatabaseProvider(
    private val databaseFactory: DatabaseFactory,
) {
    private val mutex = Mutex()
    private var database: AppDatabase? = null

    suspend fun getDatabase(): AppDatabase {
        return mutex.withLock {
            database?.let { return it }
            databaseFactory.create().also { created ->
                database = created
            }
        }
    }
}

@Serializable
private data class UsersResponseDto(
    val users: List<UserDto>,
)

@Serializable
private data class UserDto(
    val id: Int,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String,
    val email: String,
    val image: String,
)
