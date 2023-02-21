package codes.chrishorner.personasns

import android.app.Activity
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.BlurMaskFilter.Blur.NORMAL
import android.inputmethodservice.Keyboard.Row
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scaleMatrix
import androidx.core.view.WindowCompat
import codes.chrishorner.personasns.R.font
import codes.chrishorner.personasns.ui.theme.PersonaRed

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      RootContainer {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(color = PersonaRed)
        ) {
          Image(
            painter = painterResource(R.drawable.bg_splatter_background),
            contentDescription = null,
            modifier = Modifier
              .statusBarsPadding()
              .offset(y = (-16).dp)
              .graphicsLayer {
              }
          )
          Image(
            painter = painterResource(R.drawable.logo_im),
            contentDescription = null,
            modifier = Modifier
              .statusBarsPadding()
              .height(100.dp)
              .offset(x = 8.dp, y = (-4).dp),
          )

          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {

            val scale by rememberInfiniteTransition().animateFloat(
              initialValue = 1f,
              targetValue = 1f,
              animationSpec = infiniteRepeatable(
                tween(durationMillis = 1_000),
                repeatMode = RepeatMode.Reverse
              ),
            )

            Column {
              Text(
                text = "And how's that any different\nthan usual?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontFamily = OptimaNova,
                modifier = Modifier
                  .drawWithCache {
                    val outerBoxStem = Outline(OuterBoxStem())
                    val outerBoxShape = OuterBox()
                    val outerBox = Outline(outerBoxShape)
                    val innerBoxStem = Outline(InnerBoxStem())
                    val innerBox = Outline(InnerBox())
                    val shadowPaint = Paint().apply {
                      color = Color.Black
                      alpha = 0.6f
                      asFrameworkPaint().maskFilter = BlurMaskFilter(2.dp.toPx(), NORMAL)
                    }

                    onDrawBehind {
                      //scale(scale, pivot = Offset(18.dp.toPx(), size.center.y + 16.dp.toPx())) {
                        drawIntoCanvas { it.drawOutline(outerBox, shadowPaint) }
                        drawOutline(outerBox, color = Color.White)
                      //}
                      drawOutline(outerBoxStem, color = Color.White)
                      drawOutline(innerBoxStem, color = Color.Black)
                      //scale(scale, pivot = Offset(18.dp.toPx(), size.center.y + 16.dp.toPx())) {
                        drawOutline(innerBox, color = Color.Black)
                      //}
                    }
                  }
                  .padding(start = 58.dp, top = 24.dp, end = 62.dp, bottom = 26.dp)
              )
            }
          }
        }
      }
    }
  }
}

private val OptimaNova = FontFamily(
  Font(font.optima_nova_black, weight = FontWeight.Black)
)

@Composable
private fun Avatar() {

}

@Composable
private fun RootContainer(content: @Composable () -> Unit) {
  val view = LocalView.current
  val window = (view.context as Activity).window
  SideEffect {
    window.statusBarColor = Color.Black.copy(alpha = 0.3f).toArgb()
    window.navigationBarColor = Color.Transparent.toArgb()
  }
  content()
}

private fun DrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

private fun CacheDrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

@Composable
private fun OuterBox(): Shape {
  return LocalDensity.current.OuterBox()
}

private fun Density.OuterBox(): Shape {
  return GenericShape { size, _ ->
    moveTo(42.dp.toPx(), 5.dp.toPx())
    lineTo(size.width, 0f)
    lineTo(size.width - 35.dp.toPx(), size.height)
    lineTo(18.dp.toPx(), size.height - 13.dp.toPx())
    close()
  }
}

private fun Density.InnerBox(): Shape {
  return GenericShape { size, _ ->
    moveTo(47.dp.toPx(), 12.dp.toPx())
    lineTo(size.width - 18.dp.toPx(), 6.dp.toPx())
    lineTo(size.width - 40.dp.toPx(), size.height - 6.dp.toPx())
    lineTo(27.dp.toPx(), size.height - 20.dp.toPx())
    close()
  }
}

private fun Density.OuterBoxStem(): Shape = GenericShape { size, _ ->
  moveTo(0f, size.center.y + 10.dp.toPx())
  lineTo(26.dp.toPx(), size.center.y - 14.5.dp.toPx())
  lineTo(28.dp.toPx(), size.center.y - 7.dp.toPx())
  lineTo(43.dp.toPx(), size.center.y - 17.dp.toPx())
  lineTo(35.5.dp.toPx(), size.center.y + 14.dp.toPx())
  lineTo(16.dp.toPx(), size.center.y + 18.dp.toPx())
  lineTo(13.5.dp.toPx(), size.center.y + 8.5.dp.toPx())
  close()
}

private fun Density.InnerBoxStem(): Shape = GenericShape { size, _ ->
  moveTo(6.dp.toPx(), size.center.y + 5.5.dp.toPx())
  lineTo(23.dp.toPx(), size.center.y + -9.dp.toPx())
  lineTo(26.dp.toPx(), size.center.y - 2.5.dp.toPx())
  lineTo(42.dp.toPx(), size.center.y - 9.dp.toPx())
  lineTo(36.dp.toPx(), size.center.y + 7.dp.toPx())
  lineTo(19.dp.toPx(), size.center.y + 10.dp.toPx())
  lineTo(17.dp.toPx(), size.center.y + 1.dp.toPx())
  close()
}

private fun Density.AvatarColoredBox(): Shape = GenericShape { size, _ ->
  moveTo(0f, 0f)
  lineTo(92.5.dp.toPx(), 3.dp.toPx())
  lineTo(106.5.dp.toPx(), 50.5.dp.toPx())
  lineTo(23.dp.toPx(), 63.dp.toPx())
  close()
}

private fun Density.AvatarWhiteBox
