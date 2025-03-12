package com.evanmccormick.chessevaluator.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NavBarIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
){
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ){
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}