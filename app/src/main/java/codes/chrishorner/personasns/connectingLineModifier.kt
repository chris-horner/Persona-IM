package codes.chrishorner.personasns

import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

/**
 * Draws a black line from `entry` to `entry2` (if it exists).
 */
fun Modifier.drawConnectingLine(entry1: Entry, entry2: Entry?): Modifier {
  if (entry2 == null) return this

  return drawWithCache {
    val linePath = Path()
    val topOffset = TranscriptSizes.getTopDrawingOffset(entry1)
    val topLeft = entry1.lineCoordinates.leftPoint + topOffset
    val topRight = entry1.lineCoordinates.rightPoint + topOffset

    val bottomOffset = TranscriptSizes.getBottomDrawingOffset(entry2)
    val bottomLeft = entry2.lineCoordinates.leftPoint + bottomOffset
    val bottomRight = entry2.lineCoordinates.rightPoint + bottomOffset

    val shadowPaint = Paint().apply {
      color = Color.Black
      alpha = 0.5f
      asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
    }

    onDrawBehind {
      val currentBottomLeft = lerp(topLeft, bottomLeft, fraction = entry1.lineProgress.value)
      val currentBottomRight = lerp(topRight, bottomRight, fraction = entry1.lineProgress.value)

      with(linePath) {
        rewind()
        moveTo(topLeft.x, topLeft.y)
        lineTo(topRight.x, topRight.y)
        lineTo(currentBottomRight.x, currentBottomRight.y)
        lineTo(currentBottomLeft.x, currentBottomLeft.y)
        close()
      }

      translate(top = 16.dp.toPx()) {
        drawIntoCanvas {
          it.drawPath(linePath, shadowPaint)
        }
      }

      drawPath(linePath, Color.Black)
    }
  }
}