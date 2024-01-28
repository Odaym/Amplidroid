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
import androidx.compose.ui.platform.LocalContext
import com.airbeamtv.amplidroid.ui.theme.AmplidroidTheme
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

private fun fetchAuthSession(){
    Amplify.Auth.fetchAuthSession(
        { Log.i("AmplifyQuickstart", "Auth session = $it") },
        { error -> Log.e("AmplifyQuickstart", "Failed to fetch auth session", error) }
    )
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
            {
                onItemSaved(item)
            },
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
    dialogTitle: String,
    dialogText: String
) {
    AlertDialog(
        icon = {
            Icon(Icons.Filled.Check, contentDescription = null)
        },
        title = {
            Text(text = dialogTitle)
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