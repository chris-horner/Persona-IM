package codes.chrishorner.personasns

import android.app.Activity
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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

            Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
              Entry(
                avatarImage = R.drawable.ann,
                color = Color(0xFFFE93C9),
                text = "And how's that any different than usual?",
              )
              Entry(
                avatarImage = R.drawable.ryuji,
                color = Color(0xFFF0EA40),
                text = "Shaddup! I mean I seriously can't today... I even tried.",
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
private fun Entry(
  @DrawableRes avatarImage: Int,
  color: Color,
  text: String,
) {
  Row(verticalAlignment = Alignment.Bottom) {
    Avatar(avatarImage, color)
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.White,
      fontFamily = OptimaNova,
      modifier = Modifier
        .offset(x = (-18).dp)
        .drawWithCache {
          val outerBoxStem = Outline(OuterBoxStem())
          val outerBoxShape = OuterBox()
          val outerBox = Outline(outerBoxShape)
          val innerBoxStem = Outline(InnerBoxStem())
          val innerBox = Outline(InnerBox())
          val shadowPaint = Paint().apply {
            this.color = Color.Black
            alpha = 0.3f
            asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
          }

          onDrawBehind {
            drawIntoCanvas { it.drawOutline(outerBox, shadowPaint) }
            drawOutline(outerBox, color = Color.White)
            drawOutline(outerBoxStem, color = Color.White)
            drawOutline(innerBoxStem, color = Color.Black)
            drawOutline(innerBox, color = Color.Black)
          }
        }
        .padding(start = 42.dp, top = 20.dp, end = 32.dp, bottom = 20.dp)
    )
  }
}

@Composable
private fun Avatar(@DrawableRes avatarImage: Int, color: Color) {
  Box(
    modifier = Modifier
      .width(110.dp)
      .height(90.dp)
      .drawBehind {
        drawOutline(Outline(AvatarBlackBox()), Color.Black)
        drawOutline(Outline(AvatarWhiteBox()), Color.White)
        drawOutline(Outline(AvatarColoredBox()), color)
      }
      .clip(with(LocalDensity.current) { AvatarClipBox() })
  ) {
    Image(
      painter = painterResource(avatarImage),
      contentDescription = null,
      modifier = Modifier
        .size(80.dp)
        .align(Alignment.TopEnd)
    )
  }
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
    moveTo(31.7.dp.toPx(), 3.1.dp.toPx())
    lineTo(size.width, 0f)
    lineTo(size.width - 23.dp.toPx(), size.height)
    lineTo(15.6.dp.toPx(), size.height - 8.dp.toPx())
    close()
  }
}

private fun Density.InnerBox(): Shape {
  return GenericShape { size, _ ->
    moveTo(33.dp.toPx(), 7.7.dp.toPx())
    lineTo(size.width - 13.dp.toPx(), 3.7.dp.toPx())
    lineTo(size.width - 25.7.dp.toPx(), size.height - 4.6.dp.toPx())
    lineTo(20.4.dp.toPx(), size.height - 12.dp.toPx())
    close()
  }
}

private fun Density.OuterBoxStem(): Shape = GenericShape { size, _ ->
  moveTo(0f, size.center.y + 6.6.dp.toPx())
  lineTo(19.7.dp.toPx(), size.center.y - 11.dp.toPx())
  lineTo(21.dp.toPx(), size.center.y - 5.4.dp.toPx())
  lineTo(32.4.dp.toPx(), size.center.y - 13.3.dp.toPx())
  lineTo(26.6.dp.toPx(), size.center.y + 10.2.dp.toPx())
  lineTo(11.7.dp.toPx(), size.center.y + 13.3.dp.toPx())
  lineTo(10.dp.toPx(), size.center.y + 6.1.dp.toPx())
  close()
}

private fun Density.InnerBoxStem(): Shape = GenericShape { size, _ ->
  moveTo(4.6.dp.toPx(), size.center.y + 3.8.dp.toPx())
  lineTo(17.dp.toPx(), size.center.y - 7.2.dp.toPx())
  lineTo(19.3.dp.toPx(), size.center.y - 2.1.dp.toPx())
  lineTo(30.4.dp.toPx(), size.center.y - 6.7.dp.toPx())
  lineTo(27.dp.toPx(), size.center.y + 4.6.dp.toPx())
  lineTo(14.4.dp.toPx(), size.center.y + 7.4.dp.toPx())
  lineTo(12.8.dp.toPx(), size.center.y + 0.6.dp.toPx())
  close()
}

private fun Density.AvatarColoredBox(): Shape = GenericShape { _, _ ->
  moveTo(22.5.dp.toPx(), 28.dp.toPx())
  lineTo(94.4.dp.toPx(), 31.4.dp.toPx())
  lineTo(104.3.dp.toPx(), 67.5.dp.toPx())
  lineTo(40.dp.toPx(), 76.6.dp.toPx())
  close()
}

private fun Density.AvatarWhiteBox(): Shape = GenericShape { _, _ ->
  moveTo(16.4.dp.toPx(), 20.5.dp.toPx())
  lineTo(96.7.dp.toPx(), 30.4.dp.toPx())
  lineTo(106.4.dp.toPx(), 70.dp.toPx())
  lineTo(37.8.dp.toPx(), 80.4.dp.toPx())
  close()
}

private fun Density.AvatarBlackBox(): Shape = GenericShape { _, _ ->
  moveTo(0f, 17.dp.toPx())
  lineTo(100.5.dp.toPx(), 27.2.dp.toPx())
  lineTo(110.dp.toPx(), 72.7.dp.toPx())
  lineTo(33.4.dp.toPx(), 90.dp.toPx())
  close()
}

private fun Density.AvatarClipBox(): Shape = GenericShape { _, _, ->
  moveTo(10.3.dp.toPx(), (-5.6).dp.toPx())
  lineTo(114.7.dp.toPx(), (-5.6).dp.toPx())
  lineTo(114.7.dp.toPx(), 65.6.dp.toPx())
  lineTo(40.dp.toPx(), 76.6.dp.toPx())
  close()
}