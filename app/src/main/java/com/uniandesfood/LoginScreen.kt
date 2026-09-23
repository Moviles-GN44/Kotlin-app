package com.uniandesfood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.AuthUiState
import com.uniandesfood.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel? = null,
    onLoginSuccess: () -> Unit = {}
) {
    val uiState = viewModel?.uiState?.collectAsState()?.value ?: AuthUiState()
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = BackgroundOffWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo Icon & Title
            Surface(
                color = UniandesAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile_filled),
                        contentDescription = "Uniandes Food Logo",
                        tint = UniandesAmber,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Uniandes Food",
                style = MaterialTheme.typography.headlineLarge,
                color = ShadowGrey
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Campus Dining & Smart Food Directory",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Card Container for Form
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.isRegistering) "Create Student Account" else "Student Sign In",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShadowGrey
                    )

                    // Display Name (when registering)
                    AnimatedVisibility(visible = uiState.isRegistering) {
                        Column {
                            OutlinedTextField(
                                value = uiState.displayName,
                                onValueChange = { viewModel?.onDisplayNameChange(it) },
                                label = { Text("Full Name", style = MaterialTheme.typography.bodySmall) },
                                placeholder = { Text("Samuel Carrillo", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = UniandesAmber,
                                    focusedLabelColor = UniandesAmber
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Email Input Field
                    Column {
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel?.onEmailChange(it) },
                            label = { Text("University Email", style = MaterialTheme.typography.bodySmall) },
                            placeholder = { Text("s.carrillo@uniandes.edu.co", style = MaterialTheme.typography.bodySmall) },
                            isError = uiState.emailError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UniandesAmber,
                                focusedLabelColor = UniandesAmber,
                                errorBorderColor = StatusLongRed
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.emailError ?: " ",
                                color = StatusLongRed,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${uiState.email.length}/50",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            )
                        }
                    }

                    // Password Input Field
                    Column {
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel?.onPasswordChange(it) },
                            label = { Text("Password", style = MaterialTheme.typography.bodySmall) },
                            placeholder = { Text("Min. 6 characters", style = MaterialTheme.typography.bodySmall) },
                            isError = uiState.passwordError != null,
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel?.submit()
                                }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Text(
                                        text = if (isPasswordVisible) "Hide" else "Show",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UniandesAmber
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UniandesAmber,
                                focusedLabelColor = UniandesAmber,
                                errorBorderColor = StatusLongRed
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.passwordError ?: " ",
                                color = StatusLongRed,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${uiState.password.length}/32",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            )
                        }
                    }

                    // General Error Banner if any
                    if (uiState.generalError != null) {
                        Surface(
                            color = StatusLongRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.generalError ?: "",
                                color = StatusLongRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Primary Action Button (Sign In / Register)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel?.submit()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = ShadowGrey,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (uiState.isRegistering) "Register & Start" else "Sign In & Explore Campus",
                                style = MaterialTheme.typography.labelLarge,
                                color = ShadowGrey
                            )
                        }
                    }

                    // Toggle Register / Login mode
                    TextButton(
                        onClick = { viewModel?.toggleAuthMode() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (uiState.isRegistering) "Already registered? Sign In" else "New student? Create an account",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick bypass for testing / guest
            TextButton(
                onClick = onLoginSuccess
            ) {
                Text(
                    text = "Continue as Guest Student →",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = UniandesAmber
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    UniandesFoodTheme {
        LoginScreen()
    }
}
