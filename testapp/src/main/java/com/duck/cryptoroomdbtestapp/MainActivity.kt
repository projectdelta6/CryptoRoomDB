package com.duck.cryptoroomdbtestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duck.cryptoroomdbtestapp.data.model.UserDisplayModel
import com.duck.cryptoroomdbtestapp.ui.UserViewModel
import com.duck.cryptoroomdbtestapp.ui.theme.CryptoRoomDbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoRoomDbTheme {
                val userViewModel: UserViewModel = viewModel(factory = viewModelFactory {
                    initializer { UserViewModel(application) }
                })
                UserListScreen(userViewModel)
            }
        }
    }
}

@Composable
fun UserListScreen(userViewModel: UserViewModel) {
    val users by userViewModel.users.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editUser by remember { mutableStateOf<UserDisplayModel?>(null) }

    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(
                    items = users,
                    key = { it.id } // Use unique key for each item
                ) { user ->
                    UserRow(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        user = user,
                        onEdit = { editUser = it },
                        onDelete = { userViewModel.deleteUser(it.id) }
                    )
                }
                item(
                    key = "add_user_button" // Unique key for the add button item
                ) {
                    Button(
                        modifier = Modifier
                            .animateItem(),
                        onClick = { showAddDialog = true }
                    ) {
                        Text("Add User")
                    }
                }
            }
        }
        if (showAddDialog) {
            UserDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, email, age, secret ->
                    userViewModel.addUser(name, email, age, secret)
                    showAddDialog = false
                }
            )
        }
        if (editUser != null) {
            UserDialog(
                user = editUser,
                onDismiss = { editUser = null },
                onConfirm = { name, email, age, secret ->
                    userViewModel.updateUser(editUser!!.id, name, email, age, secret)
                    editUser = null
                }
            )
        }
    }
}

@Composable
fun UserRow(
    user: UserDisplayModel,
    onEdit: (UserDisplayModel) -> Unit,
    onDelete: (UserDisplayModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text("Name: ${user.name}")
        Text("Email: ${user.email}")
        Text("Age: ${user.age}")
        Text("Decrypted Secret: ${user.decryptedSecret}")
        Text("Encrypted Secret: ${user.encryptedSecret}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEdit(user) }) { Text("Edit") }
            Button(onClick = { onDelete(user) }) { Text("Delete") }
        }
    }
}

@Composable
fun UserDialog(
    user: UserDisplayModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var age by remember { mutableStateOf(user?.age?.toString() ?: "") }
    var secret by remember { mutableStateOf(user?.decryptedSecret ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Add User" else "Edit User") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("Secret") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && age.isNotBlank() && secret.isNotBlank()) {
                        onConfirm(name, email, age.toIntOrNull() ?: 0, secret)
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
