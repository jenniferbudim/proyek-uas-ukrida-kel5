package com.example.kiptrack.ui.screen

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.R
import com.example.kiptrack.ui.event.LoginEvent
import com.example.kiptrack.ui.data.UserRole
import com.example.kiptrack.ui.state.LoginUiState
import com.example.kiptrack.ui.theme.Purple100
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.theme.PurplePrimary

import com.example.kiptrack.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (UserRole, String) -> Unit
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            onLoginSuccess(event.role, event.uid)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Crossfade(
        targetState = state.isGeneralLogin,
        animationSpec = tween(durationMillis = 600),
        label = "LoginTransition",
        modifier = modifier
    ) { isGeneral ->
        if (isGeneral) {
            GeneralLoginScreen(state = state, onEvent = viewModel::onEvent)
        } else {
            AdminLoginScreen(state = state, onEvent = viewModel::onEvent)
        }
    }
}

@Composable
fun GeneralLoginScreen(state: LoginUiState, onEvent: (LoginEvent) -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Purple100)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(60.dp))
            Icon(
                painter = painterResource(id = R.drawable.kiptrack),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Welcome to KIP Track", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            Spacer(modifier = Modifier.height(30.dp))
            Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp), color = Purple50) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        UserRole.values().filter { it != UserRole.ADMIN }.forEach { role ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                                .clickable { onEvent(LoginEvent.OnRoleSelected(role)) }
                                .padding(8.dp)) {
                                // 2. FIX: UserRole not selected to be colored Purple200
                                Text(text = role.label, color = if (state.selectedRole == role) PurplePrimary else Purple200, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.selectedRole == role) Box(modifier = Modifier
                                    .width(80.dp)
                                    .height(3.dp)
                                    .background(Purple200))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    val idLabel = if (state.selectedRole == UserRole.MAHASISWA) "NIM" else "ID Wali"
                    val idPlaceholder = if (state.selectedRole == UserRole.MAHASISWA) "Masukkan NIM Anda" else "Masukkan ID Anda"
                    LoginForm(idLabel = idLabel, idPlaceholder = idPlaceholder, inputId = state.inputId, password = state.password, onIdChange = { onEvent(LoginEvent.OnIdChange(it)) }, onPasswordChange = { onEvent(LoginEvent.OnPasswordChange(it)) }, isLoading = state.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                    PurpleButton(text = "LOGIN", onClick = { onEvent(LoginEvent.OnLoginClicked) }, isLoading = state.isLoading)
                    Spacer(modifier = Modifier.weight(1f))
                    // 2. FIX: Change icon.Settings to R.drawable.adminbutton
                    OutlinedIconButton(onClick = { onEvent(LoginEvent.OnToggleAdminMode) }, modifier = Modifier
                        .size(60.dp)
                        .border(1.dp, PurplePrimary, CircleShape), colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = PurplePrimary)) {
                        Icon(painter = painterResource(id = R.drawable.adminbutton), contentDescription = "Admin Login", modifier = Modifier.size(30.dp), tint = PurplePrimary)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun AdminLoginScreen(state: LoginUiState, onEvent: (LoginEvent) -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(brush = Brush.verticalGradient(colors = listOf(Purple200, Purple50)))) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(80.dp))
            Icon(painter = painterResource(id = R.drawable.kiptrack), contentDescription = "Logo", modifier = Modifier.size(100.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Login sebagai Admin", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            Spacer(modifier = Modifier.height(50.dp))
            LoginForm(idLabel = "Username", idPlaceholder = "Masukkan Username Admin", inputId = state.inputId, password = state.password, onIdChange = { onEvent(LoginEvent.OnIdChange(it)) }, onPasswordChange = { onEvent(LoginEvent.OnPasswordChange(it)) }, isLoading = state.isLoading)
            Spacer(modifier = Modifier.height(60.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PurpleButton(text = "BACK", onClick = { onEvent(LoginEvent.OnToggleAdminMode) }, modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp), isPrimary = false)
                PurpleButton(text = "LOGIN", onClick = { onEvent(LoginEvent.OnLoginClicked) }, modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp), isLoading = state.isLoading)
            }
        }
        WaveShape(modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(150.dp))
    }
}

@Composable
fun LoginForm(
    idLabel: String,
    idPlaceholder: String,
    inputId: String,
    password: String,
    onIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(text = "$idLabel :", color = PurplePrimary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        CustomTextField(value = inputId, onValueChange = onIdChange, placeholder = idPlaceholder, enabled = !isLoading)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Password :", color = PurplePrimary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

        CustomTextField(value = password, onValueChange = onPasswordChange, placeholder = "Masukkan Password Anda", isPassword = true, enabled = !isLoading)
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val visualTransformation = if (isPassword && !passwordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    val keyboardOptions = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password)
    } else {
        KeyboardOptions.Default
    }

    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
    val iconTint = Purple200.copy(alpha = 0.7f)

    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        // 3. FIX: text on the textfield to be colored Purple200
        textStyle = LocalTextStyle.current.copy(color = Purple200),
        placeholder = { Text(text = placeholder, color = Color.LightGray, fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,

        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,

        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = enabled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                        tint = iconTint
                    )
                }
            }
        }
    )
}

@Composable
fun PurpleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.shadow(4.dp, RoundedCornerShape(50)),
        colors = ButtonDefaults.buttonColors(containerColor = Purple200),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 50.dp)
    ) {
        if (isLoading) {
            // FIX: Remove Modifier.fillMaxSize() from the Box to prevent excessive vertical stretching.
            // The Box now just ensures the spinner is centered within the space normally taken by the text.
            Box(
                // wrapContentSize centers the content within the space allocated by the Button's contentPadding
                modifier = Modifier.wrapContentSize(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
            }
        } else {
            Text(text = text, color = PurplePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun WaveShape(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, size.height * 0.3f)
            quadraticBezierTo(size.width * 0.75f, size.height * 0.1f, size.width * 0.5f, size.height * 0.4f)
            quadraticBezierTo(size.width * 0.25f, size.height * 0.8f, 0f, size.height * 0.35f)
            close()
        }
        drawPath(path = path, color = Color.White.copy(alpha = 0.5f))
    }
}