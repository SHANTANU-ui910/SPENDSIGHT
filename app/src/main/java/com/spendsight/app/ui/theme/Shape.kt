package com.spendsight.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SpendSightShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp), // 24dp rounded cards as required
    extraLarge = RoundedCornerShape(32.dp)
)

val CardShape24 = RoundedCornerShape(24.dp)
val SheetShapeTop28 = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val ChipShape16 = RoundedCornerShape(16.dp)
val KeypadButtonShape18 = RoundedCornerShape(18.dp)
val PillShapeCircle = RoundedCornerShape(50)

