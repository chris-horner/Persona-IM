package codes.chrishorner.personasns

import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class Sender(@DrawableRes val image: Int, val color: Color) {
  Ann(image = R.drawable.ann, color = Color(0xFFFE93C9)),
  Ryuji(image = R.drawable.ryuji, color = Color(0xFFF0EA40)),
  Yusuke(image = R.drawable.yusuke, color = Color(0xFF1BC8F9)),

  // Ren is the player character, and has no avatar in chat.
  Ren(image = -1, color = Color.Unspecified),
}

data class Message(
  val sender: Sender,
  val text: String,
)

/**
 * Represents how the black background line connects to a message.
 */
data class LineData(
  val widthVariation: Float,
  val horizontalOffset: Float,
)

data class Entry(
  val message: Message,
  val lineData: LineData,
)

class Transcript {
  val entries = mutableStateOf<List<Entry>>(emptyList())

  private val lineData = mutableListOf<LineData>()
  private var currentMessages = emptyList<Message>()

  private var count = 0
  private val zeroOffsetLineData = LineData(1f, 0f)

  fun advance() {
    count++

    if (count > Messages.size) {
      count = 1
      lineData.clear()
    }

    currentMessages = Messages.take(count)

    if (lineData.isNotEmpty()) {
      lineData[lineData.lastIndex] = when {
        lineData.lastIndex == 0 -> zeroOffsetLineData
        currentMessages.last().sender == Sender.Ren -> zeroOffsetLineData
        else -> generateLineData(lineData.lastIndex)
      }
    }

    lineData += zeroOffsetLineData

    entries.value = currentMessages.mapIndexed { index, message -> Entry(message, lineData[index]) }
  }

  private fun generateLineData(index: Int): LineData {
    fun randomBetween(start: Float, end: Float): Float {
      return start + Random.nextFloat() * (end - start)
    }

    return LineData(
      widthVariation = randomBetween(0.8f, 1.4f),
      horizontalOffset = when {
        index % 2 == 0 -> randomBetween(0.5f, 1f)
        else -> randomBetween(-0.5f, -1f)
      }
    )
  }
}

// https://www.youtube.com/watch?v=Bfqeo056MwA
// https://www.quotev.com/story/9496702/Persona-5-with-YN/43
private val Messages = listOf(
  Message(
    sender = Sender.Ann,
    text = "We have to find them tomorrow for sure. This is the only lead we have right now.",
  ),
  Message(
    sender = Sender.Yusuke,
    text = "Yes. It is highly likely that this part-time solicitor is somehow related to the mafia.",
  ),
  Message(
    sender = Sender.Yusuke,
    text = "If we tail him, he may lead us straight back to his boss.",
  ),
  Message(
    sender = Sender.Ryuji,
    text = "He talked to Iida and Nishiyama over at Central Street, right?",
  ),
  Message(
    sender = Sender.Yusuke,
    text = "Indeed, it seems that is where our target waits. But then... who should be the one to go?",
  ),
  Message(
    sender = Sender.Ren,
    text = "Morgana, I choose you.",
  ),
  Message(
    sender = Sender.Ann,
    text = "That's not a bad idea. Cats have nine lives, right? Morgana can spare one for this.",
  ),
  Message(
    sender = Sender.Ryuji,
    text = "Wouldn't the mafia get caught off guard if they had a cat coming to deliver for 'em?",
  ),
)