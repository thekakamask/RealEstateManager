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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dcac.realestatemanager.R

@Composable
fun AccountCreationPage(
    onAccountCreationSuccess:() -> Unit,
    onBackClick:() -> Unit,
) {

    var email by remember { mutableStateOf(TextFieldValue()) }
    var agentName by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmationPassword by remember { mutableStateOf(TextFieldValue()) }

    val isFormValid = email.text.isNotBlank()
            && agentName.text.isNotBlank()
            && password.text.isNotBlank()
            && confirmationPassword.text.isNotBlank()
            && password == confirmationPassword

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
            color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(stringResource(
                R.string.account_creation_email_content),
                color = MaterialTheme.colorScheme.tertiary) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(
            R.string.account_creation_agent_name_label),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            value = agentName,
            onValueChange = { agentName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(
                stringResource(
                    R.string.account_creation_agent_name_content),
                color = MaterialTheme.colorScheme.tertiary) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        var passwordVisible by remember { mutableStateOf(false) }

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
            placeholder = { Text(
                stringResource(
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAccountCreationSuccess() },
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
    }
}
