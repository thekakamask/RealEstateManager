package com.dcac.realestatemanager.ui.initialLoginPage.accountScreen

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.initialLoginPage.LoginUiState
import com.dcac.realestatemanager.ui.initialLoginPage.LoginViewModel

@Composable
fun ForgotPasswordPage(
    viewModel: LoginViewModel = hiltViewModel(),
    onBackClick:() -> Unit,
){

    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf(TextFieldValue()) }
    val isFormValid = EMAIL_ADDRESS
        .matcher(email.text.trim())
        .matches()

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
                    contentDescription = stringResource(R.string.forgot_password_button_content_description),
                    tint = MaterialTheme.colorScheme.onBackground)
            }

            // Title centered
            Text(
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.forgot_password_subtitle),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.forgot_password_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(
            R.string.forgot_password_email_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(stringResource(
                R.string.forgot_password_email_content),
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.sendPasswordResetEmail(email.text.trim()) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        ) {
            Text(stringResource(
                R.string.forgot_password_button))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is LoginUiState.Loading -> {
                    Text(
                        text = stringResource(R.string.reset_email_sending),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                is LoginUiState.Success -> {
                    Text(
                        text = stringResource(R.string.reset_email_send),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is LoginUiState.Error -> {
                    Text(
                        text = stringResource(
                            (uiState as LoginUiState.Error).messageResId
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> Unit
            }
        }
    }
}