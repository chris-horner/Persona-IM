package codes.chrishorner.personasns

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun rememberTranscriptState(): Transcript {
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()
  return remember(density) { Transcript(density, coroutineScope) }
}

/** A message in the transcript with every needed for rendering. */
@Stable
data class Entry(
  val message: Message,
  val lineCoordinates: LineCoordinates,
  /** Animated percentage progress of the black line connecting this message to the next. */
  val lineProgress: State<Float>,
  val avatarBackgroundScale: State<Float>,
  val avatarForegroundScale: State<Float>,
  val messageHorizontalScale: State<Float>,
  val messageVerticalScale: State<Float>,
  val messageTextAlpha: State<Float>,
)

/**
 * Represents where the black background line connects to a message entry - relative to its avatar.
 */
data class LineCoordinates(
  val leftPoint: Offset,
  val rightPoint: Offset,
)

// TODO: Rename to TranscriptState
@Stable
class Transcript internal constructor(
  private val density: Density,
  private val coroutineScope: CoroutineScope,
) {

  private val messagesState = MessagesState()

  /** An associative array of states kept in sync with [messagesState]. */
  private val entryStates = mutableListOf<EntryState>()
  private val _entries = mutableStateOf<ImmutableList<Entry>>(persistentListOf())
  val entries: State<ImmutableList<Entry>> = _entries

  fun advance() {

    val messages = messagesState.advance()

    // If messages have looped back to the start, clear any saved states so we can start calculating
    // them again.
    if (messages.size == 1) entryStates.clear()

    // Add a new EntryState to the collection to match the newly added Message. This state will be
    // updated on a subsequent call to advance().
    entryStates += createEntryState(messages.lastIndex, messages.last())

    if (entryStates.size > 1) {
      // A new message has been added and there's more than 2. Update the previous entry to its
      // final state.
      val secondLastIndex = entryStates.lastIndex - 1
      finalizeEntryState(entryStates[secondLastIndex])
    }

    _entries.value = entryStates.map { it.toEntry() }.toImmutableList()
  }

  private fun createEntryState(index: Int, message: Message): EntryState = with(density) {
    val width = randomBetween(MinLineWidth.toPx(), MaxLineWidth.toPx())

    val lineCoordinates = when (message.sender) {
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

    return EntryState(
      position = index,
      message = message,
      lineProgress = Animatable(initialValue = 0f),
      avatarBackgroundScale = Animatable(initialValue = 0.6f).apply {
        coroutineScope.launch {
          animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = 300 * AnimationDurationScale,
              easing = BetterEaseOutBack,
            ),
          )
        }
      },
      avatarForegroundScale = Animatable(0.0f).apply {
        coroutineScope.launch {
          delay(180L * AnimationDurationScale)
          snapTo(0.8f)
          animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = 150 * AnimationDurationScale,
              easing = BetterEaseOutBack,
            ),
          )
        }
      },
      messageHorizontalScale = Animatable(0.5f).apply {
        coroutineScope.launch {
          animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = 280 * AnimationDurationScale,
              easing = BetterEaseOutBack,
            ),
          )
        }
      },
      messageVerticalScale = Animatable(0.8f).apply {
        coroutineScope.launch {
          animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = 280 * AnimationDurationScale,
              easing = BetterEaseOutBack,
            ),
          )
        }
      },
      messageTextAlpha = Animatable(0f).apply {
        coroutineScope.launch {
          delay(150L * AnimationDurationScale)
          animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 100 * AnimationDurationScale),
          )
        }
      },
      lineCoordinates = lineCoordinates,
    )
  }

  private fun finalizeEntryState(entryState: EntryState) = with(density) {
    val direction = if (entryState.position % 2 == 0) 1f else -1f
    val horizontalShift = when {
      entryState.position > 0 -> randomBetween(MinLineShift.toPx(), MaxLineShift.toPx()) * direction
      else -> 0f
    }
    val horizontalOffset = when (entryState.message.sender) {
      Sender.Ren -> Offset(0f, 0f)
      else -> Offset(horizontalShift, 0f)
    }

    entryState.lineCoordinates = entryState.lineCoordinates.copy(
      leftPoint = entryState.lineCoordinates.leftPoint + horizontalOffset,
      rightPoint = entryState.lineCoordinates.rightPoint + horizontalOffset,
    )

    coroutineScope.launch {
      entryState.lineProgress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 180 * AnimationDurationScale)
      )
    }
  }

  // TODO: Consider moving out of companion object.
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

/**
 * Allows us to keep track of some mutable state associated with an individual entry in the
 * transcript.
 */
private class EntryState(
  val position: Int,
  val message: Message,
  val lineProgress: Animatable<Float, AnimationVector1D>,
  val avatarBackgroundScale: Animatable<Float, AnimationVector1D>,
  val avatarForegroundScale: Animatable<Float, AnimationVector1D>,
  val messageHorizontalScale: Animatable<Float, AnimationVector1D>,
  val messageVerticalScale: Animatable<Float, AnimationVector1D>,
  val messageTextAlpha: Animatable<Float, AnimationVector1D>,
  var lineCoordinates: LineCoordinates,
)

private fun EntryState.toEntry() = Entry(
  message = message,
  lineCoordinates = lineCoordinates,
  avatarBackgroundScale = avatarBackgroundScale.asState(),
  avatarForegroundScale = avatarForegroundScale.asState(),
  messageHorizontalScale = messageHorizontalScale.asState(),
  messageVerticalScale = messageVerticalScale.asState(),
  messageTextAlpha = messageTextAlpha.asState(),
  lineProgress = lineProgress.asState(),
)

private val MinLineWidth = 44.dp
private val MaxLineWidth = 60.dp

private val MinLineShift = 16.dp
private val MaxLineShift = 48.dp

private val RenMessageCenter = DpOffset(x = 60.dp, y = 28.dp)

private fun randomBetween(start: Float, end: Float): Float {
  return start + Random.nextFloat() * (end - start)
}