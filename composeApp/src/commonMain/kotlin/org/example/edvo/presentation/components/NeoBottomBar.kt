package org.example.edvo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState

@Composable
fun NeoBottomBar(
    items: List<NavigationItem>,
    selectedItem: NavigationItem,
    onItemSelect: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOf(selectedItem).takeIf { it != -1 } ?: 1
    
    Box(
        modifier = modifier
            .padding(bottom = 24.dp)
            .width(320.dp) // Increased width
            .height(64.dp)
            .background(NeoPaletteV2.SurfacePrimary, RoundedCornerShape(32.dp))
            .border(1.dp, NeoPaletteV2.AccentWhite, RoundedCornerShape(32.dp))
            .padding(4.dp) // Padding for the pill to float inside
    ) {
        // Animated White Pill Indicator
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / items.size
            val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                )
            )
            
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(NeoPaletteV2.AccentWhite)
            )
            
            // Icons Layer
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { onItemSelect(item) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icon color transitions based on selection (Black if on white pill, Grey otherwise)
                        val targetColor = if (isSelected) NeoPaletteV2.SurfacePrimary else NeoPaletteV2.Functional.TextSecondary
                        val animatedColor by androidx.compose.animation.animateColorAsState(targetColor)
                        
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = animatedColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)
