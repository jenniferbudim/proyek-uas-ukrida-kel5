package com.example.kiptrack.ui.screen // <-- ADDED/FIXED PACKAGE DECLARATION

import androidx.compose.animation.Crossfade
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

// --- Data Classes (Integrated from request) ---

data class UserMahasiswa(
    val nim: String,
    val nama: String,
    val password: String
)

data class UserWali(
    val id: String,
    val nama: String,
    val password: String
)

data class UserAdmin(
    val id: String,
    val username: String,
    val password: String
)

// --- Color Palette based on images ---
val LightPurple = Color(0xFFF3E5F5)
val MediumPurple = Color(0xFFCE93D8)
val DeepPurple = Color(0xFF9575CD)
val TextLabelColor = Color(0xFF7E57C2)
val BackgroundGradientStart = Color(0xFFD1C4E9)
val BackgroundGradientEnd = Color(0xFFEDE7F6)

// --- MVVM: State ---
data class LoginUiState(
    val isGeneralLogin: Boolean = true, // true = General, false = Admin
    val selectedRole: UserRole = UserRole.MAHASISWA,
    val inputId: String = "", // Represents NIM for Mahasiswa, ID for Wali, Username for Admin
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class UserRole(val label: String) {
    MAHASISWA("MAHASISWA"),
    ORANG_TUA("ORANG TUA/WALI")
}

// --- MVVM: Events ---
sealed interface LoginEvent {
    data class OnIdChange(val value: String) : LoginEvent
    data class OnPasswordChange(val value: String) : LoginEvent
    data class OnRoleSelected(val role: UserRole) : LoginEvent
    object OnToggleAdminMode : LoginEvent
    object OnLoginClicked : LoginEvent
}

// --- MVVM: ViewModel ---
class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnIdChange -> {
                uiState = uiState.copy(inputId = event.value, errorMessage = null)
            }
            is LoginEvent.OnPasswordChange -> {
                uiState = uiState.copy(password = event.value, errorMessage = null)
            }
            is LoginEvent.OnRoleSelected -> {
                uiState = uiState.copy(selectedRole = event.role, inputId = "", password = "", errorMessage = null)
            }
            is LoginEvent.OnToggleAdminMode -> {
                // Switch screens and reset inputs
                uiState = uiState.copy(
                    isGeneralLogin = !uiState.isGeneralLogin,
                    inputId = "",
                    password = "",
                    errorMessage = null
                )
            }
            is LoginEvent.OnLoginClicked -> {
                performLogin()
            }
        }
    }

    private fun performLogin() {
        val currentId = uiState.inputId
        val currentPassword = uiState.password

        if (currentId.isBlank() || currentPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "Form tidak boleh kosong")
            return
        }

        uiState = uiState.copy(isLoading = true)

        // Simulating data processing
        if (uiState.isGeneralLogin) {
            when (uiState.selectedRole) {
                UserRole.MAHASISWA -> {
                    println("Login Request: Mahasiswa (NIM: $currentId)")
                    val potentialUser = UserMahasiswa(nim = currentId, nama = "Mahasiswa Test", password = currentPassword)
                }
                UserRole.ORANG_TUA -> {
                    println("Login Request: Wali (ID: $currentId)")
                    val potentialUser = UserWali(id = currentId, nama = "Wali Test", password = currentPassword)
                }
            }
        } else {
            println("Login Request: Admin (Username: $currentId)")
            val potentialUser = UserAdmin(id = "admin_01", username = currentId, password = currentPassword)
        }

        // Reset loading for demo
        uiState = uiState.copy(isLoading = false)
    }
}

// --- Main Screen Composable ---
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier, // <--- ADD THIS
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

// --- General Login UI (First Image) ---
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
                tint = Color.White
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
                            imageVector = Icons.Filled.Settings,
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

// --- Admin Login UI (Second Image) ---
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
                imageVector = Icons.Filled.Face,
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                tint = Color.White
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

// --- Reusable Components ---

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