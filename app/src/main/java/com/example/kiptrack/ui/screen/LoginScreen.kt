package com.example.kiptrack.ui.screen

import androidx.compose.animation.Crossfade
import com.example.kiptrack.R
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.* import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.UserAdmin
import com.example.kiptrack.ui.data.UserMahasiswa
import com.example.kiptrack.ui.data.UserWali
import com.example.kiptrack.ui.event.LoginEvent
import com.example.kiptrack.ui.state.LoginUiState
import com.example.kiptrack.ui.theme.BackgroundGradientStart
import com.example.kiptrack.ui.theme.DeepPurple
import com.example.kiptrack.ui.theme.LightPurple
import com.example.kiptrack.ui.theme.MediumPurple
import com.example.kiptrack.ui.theme.TextLabelColor
import com.example.kiptrack.ui.viewmodel.LoginViewModel

enum class UserRole(val label: String) {
    MAHASISWA("MAHASISWA"),
    ORANG_TUA("ORANG TUA/WALI")
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState

    Crossfade(
        targetState = state.isGeneralLogin,
        animationSpec = tween(durationMillis = 600),
        label = "LoginTransition",
        modifier = modifier
    ) { isGeneral ->
        if (isGeneral) {
            GeneralLoginScreen(
                state = state,
                onEvent = viewModel::onEvent
            )
        } else {
            AdminLoginScreen(
                state = state,
                onEvent = viewModel::onEvent
            )
        }
    }
}

// General Login UI
@Composable
fun GeneralLoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MediumPurple)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Logo & Welcome
            Spacer(modifier = Modifier.height(60.dp))
            Icon(
                painter = painterResource(id = R.drawable.kiptrack),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to KIP Track",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextLabelColor
            )
            Spacer(modifier = Modifier.height(30.dp))

            // White Container with Rounded Top
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = LightPurple
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        UserRole.values().forEach { role ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onEvent(LoginEvent.OnRoleSelected(role)) }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = role.label,
                                    color = if (state.selectedRole == role) DeepPurple else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Underline indicator
                                if (state.selectedRole == role) {
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(3.dp)
                                            .background(DeepPurple)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Inputs
                    val idLabel = if (state.selectedRole == UserRole.MAHASISWA) "NIM" else "ID Wali"
                    val idPlaceholder = if (state.selectedRole == UserRole.MAHASISWA) "Masukkan NIM Anda" else "Masukkan ID Anda"

                    LoginForm(
                        idLabel = idLabel,
                        idPlaceholder = idPlaceholder,
                        inputId = state.inputId,
                        password = state.password,
                        onIdChange = { onEvent(LoginEvent.OnIdChange(it)) },
                        onPasswordChange = { onEvent(LoginEvent.OnPasswordChange(it)) },
                        errorMessage = state.errorMessage
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Login Button
                    PurpleButton(text = "LOGIN", onClick = { onEvent(LoginEvent.OnLoginClicked) })

                    Spacer(modifier = Modifier.weight(1f))

                    // Admin Access Button (Circle)
                    OutlinedIconButton(
                        onClick = { onEvent(LoginEvent.OnToggleAdminMode) },
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, DeepPurple, CircleShape),
                        colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = DeepPurple)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.adminbutton),
                            contentDescription = "Admin Login",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// Admin Login
@Composable
fun AdminLoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MediumPurple, BackgroundGradientStart)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo
            Icon(
                painter = painterResource(id = R.drawable.kiptrack),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Login sebagai Admin",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DeepPurple
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Inputs
            LoginForm(
                idLabel = "Username",
                idPlaceholder = "Masukkan Username Admin",
                inputId = state.inputId,
                password = state.password,
                onIdChange = { onEvent(LoginEvent.OnIdChange(it)) },
                onPasswordChange = { onEvent(LoginEvent.OnPasswordChange(it)) },
                errorMessage = state.errorMessage
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PurpleButton(
                    text = "BACK",
                    onClick = { onEvent(LoginEvent.OnToggleAdminMode) },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    isPrimary = false
                )
                PurpleButton(
                    text = "LOGIN",
                    onClick = { onEvent(LoginEvent.OnLoginClicked) },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        }

        // Bottom Wave Decoration
        WaveShape(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(150.dp)
        )
    }
}

// Reusable Components
@Composable
fun LoginForm(
    idLabel: String,
    idPlaceholder: String,
    inputId: String,
    password: String,
    onIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$idLabel :",
            color = DeepPurple,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        CustomTextField(
            value = inputId,
            onValueChange = onIdChange,
            placeholder = idPlaceholder
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Password :",
            color = DeepPurple,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        CustomTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Masukkan Password Anda",
            isPassword = true
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, color = Color.LightGray, fontSize = 14.sp)
        },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun PurpleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(50)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) MediumPurple else Color(0xFFE1BEE7)
        ),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 50.dp)
    ) {
        Text(
            text = text,
            color = if (isPrimary) TextLabelColor else TextLabelColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun WaveShape(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            // Start from bottom left
            moveTo(0f, size.height)
            // Go to bottom right
            lineTo(size.width, size.height)
            // Go up to the right side (start of wave)
            lineTo(size.width, size.height * 0.5f)

            // Create the wave curve
            quadraticBezierTo(
                size.width * 0.75f, size.height * 0.2f, // Control point
                size.width * 0.5f, size.height * 0.7f   // Mid point
            )
            quadraticBezierTo(
                size.width * 0.25f, size.height * 1.1f, // Control point
                0f, size.height * 0.6f                  // End point (left side)
            )
            close()
        }
        drawPath(
            path = path,
            color = Color(0xFFF3E5F5).copy(alpha = 0.5f) // Very light purple/white
        )
    }
}