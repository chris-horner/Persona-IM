package codes.chrishorner.personasns

import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Modifier.drawConnectingLine(entry1: Entry, entry2: Entry?): Modifier {
  return this then ConnectingLineElement(entry1, entry2)
}

private data class ConnectingLineElement(
  val entry1: Entry,
  val entry2: Entry?,
) : ModifierNodeElement<LineNode2>() {

  override fun create(): LineNode2 = LineNode2(entry1, entry2)

  override fun update(node: LineNode2) {
    node.updateEntries(entry1, entry2)
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "connecting line"
    value = entry1 to entry2
  }
}

private class LineNode2(
  var entry1: Entry,
  var entry2: Entry?,
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
        asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
      }

      onDrawBehind {
        //val currentBottomLeft = lerp(topLeft, bottomLeft, progress = 1f)
        //val currentBottomRight = lerp(topRight, bottomRight, progress = 1f)
        val currentBottomLeft = bottomLeft
        val currentBottomRight = bottomRight

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

private class LineNode(
  var entry1: Entry,
  var entry2: Entry?,
) : DelegatingNode(), DrawModifierNode, ObserverModifierNode, CompositionLocalConsumerModifierNode {
  private var density: Density = UnityDensity
  private val linePath = Path()
  private val progress = Animatable(0f)
  private val shadowPaint = Paint().apply {
    color = Color.Black
    alpha = 0.5f
    asFrameworkPaint().maskFilter = with(density) { BlurMaskFilter(4.dp.toPx(), NORMAL) }
  }
  private var currentAnimation: Job = Job()

  fun updateEntries(entry1: Entry, entry2: Entry?) {
    this.entry1 = entry1
    this.entry2 = entry2
    //println("update entries")
    //entry2?.let { println(it) }
    //runAnimation()
  }

  override fun onAttach() {
    updateDensity()
    runAnimation()
  }

  private fun runAnimation() {
    currentAnimation.cancel()
    currentAnimation = coroutineScope.launch {
      progress.snapTo(0f)
      progress.animateTo(1f) {
        invalidateDraw()
      }
    }
  }

  override fun ContentDrawScope.draw() {

    if (entry2 == null) {
      drawContent()
      return
    }

    //val topOffset = Transcript.getTopDrawingOffset(entry1)
    val topOffset = Offset.Unspecified
    val topLeft = entry1.lineCoordinates.leftPoint + topOffset
    val topRight = entry1.lineCoordinates.rightPoint + topOffset

    // TODO: Fix null unwraps
    //val bottomOffset = Transcript.getBottomDrawingOffset(entry2!!)
    val bottomOffset = Offset.Unspecified
    val bottomLeft = entry2!!.lineCoordinates.leftPoint + bottomOffset
    val bottomRight = entry2!!.lineCoordinates.rightPoint + bottomOffset

    //val currentBottomLeft = lerp(topLeft, bottomLeft, progress.value)
    //val currentBottomRight = lerp(topRight, bottomRight, progress.value)

    with(linePath) {
      reset()
      moveTo(topLeft.x, topLeft.y)
      lineTo(topRight.x, topRight.y)
      //lineTo(currentBottomRight.x, currentBottomRight.y)
      //lineTo(currentBottomLeft.x, currentBottomLeft.y)
      lineTo(bottomRight.x, bottomRight.y)
      lineTo(bottomLeft.x, bottomLeft.y)
      close()
    }

    translate(top = 16.dp.toPx()) {
      drawIntoCanvas {
        it.drawPath(linePath, shadowPaint)
      }
    }

    drawPath(linePath, Color.Black)

    drawContent()
  }

  override fun onObservedReadsChanged() {
    updateDensity()
  }

  private fun updateDensity() {
    density = currentValueOf(LocalDensity)
    shadowPaint.asFrameworkPaint().maskFilter = with(density) {
      BlurMaskFilter(4.dp.toPx(), NORMAL)
    }
  }
}

private object UnityDensity : Density {
  override val density: Float
    get() = 1f
  override val fontScale: Float
    get() = 1f
}



private val linePath = Path()

private fun Modifier.drawOldConnectingLine(entry1: Entry, entry2: Entry?, progress: Float): Modifier {
  if (entry2 == null) return this

  return drawWithCache {
    val topOffset = Transcript.getTopDrawingOffset(entry1)
    val topLeft = entry1.lineCoordinates.leftPoint + topOffset
    val topRight = entry1.lineCoordinates.rightPoint + topOffset

    val bottomOffset = Transcript.getBottomDrawingOffset(entry2)
    val bottomLeft = entry2.lineCoordinates.leftPoint + bottomOffset
    val bottomRight = entry2.lineCoordinates.rightPoint + bottomOffset

    val shadowPaint = Paint().apply {
      this.color = Color.Black
      alpha = 0.5f
      asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
    }

    onDrawBehind {
      val currentBottomLeft = lerp(topLeft, bottomLeft, progress)
      val currentBottomRight = lerp(topRight, bottomRight, progress)

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
}