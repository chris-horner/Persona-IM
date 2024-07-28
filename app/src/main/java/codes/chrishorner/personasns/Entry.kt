package codes.chrishorner.personasns

import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

@Composable
fun Entry(
  entry: Entry,
  modifier: Modifier = Modifier,
) {
  EntryLayout(
    avatar = { Avatar(entry) },
    textBox = { TextBox(entry) },
    modifier = Modifier
      .padding(horizontal = 8.dp)
      .then(modifier)
  )
}

@Composable
private fun EntryLayout(
  avatar: @Composable () -> Unit,
  textBox: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Layout(
    content = {
      avatar()
      textBox()
    },
    modifier = modifier,
  ) { (avatarMeasurable, textMeasurable), constraints ->
    // How much the text box should overlap into the avatar (to account for the box's stem).
    val textBoxOverlap = 18.dp.roundToPx()
    // Make sure there's a bit of space between the text box and the top of the entry.
    val textBoxVerticalPadding = 4.dp.roundToPx()

    val avatarPlaceable = avatarMeasurable.measure(constraints)
    val textMaxWidth = constraints.maxWidth - avatarPlaceable.width + textBoxOverlap
    val textConstraints = constraints.copy(maxWidth = textMaxWidth)
    val textPlaceable = textMeasurable.measure(textConstraints)

    val width = avatarPlaceable.width + textPlaceable.width - textBoxOverlap
    val height = maxOf(avatarPlaceable.height, textPlaceable.height + textBoxVerticalPadding)
    layout(width, height) {
      avatarPlaceable.place(0, 0)
      val textBoxX = avatarPlaceable.width - textBoxOverlap
      val textBoxY = (avatarPlaceable.height - textPlaceable.height).coerceAtLeast(textBoxVerticalPadding)
      textPlaceable.place(textBoxX, textBoxY)
    }
  }
}

@Composable
private fun TextBox(entry: Entry) {
  Box {
    Text(
      text = entry.message.text,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.White,
      fontFamily = OptimaNova,
      modifier = Modifier
        .drawWithCache {
          val outerBoxStem = Outline(outerStem())
          val outerBoxShape = outerBox()
          val outerBox = Outline(outerBoxShape)
          val innerBoxStem = Outline(innerStem())
          val innerBox = Outline(innerBox())
          val shadowPaint = Paint().apply {
            this.color = Color.Black
            alpha = 0.3f
            asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
          }

          onDrawBehind {
            scale(
              scaleX = entry.messageHorizontalScale.value,
              scaleY = entry.messageVerticalScale.value,
              pivot = Offset(x = outerBoxStem.bounds.width, y = getStemY(size.height)),
            ) {
              drawIntoCanvas { it.drawOutline(outerBox, shadowPaint) }
              drawOutline(outerBox, color = Color.White)
            }

            drawOutline(outerBoxStem, color = Color.White)
            drawOutline(innerBoxStem, color = Color.Black)

            scale(
              scaleX = entry.messageHorizontalScale.value,
              scaleY = entry.messageVerticalScale.value,
              pivot = Offset(x = outerBoxStem.bounds.width, y = getStemY(size.height)),
            ) {
              drawOutline(innerBox, color = Color.Black)
            }
          }
        }
        .alpha(entry.messageTextAlpha.value)
        .padding(start = 42.dp, top = 20.dp, end = 32.dp, bottom = 20.dp)
    )

    if (entry.drawPunctuation) {
      Image(
        painter = painterResource(R.drawable.question_mark),
        contentDescription = null,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = (-12).dp, y = (-16).dp)
          .scale(entry.punctuationScale.value)
      )
    }
  }
}

private fun Density.outerStem(): Shape = GenericShape { size, _ ->
  val verticalOrigin = getStemY(size.height)

  moveTo(0f, verticalOrigin - 19.2.dp.toPx())
  lineTo(19.5.dp.toPx(), verticalOrigin - 37.2.dp.toPx())
  lineTo(20.8.dp.toPx(), verticalOrigin - 31.5.dp.toPx())
  lineTo(32.4.dp.toPx(), verticalOrigin - 39.3.dp.toPx())
  lineTo(30.6.dp.toPx(), verticalOrigin - 15.8.dp.toPx())
  lineTo(11.7.dp.toPx(), verticalOrigin - 12.6.dp.toPx())
  lineTo(10.dp.toPx(), verticalOrigin - 20.dp.toPx())
  close()
}

private fun Density.innerStem(): Shape = GenericShape { size, _ ->
  val verticalOrigin = getStemY(size.height)

  moveTo(4.6.dp.toPx(), verticalOrigin - 22.2.dp.toPx())
  lineTo(17.dp.toPx(), verticalOrigin - 33.2.dp.toPx())
  lineTo(19.3.dp.toPx(), verticalOrigin - 28.1.dp.toPx())
  lineTo(34.4.dp.toPx(), verticalOrigin - 36.5.dp.toPx())
  lineTo(34.dp.toPx(), verticalOrigin - 21.4.dp.toPx())
  lineTo(14.4.dp.toPx(), verticalOrigin - 18.6.dp.toPx())
  lineTo(12.8.dp.toPx(), verticalOrigin - 25.4.dp.toPx())
  close()
}

private fun Density.getStemY(boxHeight: Float): Float {
  // The Y coordinate of the stem changes slightly depending on the height of the box.
  val avatarHeight = Transcript.AvatarSize.height.toPx()
  val offset = if (boxHeight > avatarHeight) {
    16.dp.roundToPx()
  } else {
    20.dp.roundToPx()
  }

  return avatarHeight - offset
}

private fun Density.outerBox(): Shape = GenericShape { size, _ ->
  moveTo(31.7.dp.toPx(), 3.1.dp.toPx())
  lineTo(size.width, 0f)
  lineTo(size.width - 23.dp.toPx(), size.height)
  lineTo(15.6.dp.toPx(), size.height - 8.dp.toPx())
  close()
}

private fun Density.innerBox(): Shape {
  return GenericShape { size, _ ->
    moveTo(33.dp.toPx(), 7.7.dp.toPx())
    lineTo(size.width - 13.dp.toPx(), 3.7.dp.toPx())
    lineTo(size.width - 25.7.dp.toPx(), size.height - 4.6.dp.toPx())
    lineTo(20.4.dp.toPx(), size.height - 12.dp.toPx())
    close()
  }
}