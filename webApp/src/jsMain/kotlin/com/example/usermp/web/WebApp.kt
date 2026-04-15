package com.example.usermp.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.usermp.shared.PlatformConfiguration
import com.example.usermp.shared.User
import com.example.usermp.shared.UsersPresenter
import com.example.usermp.shared.getSharedDependency
import com.example.usermp.shared.initKoin
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

fun main() {
    initKoin(PlatformConfiguration())

    renderComposable(rootElementId = "root") {
        WebApp()
    }
}

@Composable
private fun WebApp() {
    val presenter = getSharedDependency<UsersPresenter>()
    val state by presenter.state.collectAsState()

    DisposableEffect(presenter) {
        onDispose {
            presenter.close()
        }
    }

    Div({
        style {
            fontFamily("Inter", "Arial", "sans-serif")
            maxWidth(900.px)
            margin(24.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
                marginBottom(16.px)
                property("align-items", "center")
            }
        }) {
            H1({ style { margin(0.px) } }) {
                Text("Users")
            }
            Button(
                attrs = {
                    style {
                        padding(10.px, 16.px)
                    }
                    onClick { presenter.refresh() }
                },
            ) {
                Text("Refresh")
            }
        }

        state.infoMessage?.let { info ->
            P({ style { color(Color("#0b57d0")) } }) {
                Text(info)
            }
        }

        when {
            state.isLoading && state.users.isEmpty() -> P { Text("Loading users...") }
            state.errorMessage != null -> P { Text(state.errorMessage ?: "") }
            state.isEmpty -> P { Text("No users available.") }
            else -> UserList(state.users)
        }
    }
}

@Composable
private fun UserList(users: List<User>) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(12.px)
        }
    }) {
        users.forEach { user ->
            UserCard(user)
        }
    }
}

@Composable
private fun UserCard(user: User) {
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(16.px)
            padding(16.px)
            property("align-items", "center")
            border {
                width = 1.px
                style = LineStyle.Solid
                color = Color("#e0e0e0")
            }
            borderRadius(16.px)
        }
    }) {
        Img(
            src = user.avatarUrl,
            alt = user.fullName,
            attrs = {
                style {
                    width(64.px)
                    property("height", "64px")
                    property("border-radius", "999px")
                    property("object-fit", "cover")
                    property("background", "#f1f3f4")
                }
            },
        )
        Div {
            P({
                style {
                    margin(0.px)
                    fontSize(20.px)
                    property("font-weight", "600")
                }
            }) {
                Text(user.fullName)
            }
            P({ style { margin(0.px) } }) {
                Text(user.email)
            }
        }
    }
}
