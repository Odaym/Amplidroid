package com.airbeamtv.amplidroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbeamtv.amplidroid.ui.theme.AmplidroidTheme
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.datastore.generated.model.Todo
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val openAlertDialog = remember { mutableStateOf(false) }

            val dialogTitle = remember { mutableStateOf("") }
            val dialogText = remember { mutableStateOf("") }

            AmplidroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AddTodoItemButton {
                            dialogTitle.value = Operation.SAVE.name
                            dialogText.value = "$it"

                            openAlertDialog.value = true
                        }

                        QueryTodoItemsButton {
                            dialogTitle.value = Operation.FETCH.name
                            dialogText.value = it

                            openAlertDialog.value = true
                        }

                        FetchAuthSessionButton {
                            dialogText.value = it

                            openAlertDialog.value = true
                        }

                        GetAccessCredentialsButton {
                            dialogText.value = it

                            openAlertDialog.value = true
                        }

                        RegisterUserPhoneNumber {
                            dialogText.value = it

                            openAlertDialog.value = true
                        }

                        SignInUser {
                            dialogText.value = it

                            openAlertDialog.value = true
                        }
                    }

                    if (openAlertDialog.value) {
                        OperationStatusDialog(
                            onConfirmation = { openAlertDialog.value = false },
                            dialogTitle = dialogTitle.value,
                            dialogText = dialogText.value
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FetchAuthSessionButton(
    onSessionFetched: (String) -> Unit
) {
    Button(onClick = {
        Amplify.Auth.fetchAuthSession(
            { onSessionFetched(if (it.isSignedIn) "Signed in!" else "Not signed in!") },
            { error -> Log.e("AmplifyQuickstart", "Failed to fetch auth session", error) }
        )
    }) {
        Text(text = "Fetch Auth Session")
    }
}

@Composable
fun RegisterUserPhoneNumber(
    onUserRegistered: (String) -> Unit
) {
    Button(onClick = {
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.phoneNumber(), "+31629378085")
            .build()

        Amplify.Auth.signUp("+31629378085", "Password123", options,
            {
                val result = StringBuilder()

                result.append(it.userId.toString())
                result.append("\n")
                result.append("Next step -> ${it.nextStep.signUpStep.name}")
                onUserRegistered(result.toString())

                Log.i("AuthQuickStart", "Sign up succeeded: $it")
            },
            { Log.e("AuthQuickStart", "Sign up failed", it) }
        )
    }) {
        Text(text = "Register +31629378085")
    }
}

@Composable
fun GetAccessCredentialsButton(
    onCredentialsReceived: (String) -> Unit
) {
    Button(onClick = {
        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                when (session.identityIdResult.type) {
                    AuthSessionResult.Type.SUCCESS -> {
                        onCredentialsReceived(session.identityIdResult.value ?: "ID was null")
                        Log.i("AuthQuickStart", "IdentityId = ${session.identityIdResult.value}")
                    }

                    AuthSessionResult.Type.FAILURE ->
                        Log.w(
                            "AuthQuickStart",
                            "IdentityId not found",
                            session.identityIdResult.error
                        )
                }
            },
            { Log.e("AuthQuickStart", "Failed to fetch session", it) }
        )
    }) {
        Text("Get Access Credentials")
    }
}

@Composable
fun SignInUser(
    onUserSignedIn: (String) -> Unit
) {
    Button(onClick = {
        Amplify.Auth.signIn("+31629378085", "Password123",
            { result ->
                if (result.isSignedIn) {
                    onUserSignedIn("Succeeded")
                    Log.i("AuthQuickstart", "Sign in succeeded")
                } else {
                    onUserSignedIn("Failed")
                    Log.i("AuthQuickstart", "Sign in not complete")
                }
            },
            { Log.e("AuthQuickstart", "Failed to sign in", it) }
        )
    }) {
        Text(text = "Sign in user")
    }
}

@Composable
fun AddTodoItemButton(
    onItemSaved: (Todo) -> Unit
) {
    Button(onClick = {
        val item = Todo.builder()
            .name("Task #${Random.nextInt()}")
            .priority(Priority.entries[Random.nextInt(3)])
            .build()

        Amplify.DataStore.save(item,
            { onItemSaved(item) },
            { Log.e("Tutorial", "Could not save item to DataStore", it) }
        )
    }) {
        Text("Add todo item")
    }
}

@Composable
fun QueryTodoItemsButton(
    onItemsFetched: (String) -> Unit
) {
    Button(onClick = {
        Amplify.DataStore.query(Todo::class.java,
            { todos ->
                val result = StringBuilder()

                result.append("==== Todo ====\n")

                while (todos.hasNext()) {
                    val todo: Todo = todos.next()

                    result.apply {
                        append("Name: ${todo.name}\n")
                        todo.priority?.let { prio ->
                            append(
                                "Priority: $prio\n"
                            )
                        }
                        todo.completedAt?.let { completedAt ->
                            append(
                                "CompletedAt: $completedAt\n"
                            )
                        }

                        append("\n")
                    }
                }

                onItemsFetched(result.toString())
            },
            { Log.e("Tutorial", "Could not query DataStore", it) }
        )
    }) {
        Text("Query Todo Items")
    }
}

@Composable
fun OperationStatusDialog(
    onConfirmation: () -> Unit,
    dialogTitle: String? = null,
    dialogText: String
) {
    AlertDialog(
        icon = {
            Icon(Icons.Filled.Check, contentDescription = null)
        },
        title = {
            if (dialogTitle != null) {
                Text(text = dialogTitle)
            }
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onConfirmation()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("OK")
            }
        }
    )
}

enum class Operation(name: String) {
    SAVE("Saved"),
    DELETE("Deleted"),
    MODIFY("Modified"),
    FETCH("Fetched")
}