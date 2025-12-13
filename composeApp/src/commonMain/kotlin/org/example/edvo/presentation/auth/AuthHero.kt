package org.example.edvo.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.edvo.presentation.designsystem.NeoTypographyV2
import org.jetbrains.compose.resources.painterResource
import edvo.composeapp.generated.resources.Res
import edvo.composeapp.generated.resources.edvo_base_logo

@Composable
fun AuthHero(
    isCompact: Boolean, 
    pulseScale: Float
) {
    AnimatedContent(
        targetState = isCompact,
        transitionSpec = {
            // Fade + slide + size interpolation for smooth layout transition
            // Fast fade (150ms) for snappier response
            (fadeIn(tween(150)) + slideInVertically { -it / 4 }) togetherWith
            (fadeOut(tween(150)) + slideOutVertically { it / 4 }) using
            SizeTransform(clip = false) // Animate size change smoothly
        },
        label = "HeroTransition"
    ) { compact ->
        if (compact) {
            // Horizontal Compact Layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(Res.drawable.edvo_base_logo),
                    contentDescription = "EDVO Logo",
                    modifier = Modifier
                        .size(48.dp) // Significantly reduced size
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("EDVO", style = NeoTypographyV2.Header().copy(fontSize = 36.sp)) // 50% bigger compact title
            }
        } else {
            // Vertical Expanded Layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(Res.drawable.edvo_base_logo),
                    contentDescription = "EDVO Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("EDVO", style = NeoTypographyV2.Header().copy(fontSize = 32.sp))
            }
        }
    }
}
