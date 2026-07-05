package com.surfschool.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SurfPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(SurfSchoolPaddings.Large * 2),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfSchoolColors.Primary,
            disabledContainerColor = SurfSchoolColors.Surface
        )
    ) {
        Text(
            text = text,
            style = SurfSchoolTypography.Body.copy(
                color = if (enabled) SurfSchoolColors.Background else SurfSchoolColors.TextSecondary
            )
        )
    }
}

@Composable
fun SurfErrorSnackbar(
    message: String,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(SurfSchoolPaddings.Medium),
        containerColor = SurfSchoolColors.Error,
        contentColor = SurfSchoolColors.Background
    ) {
        Text(
            text = message,
            style = SurfSchoolTypography.Body
        )
    }
}

@Composable
fun SurfAlertDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        modifier = modifier,
        containerColor = SurfSchoolColors.Surface,
        titleContentColor = SurfSchoolColors.TextMain,
        textContentColor = SurfSchoolColors.TextMain,
        title = {
            Text(text = title, style = SurfSchoolTypography.H2)
        },
        text = {
            Text(text = text, style = SurfSchoolTypography.Body)
        },
        confirmButton = {
            SurfPrimaryButton(
                text = confirmButtonText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            if (dismissButtonText != null && onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissButtonText,
                        style = SurfSchoolTypography.Body.copy(color = SurfSchoolColors.TextSecondary)
                    )
                }
            }
        }
    )
}
