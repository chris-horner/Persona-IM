package codes.chrishorner.personasns

import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

fun CacheDrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}