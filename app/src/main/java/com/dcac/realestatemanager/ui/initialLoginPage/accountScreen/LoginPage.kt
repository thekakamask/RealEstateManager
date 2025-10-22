package com.dcac.realestatemanager.ui.initialLoginPage.accountScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dcac.realestatemanager.R

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit,
    onInfoClick: () -> Unit,
    onBackClick:() -> Unit,
    onPasswordForgotClick: () -> Unit
) {

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    val isFormValid = email.text.isNotBlank() && password.text.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Icon back (left aligned)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.login_back_button_content_description),
                    tint = MaterialTheme.colorScheme.secondary)
            }

            // Title centered
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.Center),
            )

            Text(
                text = stringResource(R.string.login_help_title_button),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Blue,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onInfoClick() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(
            R.string.login_email_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(stringResource(
                R.string.login_email_content),
                color = MaterialTheme.colorScheme.tertiary) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        var passwordVisible by remember { mutableStateOf(false) }

        // Subject Field
        Text(text = stringResource(
            R.string.login_password_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(
                stringResource(
                    R.string.login_password_content),
                color = MaterialTheme.colorScheme.tertiary) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.login_forgot_password_content),
            color = Color.Blue,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { onPasswordForgotClick() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginSuccess() },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.login_log_in_button))
        }

    }
}