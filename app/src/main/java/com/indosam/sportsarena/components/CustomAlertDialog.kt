package com.indosam.sportsarena.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun CustomAlertDialog(
    showDialog: MutableState<Boolean>,
    title: String,
    text: String,
    confirmText: String = "Yes",
    secondaryButtonText: String? = null,
    dismissText: String = "No",
    onConfirm: () -> Unit,
    onSecondary: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
                onDismiss()
            },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    showDialog.value = false
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                if (secondaryButtonText != null && onSecondary != null) {
                    TextButton(onClick = {
                        onSecondary()
                        showDialog.value = false
                    }) {
                        Text(secondaryButtonText)
                    }
                } else {
                    TextButton(onClick = {
                        showDialog.value = false
                        onDismiss()
                    }) {
                        Text(dismissText)
                    }
                }
            }
        )
    }
}