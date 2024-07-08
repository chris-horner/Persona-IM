package codes.chrishorner.personasns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastZip
import kotlin.random.Random

@Composable
fun rememberTranscript(): Transcript {
  val density = LocalDensity.current
  return remember(density) { Transcript(density) }
}

@Stable
class Transcript internal constructor(private val density: Density) {

  private val messagesState = MessagesState()
  private val coordinates = mutableListOf<LineCoordinates>()
  private val _entries = mutableStateOf(emptyList<Entry>())

  val entries: State<List<Entry>> = _entries

  fun advance() {
    val messages = messagesState.advance()

    // If messages have looped back to the start, clear any saved line coordinates so we can start
    // calculating them again.
    if (messages.size == 1) coordinates.clear()

    // Add coordinates for the most recently added message to the coordinates list. These are
    // updated when a subsequent call to advance() is made.
    coordinates += getMostRecentLineCoordinates(messages.last().sender)

    if (coordinates.size > 1) {
      val secondLastIndex = coordinates.lastIndex - 1
      val secondLastMessage = messages[secondLastIndex]
      val secondLastCoordinates = coordinates[secondLastIndex]
      coordinates[secondLastIndex] = updateLineCoordinates(
        index = secondLastIndex,
        sender = secondLastMessage.sender,
        lineCoordinates = secondLastCoordinates,
      )
    }

    _entries.value = messages.fastZip(coordinates) { message, lineCoordinates ->
      Entry(message, lineCoordinates)
    }
  }

  private fun getMostRecentLineCoordinates(sender: Sender): LineCoordinates = with(density) {
    val width = randomBetween(MinLineWidth.toPx(), MaxLineWidth.toPx())

    return when (sender) {
      Sender.Ren -> {
        val leftX = RenMessageCenter.x.toPx() - (width / 2f)
        val y = RenMessageCenter.y.toPx()
        LineCoordinates(
          leftPoint = Offset(leftX, y),
          rightPoint = Offset(leftX + width, y),
        )
      }

      else -> {
        val leftX = (AvatarSize.width.toPx() / 2f) - (width / 2f)
        val y = AvatarSize.height.toPx() / 2f
        LineCoordinates(
          leftPoint = Offset(leftX, y),
          rightPoint = Offset(leftX + width, y),
        )
      }
    }
  }

  private fun updateLineCoordinates(
    index: Int,
    sender: Sender,
    lineCoordinates: LineCoordinates,
  ): LineCoordinates = with(density) {
    val direction = if (index % 2 == 0) 1f else -1f
    val horizontalShift = when {
      index > 0 -> randomBetween(MinLineShift.toPx(), MaxLineShift.toPx()) * direction
      else -> 0f
    }
    val horizontalOffset = when (sender) {
      Sender.Ren -> Offset(0f, 0f)
      else -> Offset(horizontalShift, 0f)
    }

    return lineCoordinates.copy(
      leftPoint = lineCoordinates.leftPoint + horizontalOffset,
      rightPoint = lineCoordinates.rightPoint + horizontalOffset,
    )
  }

  companion object {
    val AvatarSize = DpSize(110.dp, 90.dp)
    val EntrySpacing = 16.dp

    context(CacheDrawScope)
    fun getTopDrawingOffset(entry: Entry): Offset {
      return when (entry.message.sender) {
        Sender.Ren -> {
          val horizontalShift = size.width - (RenMessageCenter.x.toPx() * 2f)
          Offset(x = horizontalShift, y = 0f)
        }

        else -> Offset(x = 0f, y = 0f)
      }
    }

    context(CacheDrawScope)
    fun getBottomDrawingOffset(entry: Entry): Offset {
      val verticalShift = size.height + EntrySpacing.toPx()
      return when (entry.message.sender) {
        Sender.Ren -> {
          val horizontalShift = size.width - (RenMessageCenter.x.toPx() * 2f)
          Offset(x = horizontalShift, y = verticalShift)
        }

        else -> Offset(x = 0f, y = verticalShift)
      }
    }
  }
}

data class Entry(
  val message: Message,
  val lineCoordinates: LineCoordinates,
)

/**
 * Represents how the black background line connects to a message.
 */
data class LineCoordinates(
  val leftPoint: Offset,
  val rightPoint: Offset,
)

private val MinLineWidth = 44.dp
private val MaxLineWidth = 66.dp

private val MinLineShift = 16.dp
private val MaxLineShift = 48.dp

private val RenMessageCenter = DpOffset(x = 60.dp, y = 28.dp)

private fun randomBetween(start: Float, end: Float): Float {
  return start + Random.nextFloat() * (end - start)
}