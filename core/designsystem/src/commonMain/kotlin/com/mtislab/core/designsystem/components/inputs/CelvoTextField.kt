package com.mtislab.core.designsystem.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.theme.extended

@Composable
fun CelvoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    forceUppercase: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val actualValue = if (forceUppercase) value.uppercase() else value
    val actualOnValueChange: (String) -> Unit = {
        onValueChange(if (forceUppercase) it.uppercase() else it)
    }

    val actualKeyboardOptions = if (forceUppercase) {
        keyboardOptions.copy(capitalization = KeyboardCapitalization.Characters)
    } else {
        keyboardOptions
    }

    OutlinedTextField(
        value = actualValue,
        onValueChange = actualOnValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.extended.textPrimary
        ),
        label = label?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.extended.textTertiary
                )
            }
        },
        isError = isError,
        supportingText = errorMessage?.let {
            if (isError) {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else null
        },
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = actualKeyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.extended.inputBackground,
            unfocusedContainerColor = MaterialTheme.colorScheme.extended.inputBackground,
            disabledContainerColor = MaterialTheme.colorScheme.extended.inputBackground,
            errorContainerColor = MaterialTheme.colorScheme.extended.inputBackground,

            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,

            focusedTextColor = MaterialTheme.colorScheme.extended.textPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.extended.textPrimary,
            errorTextColor = MaterialTheme.colorScheme.extended.textPrimary,

            cursorColor = MaterialTheme.colorScheme.primary,
        )
    )
}