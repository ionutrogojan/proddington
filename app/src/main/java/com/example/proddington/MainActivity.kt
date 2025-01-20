package com.example.proddington

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.proddington.ui.theme.ProddingtonTheme
import com.example.proddington.ui.theme.accentColor
import com.example.proddington.ui.theme.accentColor2
import com.example.proddington.ui.theme.accentColor3
import com.example.proddington.ui.theme.backgroundColor
import com.example.proddington.ui.theme.containerColor
import com.example.proddington.ui.theme.textColor
import com.example.proddington.ui.theme.textFocusColor

data class InputState(
    val reqMaterial: MutableState<String> = mutableStateOf(""),
    var remPallets: MutableState<String> = mutableStateOf(""),
    var remHours: MutableState<String> = mutableStateOf(""),
    var avgRollSize: MutableState<String> = mutableStateOf(""),
    var shiftHours: MutableState<String> = mutableStateOf("")
)

data class InputFocus(
    var reqMaterial: FocusRequester = FocusRequester(),
    var remPallets: FocusRequester = FocusRequester(),
    var remHours: FocusRequester = FocusRequester(),
    var avgRollSize: FocusRequester = FocusRequester(),
    var shiftHours: FocusRequester = FocusRequester()
)

data class CalcResult(
    val pph: MutableState<Double> = mutableDoubleStateOf(0.0),
    val pps: MutableState<Double> = mutableDoubleStateOf(0.0),
    val rph: MutableState<Double> = mutableDoubleStateOf(0.0),
    val rps: MutableState<Double> = mutableDoubleStateOf(0.0)
)

class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock the screen orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        enableEdgeToEdge()
        setContent {

            val states = InputState()
            val focuses = InputFocus()
            val results = CalcResult()

            val showResult = remember { mutableStateOf(false) }

            val keyboardController = LocalSoftwareKeyboardController.current

            ProddingtonTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() },
                                // God knows at this point. It works, but the padding is not going back to
                                // the navigationBars height after closing the keyboard because the
                                // animation is not done. So it goes down to 0.dp padding and it slides
                                // back to the navigationBars height as the keyboard closes.
                                bottom = if (!WindowInsets.isImeVisible) with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() } else 0.dp
                            )
                    ) {
                        // TODO: Somehow scroll into view the text field
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Banner(image = R.drawable.banner)
                            InText(
                                name = "Material Required",
                                state = states.reqMaterial,
                                focus = focuses.reqMaterial,
                                icon = R.drawable.req_material
                            )
                            InText(
                                name = "Remaining Pallets",
                                state = states.remPallets,
                                focus = focuses.remPallets,
                                icon = R.drawable.rem_pallets
                            )
                            InText(
                                name = "Remaining Hours",
                                state = states.remHours,
                                focus = focuses.remHours,
                                icon = R.drawable.rem_hours
                            )
                            InText(
                                name = "Average Roll Size",
                                state = states.avgRollSize,
                                focus = focuses.avgRollSize,
                                icon = R.drawable.avr_roll_size
                            )
                            InText(
                                name = "Shift Hours",
                                state = states.shiftHours,
                                focus = focuses.shiftHours,
                                icon = R.drawable.shift_hours
                            )
                            if (showResult.value) OutResult(results)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .imePadding()
                                .zIndex(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CalcButton(name = "Calculate",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                onClick = {
                                    when {
                                        states.reqMaterial.value.isEmpty() -> focuses.reqMaterial.requestFocus()
                                        states.remPallets.value.isEmpty() -> focuses.remPallets.requestFocus()
                                        states.remHours.value.isEmpty() -> focuses.remHours.requestFocus()
                                        states.avgRollSize.value.isEmpty() -> focuses.avgRollSize.requestFocus()
                                        states.shiftHours.value.isEmpty() -> focuses.shiftHours.requestFocus()
                                        else -> {
                                            keyboardController?.hide()
                                            calc(results, states)
                                            showResult.value = true
                                        }
                                    }
                                })
                            ResetButton(
                                onClick = {
                                    showResult.value = false
                                    focuses.reqMaterial.requestFocus()
                                    states.reqMaterial.value = ""
                                    states.remPallets.value = ""
                                    states.remHours.value = ""
                                    states.avgRollSize.value = ""
                                    states.shiftHours.value = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calc(results: CalcResult, states: InputState) {
    results.pph.value = states.remPallets.value.toDouble() / states.remHours.value.toDouble()
    results.rph.value = states.reqMaterial.value.toDouble() / states.remHours.value.toDouble()
    if (states.remHours.value.toDouble() < states.shiftHours.value.toDouble()) {
        results.pps.value = states.remPallets.value.toDouble()
        results.rps.value =
            (results.rph.value * states.remHours.value.toDouble()) / states.avgRollSize.value.toDouble()
    } else {
        results.pps.value = results.pph.value * states.shiftHours.value.toDouble()
        results.rps.value =
            (results.rph.value * states.shiftHours.value.toDouble()) / states.avgRollSize.value.toDouble()
    }
}

@Composable
fun Banner(image: Int) {
    Image(
        painter = painterResource(image),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun OutLabel(icon: Int, text: String) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = textColor,
        )
        Text(
            text = text,
            style = TextStyle(color = textColor),
        )
    }
}

@Composable
fun OutResult(results: CalcResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor3, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                OutLabel(icon = R.drawable.rem_pallets, text = "${"%.2f".format(results.pps.value)} Pallets")
                OutLabel(icon = R.drawable.hours, text = "${"%.2f".format(results.pph.value)}/h")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                OutLabel(icon = R.drawable.rolls, text = "${"%.2f".format(results.rps.value)} Rolls")
                OutLabel(icon = R.drawable.hours, text = "${"%.2f".format(results.rph.value)}kg/h")
            }
        }
    }
}

// TODO: Prevent keyboard paste suggestions
@Composable
fun InText(
    name: String,
    state: MutableState<String>,
    focus: FocusRequester,
    icon: Int
) {
    TextField(
        value = state.value,
        // This onValueChanged is so ChatGPTed that I don't even understand it yet,
        // but I will have to get back to it and figure it out
        // It prevents anything other than floating point numbers from being entered
        onValueChange = { input ->
            state.value = input.filterIndexed { index, c ->
                c.isDigit() || (c == '.' && !input.take(index).contains('.'))
            }
        },
        label = { Text(text = name) },
        leadingIcon = { Icon(painterResource(icon), contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            autoCorrect = false,
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.None,
            platformImeOptions = PlatformImeOptions()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focus),
        textStyle = TextStyle(color = textColor),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedLeadingIconColor = textColor,
            unfocusedLeadingIconColor = textColor,
            focusedLabelColor = textFocusColor,
            unfocusedLabelColor = textColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            cursorColor = accentColor,
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun ResetButton(onClick: () -> Unit) {
    Button(
        content = {
            Icon(
                painterResource(id = R.drawable.reset),
                contentDescription = null,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        },
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accentColor2,
            contentColor = textColor
        )
    )
}

@Composable
fun CalcButton(name: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        content = { Text(name, modifier = Modifier.padding(vertical = 8.dp)) },
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accentColor,
            contentColor = textColor
        )
    )
}