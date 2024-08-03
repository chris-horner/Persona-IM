package codes.chrishorner.personasns

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypingIndicator(
  modifier: Modifier = Modifier,
) {
  val (dot1, dot2, dot3) = rememberDots()
  val rotation = remember { randomBetween(-6f, 12f) }
  val dotState = rememberDotsState()

  val scaleAnimation = remember { Animatable(initialValue = 0f) }
  LaunchedEffect(Unit) {
    delay(180L * AnimationDurationScale)
    scaleAnimation.snapTo(0.6f)
    scaleAnimation.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = 116 * AnimationDurationScale,
        easing = BetterEaseOutBack,
      ),
    )
  }

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(Transcript.AvatarSize)
      .padding(start = 16.dp)
  ) {
    Image(
      painter = painterResource(R.drawable.typing_bubble),
      contentDescription = null,
      modifier = Modifier
        .drawWithContent {
          scale(scaleAnimation.value) {
            rotate(rotation) {
              this@drawWithContent.drawContent()
            }

            translate(left = center.x - 4.dp.toPx(), top = center.y) {
              if (dotState.dot1) drawPath(dot1, PersonaRed)

              translate(left = 12.dp.toPx()) {
                if (dotState.dot2) drawPath(dot2, PersonaRed)
              }

              translate(left = 24.dp.toPx()) {
                if (dotState.dot3) drawPath(dot3, PersonaRed)
              }
            }
          }
        }
    )
  }
}

@Composable
private fun rememberDots(): ImmutableList<Path> {
  val density = LocalDensity.current

  return remember(density) {
    with(density) {
      val dot1 = parsePath(
        "M2.9717,-0C5.7391,-0 6.233,2.2052 6.233,3.9155C6.233,5.6253 4.3672,6.1204 3.1285,6.1204C1.4861,6.1204 0.0005,4.9279 0.0005,3.2852C0.0005,1.6428 1.2392,-0 2.9717,-0Z"
      )
      val dot2 = parsePath(
        "M2.9715,0.0005C5.198,0.0005 6.0525,1.8907 6.0525,3.3081C6.0525,4.7259 4.6139,5.9861 3.0617,5.9861C1.5094,5.9861 0.0003,4.6357 0.0003,3.3308C0.0003,2.0255 1.1684,0.0005 2.9715,0.0005Z"
      )
      val dot3 = parsePath(
        "M3.1075,-0C4.7029,-0 6.0082,2.0253 6.0082,3.3079C6.0082,4.5905 4.6127,5.648 3.084,5.648C1.5514,5.648 -0.0009,4.478 -0.0009,3.1053C-0.0009,1.7325 1.1045,-0 3.1075,-0Z"
      )

      persistentListOf(dot1, dot2, dot3)
    }
  }
}

/**
 * Takes an SVG string and returns a [Path]. If [width] and [height] are not provided, then it's
 * assumed that the intrinsic width and height of the path's bounds are in [Dp].
 */
private fun Density.parsePath(
  pathData: String,
  width: Dp = Dp.Unspecified,
  height: Dp = Dp.Unspecified,
): Path {
  val path = PathParser().parsePathString(pathData).toPath()
  val bounds = path.getBounds()
  val widthPx = width.takeOrElse { bounds.width.dp }.toPx()
  val heightPx = height.takeOrElse { bounds.height.dp }.toPx()
  val matrix = Matrix().apply {
    translate(x = bounds.center.x, y = bounds.center.y)
    scale(x = widthPx / bounds.width, y = heightPx / bounds.height)
  }

  path.transform(matrix)
  return path
}

@Composable
private fun rememberDotsState(): DotsState {
  val scope = rememberCoroutineScope()
  return remember { DotsState(scope) }
}

@Stable
private class DotsState(coroutineScope: CoroutineScope) {
  private val _dot1 = mutableStateOf(false)
  private val _dot2 = mutableStateOf(false)
  private val _dot3 = mutableStateOf(false)
  val dot1: Boolean by _dot1
  val dot2: Boolean by _dot2
  val dot3: Boolean by _dot3

  init {
    coroutineScope.launch {
      while (true) {
        delay(300)
        _dot1.value = true
        delay(300)
        _dot2.value = true
        delay(300)
        _dot3.value = true
        delay(400)
        _dot1.value = false
        delay(100)
        _dot2.value = false
        delay(100)
        _dot3.value = false
      }
    }
  }
}