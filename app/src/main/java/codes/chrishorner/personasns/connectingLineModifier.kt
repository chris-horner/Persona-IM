package codes.chrishorner.personasns

import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.dp

fun Modifier.drawConnectingLine(entry1: Entry, entry2: Entry?): Modifier {
  return this then ConnectingLineElement(entry1, entry2)
}

private data class ConnectingLineElement(
  val entry1: Entry,
  val entry2: Entry?,
) : ModifierNodeElement<LineNode>() {

  override fun create(): LineNode = LineNode(entry1, entry2)

  override fun update(node: LineNode) {
    node.updateEntries(entry1, entry2)
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "connecting line"
    value = entry1 to entry2
  }
}

private class LineNode(
  private var entry1: Entry,
  private var entry2: Entry?,
) : DelegatingNode() {

  private val linePath = Path()

  private val drawWithCacheModifierNode = delegate(
    CacheDrawModifierNode {
      val entry2 = entry2 ?: return@CacheDrawModifierNode onDrawBehind {  }

      val topOffset = Transcript.getTopDrawingOffset(entry1)
      val topLeft = entry1.lineCoordinates.leftPoint + topOffset
      val topRight = entry1.lineCoordinates.rightPoint + topOffset

      val bottomOffset = Transcript.getBottomDrawingOffset(entry2)
      val bottomLeft = entry2.lineCoordinates.leftPoint + bottomOffset
      val bottomRight = entry2.lineCoordinates.rightPoint + bottomOffset
      val shadowPaint = Paint().apply {
        color = Color.Black
        alpha = 0.5f
        asFrameworkPaint().maskFilter = BlurMaskFilter(2.dp.toPx(), NORMAL)
      }

      onDrawBehind {
        val currentBottomLeft = lerp(topLeft, bottomLeft, fraction = entry1.lineProgress.value)
        val currentBottomRight = lerp(topRight, bottomRight, fraction = entry1.lineProgress.value)

        with(linePath) {
          reset()
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
  )

  fun updateEntries(entry1: Entry, entry2: Entry?) {
    this.entry1 = entry1
    this.entry2 = entry2
    drawWithCacheModifierNode.invalidateDrawCache()
  }
}