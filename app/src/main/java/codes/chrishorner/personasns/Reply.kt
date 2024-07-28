package codes.chrishorner.personasns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/**
 * A message sent in chat by the player.
 */
@Composable
fun Reply(entry: Entry, modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.CenterEnd,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp)
  ) {
    Text(
      text = entry.message.text,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black,
      fontFamily = OptimaNova,
      modifier = Modifier
        .drawWithCache {
          val outerStem = Outline(replyOuterStem())
          val outerBoxShape = replyOuterBox()
          val outerBox = Outline(outerBoxShape)
          val innerStem = Outline(replyInnerStem())
          val innerBox = Outline(replyInnerBox())

          onDrawBehind {

            scale(
              scaleX = entry.messageHorizontalScale.value,
              scaleY = entry.messageVerticalScale.value,
              pivot = Offset(size.width, size.center.y)
            ) {
              drawOutline(outerBox, color = Color.Black)
              drawOutline(outerStem, color = Color.Black)
              drawOutline(innerStem, color = Color.White)
              drawOutline(innerBox, color = Color.White)
            }
          }
        }
        .alpha(entry.messageTextAlpha.value)
        .padding(start = 44.dp, top = 20.dp, end = 40.dp, bottom = 20.dp)
    )
  }
}

private fun Density.replyOuterBox(): Shape = GenericShape { size, _ ->
  moveTo(0f, 0f)
  lineTo(size.width - 35.dp.toPx(), 4.dp.toPx())
  lineTo(size.width - 10.7.dp.toPx(), size.height - 6.6.dp.toPx())
  lineTo(35.5.dp.toPx(), size.height)
  close()
}

private fun Density.replyInnerBox(): Shape = GenericShape { size, _ ->
  moveTo(12.dp.toPx(), 5.dp.toPx())
  lineTo(size.width - 36.dp.toPx(), 9.5.dp.toPx())
  lineTo(size.width - 16.4.dp.toPx(), size.height - 11.7.dp.toPx())
  lineTo(36.5.dp.toPx(), size.height - 3.5.dp.toPx())
  close()
}

private fun Density.replyOuterStem(): Shape = GenericShape { size, _ ->
  val verticalOrigin = size.height
  moveTo(size.width - 37.6.dp.toPx(), verticalOrigin - 42.3.dp.toPx())
  lineTo(size.width - 20.8.dp.toPx(), verticalOrigin - 30.2.dp.toPx())
  lineTo(size.width - 19.4.dp.toPx(), verticalOrigin - 36.8.dp.toPx())
  lineTo(size.width, verticalOrigin - 19.6.dp.toPx())
  lineTo(size.width - 10.3.dp.toPx(), verticalOrigin - 19.6.dp.toPx())
  lineTo(size.width - 12.dp.toPx(), verticalOrigin - 12.3.dp.toPx())
  lineTo(size.width - 27.6.dp.toPx(), verticalOrigin - 15.2.dp.toPx())
  close()
}

private fun Density.replyInnerStem(): Shape = GenericShape { size, _ ->
  val verticalOrigin = size.height
  moveTo(size.width - 33.1.dp.toPx(), verticalOrigin - 33.2.dp.toPx())
  lineTo(size.width - 19.3.dp.toPx(), verticalOrigin - 26.3.dp.toPx())
  lineTo(size.width - 16.4.dp.toPx(), verticalOrigin - 31.6.dp.toPx())
  lineTo(size.width - 4.2.dp.toPx(), verticalOrigin - 21.dp.toPx())
  lineTo(size.width - 12.4.dp.toPx(), verticalOrigin - 23.4.dp.toPx())
  lineTo(size.width - 14.dp.toPx(), verticalOrigin - 17.2.dp.toPx())
  lineTo(size.width - 28.6.dp.toPx(), verticalOrigin - 21.2.dp.toPx())
  close()
}
