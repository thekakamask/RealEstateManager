package com.dcac.realestatemanager.ui.initialLoginPage.accountScreen

import android.util.Patterns
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.initialLoginPage.LoginUiState
import com.dcac.realestatemanager.ui.initialLoginPage.LoginViewModel

@Composable
fun AccountCreationPage(
    viewModel: LoginViewModel = hiltViewModel(),
    onAccountCreationSuccess:() -> Unit,
    onBackClick:() -> Unit,
) {

    var email by remember { mutableStateOf(TextFieldValue()) }
    var agentName by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmationPassword by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    val isFormValid = Patterns.EMAIL_ADDRESS.matcher(email.text).matches()
            && agentName.text.isNotBlank()
            && password.text.length >= 6
            && confirmationPassword.text.isNotBlank()
            && password == confirmationPassword
            && uiState !is LoginUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onAccountCreationSuccess()
        }
    }

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
                    contentDescription = stringResource(R.string.account_creation_button_content_description),
                    tint = MaterialTheme.colorScheme.secondary)
            }

            // Title centered
            Text(
                text = stringResource(R.string.account_creation_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.account_creation_subtitle),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.account_creation_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(
            R.string.account_creation_email_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(stringResource(
                R.string.account_creation_email_content),
                color = MaterialTheme.colorScheme.tertiary
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(
            R.string.account_creation_agent_name_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
        OutlinedTextField(
            value = agentName,
            onValueChange = { agentName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(stringResource(
                    R.string.account_creation_agent_name_content),
                color = MaterialTheme.colorScheme.tertiary
            )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(
            R.string.account_creation_password_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(stringResource(
                    R.string.account_creation_password_content),
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(
            R.string.account_creation_password_confirmation_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            value = confirmationPassword,
            onValueChange = { confirmationPassword = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(
                stringResource(
                    R.string.account_creation_password_content),
                color = MaterialTheme.colorScheme.tertiary) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top =8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (password != confirmationPassword && confirmationPassword.text.isNotBlank()) {
                Text(
                    text = stringResource(R.string.passwords_do_not_match),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.signUp(
                    email = email.text.trim(),
                    password = password.text.trim(),
                    agentName = agentName.text.trim()
                )
            },
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
            Text(stringResource(R.string.account_creation_button))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is LoginUiState.Loading -> {
                    Text(
                        text = stringResource(R.string.account_creation_in_progress),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is LoginUiState.Error -> {
                    val messageResId = (uiState as LoginUiState.Error).messageResId
                    Text(
                        text = stringResource(messageResId),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> Unit
            }
        }

    }
}
