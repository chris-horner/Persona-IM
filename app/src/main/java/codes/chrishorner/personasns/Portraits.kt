package codes.chrishorner.personasns

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlin.random.Random

/**
 * Collection of portraits to show the current participants of the conversation.
 */
@Composable
fun Portraits(senders: ImmutableList<Sender>, modifier: Modifier = Modifier) {
  val density = LocalDensity.current
  val resources = LocalContext.current.resources

  val portraitDisplayModels = remember(senders, density, resources) {
    with(density) {
      with(resources) {
        // Pick an index at random to have a "dark avatar", where black is rendered behind the
        // portrait.
        val darkAvatarIndex = Random.nextInt(senders.size)
        senders.shuffled().mapIndexed { index, sender ->
          sender.getDisplayModel(
            allowHorizontalOffset = index > 0,
            darkAvatar = darkAvatarIndex == index,
          )
        }
      }
    }
  }

  Canvas(
    modifier = modifier.height(PortraitSize.height),
  ) {
    var stride = 0f

    // We need to draw _all_ the black backgrounds first.
    for (model in portraitDisplayModels) {
      stride += model.horizontalOffset

      val rotationPivot = Offset(x = PortraitSize.width.toPx(), y = PortraitSize.height.toPx())

      withTransform(
        transformBlock = {
          translate(left = stride + model.horizontalOffset, model.verticalOffset)
          rotate(model.outerRotation, pivot = rotationPivot)
        }
      ) {
        drawRect(Color.Black, size = PortraitSize.toSize())
      }

      stride += PortraitSize.width.toPx()
    }

    stride = 0f

    // Then draw the white part of the background in a separate loop, since these can overlap.
    for (model in portraitDisplayModels) {
      stride += model.horizontalOffset

      val rotationPivot = Offset(x = PortraitSize.width.toPx(), y = PortraitSize.height.toPx())

      withTransform(
        transformBlock = {
          translate(left = stride + model.horizontalOffset, model.verticalOffset)
          rotate(model.middleRotation, pivot = rotationPivot)
        }
      ) {
        drawPath(model.middlePath, Color.White)

        withTransform(
          transformBlock = {
            rotate(model.innerRotation, rotationPivot)
            clipPath(model.innerPath)
          },
        ) {
          if (model.darkAvatar) {
            drawRect(Color.Black)
          }

          translate(left = model.imageOffset.x, top = model.imageOffset.y) {
            drawImage(model.image)
          }
        }
      }

      stride += PortraitSize.width.toPx()
    }
  }
}

context(Density, Resources)
private fun Sender.getDisplayModel(
  allowHorizontalOffset: Boolean,
  darkAvatar: Boolean,
): PortraitDisplayModel {

  fun middleOffset(): Float {
    return randomPxBetween(6.dp, 7.dp)
  }

  fun innerOffset(): Float {
    return randomPxBetween(10.dp, 11.dp)
  }

  val size = PortraitSize.toSize()

  val middlePath = Path().apply {
    moveTo(middleOffset(), middleOffset())
    lineTo(size.width - middleOffset(), middleOffset())
    lineTo(size.width - middleOffset(), size.height - middleOffset())
    lineTo(middleOffset(), size.height - middleOffset())
    close()
  }

  val innerPath = Path().apply {
    moveTo(innerOffset(), innerOffset())
    lineTo(size.width - innerOffset(), innerOffset())
    lineTo(size.width - innerOffset(), size.height - innerOffset() - 1.dp.toPx())
    lineTo(innerOffset(), size.height - innerOffset())
    close()
  }

  val outerRotation = randomBetween(-12f, 2f)
  val middleRotation = outerRotation + randomBetween(-2f, 0f)
  val innerRotation = randomBetween(-2f, 0f)

  return PortraitDisplayModel(
    image = ImageBitmap.imageResource(this@Resources, image),
    imageOffset = Offset(
      // Ann's portrait is kind of annoying and looks a bit better when offset to the right.
      x = if (this@Sender == Sender.Ann) 8.dp.toPx() else 0f,
      y = 10.dp.toPx(),
    ),
    outerRotation = outerRotation,
    middleRotation = middleRotation,
    innerRotation = innerRotation,
    horizontalOffset = if (allowHorizontalOffset) {
      randomBetween((-14).dp.toPx(), (-12).dp.toPx())
    } else {
      0f
    },
    verticalOffset = randomBetween((-4).dp.toPx(), 4.dp.toPx()),
    middlePath = middlePath,
    innerPath = innerPath,
    darkAvatar = darkAvatar,
  )
}

private val PortraitSize = DpSize(width = 70.dp, height = 80.dp)

private data class PortraitDisplayModel(
  val image: ImageBitmap,
  val imageOffset: Offset,
  val outerRotation: Float,
  val middleRotation: Float,
  val innerRotation: Float,
  val horizontalOffset: Float,
  val verticalOffset: Float,
  val middlePath: Path,
  val innerPath: Path,
  val darkAvatar: Boolean,
)