package codes.chrishorner.personasns

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
import codes.chrishorner.personasns.TranscriptSizes.MaxLineShift
import codes.chrishorner.personasns.TranscriptSizes.MaxLineWidth
import codes.chrishorner.personasns.TranscriptSizes.MinLineShift
import codes.chrishorner.personasns.TranscriptSizes.MinLineWidth
import codes.chrishorner.personasns.TranscriptSizes.RenMessageCenter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberTranscriptState(): TranscriptState {
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()
  return remember(density) { TranscriptState(density, coroutineScope) }
}

/** A message in the transcript with every needed for rendering. */
data class Entry(
  val message: Message,
  val lineCoordinates: LineCoordinates,
  val drawPunctuation: Boolean,
  /** Animated percentage progress of the black line connecting this message to the next. */
  val lineProgress: State<Float>,
  val avatarBackgroundScale: State<Float>,
  val avatarForegroundScale: State<Float>,
  val messageHorizontalScale: State<Float>,
  val messageVerticalScale: State<Float>,
  val messageTextAlpha: State<Float>,
  val punctuationScale: State<Float>,
)

/**
 * Represents where the black background line connects to a message entry - relative to its avatar.
 */
data class LineCoordinates(
  val leftPoint: Offset,
  val rightPoint: Offset,
)

/**
 * Responsible for keeping track of all the current entries in the transcript, and animating them
 * over time.
 */
@Stable
class TranscriptState internal constructor(
  private val density: Density,
  private val coroutineScope: CoroutineScope,
) {

  private val messagesState = MessagesState()

  /** An associative array of states kept in sync with [messagesState]. */
  private val entryStates = mutableListOf<EntryState>()
  private val _entries = mutableStateOf<ImmutableList<Entry>>(persistentListOf())
  val entries: ImmutableList<Entry> by _entries

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
        val leftX = (TranscriptSizes.AvatarSize.width.toPx() / 2f) - (width / 2f)
        val y = TranscriptSizes.AvatarSize.height.toPx() / 2f
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
          delay(160L * AnimationDurationScale)
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
      messageHorizontalScale = Animatable(0.3f).apply {
        coroutineScope.launch {
          animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = 180 * AnimationDurationScale,
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
              durationMillis = 180 * AnimationDurationScale,
              easing = BetterEaseOutBack,
            ),
          )
        }
      },
      messageTextAlpha = Animatable(0f).apply {
        coroutineScope.launch {
          delay(100L * AnimationDurationScale)
          animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 130 * AnimationDurationScale),
          )
        }
      },
      punctuationScale = Animatable(0f).apply {
        if (message.text.endsWith('?')) {
          coroutineScope.launch {
            delay(130L * AnimationDurationScale)
            snapTo(0.4f)
            animateTo(
              targetValue = 1f,
              animationSpec = tween(durationMillis = 100 * AnimationDurationScale),
            )
          }
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
}

object TranscriptSizes {
  val AvatarSize = DpSize(110.dp, 90.dp)
  val EntrySpacing = 16.dp
  val RenMessageCenter = DpOffset(x = 60.dp, y = 28.dp)
  val MinLineShift = 16.dp
  val MaxLineShift = 48.dp
  val MinLineWidth = 44.dp
  val MaxLineWidth = 60.dp

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
  val punctuationScale: Animatable<Float, AnimationVector1D>,
  var lineCoordinates: LineCoordinates,
)

private fun EntryState.toEntry() = Entry(
  message = message,
  lineCoordinates = lineCoordinates,
  drawPunctuation = message.text.endsWith('?'),
  avatarBackgroundScale = avatarBackgroundScale.asState(),
  avatarForegroundScale = avatarForegroundScale.asState(),
  messageHorizontalScale = messageHorizontalScale.asState(),
  messageVerticalScale = messageVerticalScale.asState(),
  messageTextAlpha = messageTextAlpha.asState(),
  punctuationScale = punctuationScale.asState(),
  lineProgress = lineProgress.asState(),
)