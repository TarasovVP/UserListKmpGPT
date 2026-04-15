package com.example.usermp.shared

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UsersRepositoryTest {
    @Test
    fun repositoryReturnsRemoteUsersAndUpdatesCache() = runTest {
        val remoteUsers = listOf(
            User(1, "Ada", "Lovelace", "ada@example.com", "https://example.com/ada.png"),
        )
        val local = FakeLocalDataSource()
        val repository = DefaultUsersRepository(
            api = FakeUsersApi { remoteUsers },
            localDataSource = local,
        )

        val result = repository.refresh()

        val data = assertIs<UsersLoadResult.Data>(result)
        assertEquals(remoteUsers, data.users)
        assertEquals(remoteUsers, local.storedUsers)
        assertTrue(!data.isFromCache)
    }

    @Test
    fun repositoryReturnsCachedUsersWhenRemoteFails() = runTest {
        val cachedUsers = listOf(
            User(7, "Grace", "Hopper", "grace@example.com", "https://example.com/grace.png"),
        )
        val local = FakeLocalDataSource(cachedUsers.toMutableList())
        val repository = DefaultUsersRepository(
            api = FakeUsersApi { error("boom") },
            localDataSource = local,
        )

        val result = repository.refresh()

        val data = assertIs<UsersLoadResult.Data>(result)
        assertEquals(cachedUsers, data.users)
        assertTrue(data.isFromCache)
        assertTrue(data.message?.contains("cached users") == true)
    }

    @Test
    fun repositoryReturnsErrorWhenRemoteFailsAndCacheIsEmpty() = runTest {
        val repository = DefaultUsersRepository(
            api = FakeUsersApi { error("boom") },
            localDataSource = FakeLocalDataSource(),
        )

        val result = repository.refresh()

        val error = assertIs<UsersLoadResult.Error>(result)
        assertTrue(error.message.contains("Unable to load users"))
    }

    @Test
    fun presenterExposesStaleDataMessage() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = object : UsersRepository {
            override suspend fun refresh(): UsersLoadResult {
                return UsersLoadResult.Data(
                    users = listOf(
                        User(3, "Linus", "Torvalds", "linus@example.com", "https://example.com/linus.png"),
                    ),
                    isFromCache = true,
                    message = "Showing cached users because the latest refresh failed.",
                )
            }
        }

        val presenter = UsersPresenter(
            repository = repository,
            coroutineDispatcher = dispatcher,
        )

        advanceUntilIdle()

        assertEquals(1, presenter.state.value.users.size)
        assertNull(presenter.state.value.errorMessage)
        assertTrue(presenter.state.value.infoMessage?.contains("cached users") == true)
    }
}

private class FakeUsersApi(
    private val block: suspend () -> List<User>,
) : UsersApi {
    override suspend fun fetchUsers(): List<User> = block()
}

private class FakeLocalDataSource(
    initialUsers: MutableList<User> = mutableListOf(),
) : UserLocalDataSource {
    val storedUsers: MutableList<User> = initialUsers

    override suspend fun getUsers(): List<User> = storedUsers.toList()

    override suspend fun replaceUsers(users: List<User>) {
        storedUsers.clear()
        storedUsers.addAll(users)
    }
}
