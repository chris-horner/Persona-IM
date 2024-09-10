package codes.chrishorner.personasns

import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse

fun DrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

fun CacheDrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

/**
 * Takes an SVG string and returns a [Path]. If [width] and [height] are not provided, then it's
 * assumed that the intrinsic width and height of the path's bounds are in [Dp].
 */
context(Density)
fun String.asPath(
  width: Dp = Dp.Unspecified,
  height: Dp = Dp.Unspecified,
): Path {
  val path = PathParser().parsePathString(this).toPath()
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
