package codes.chrishorner.personasns

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class Message(
  val sender: Sender,
  val text: String,
)

enum class Sender(@DrawableRes val image: Int, val color: Color) {
  Ann(image = R.drawable.ann, color = Color(0xFFFE93C9)),
  Ryuji(image = R.drawable.ryuji, color = Color(0xFFF0EA40)),
  Yusuke(image = R.drawable.yusuke, color = Color(0xFF1BC8F9)),

  // Ren is the player character, and has no avatar in chat.
  Ren(image = -1, color = Color.Unspecified),
}

/**
 * Kind of like the "business logic" of the app - put into a simple class for the purposes of
 * this demo app.
 */
class MessagesState {
  private var count = 0

  fun advance(): ImmutableList<Message> {
    count++

    if (count > Messages.size) {
      count = 1
    }

    return Messages.take(count).toImmutableList()
  }
}

private val Messages = persistentListOf(
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
  Message(
    sender = Sender.Yusuke,
    text = "In other words, Maaku will be going. I have no objections.",
  ),
  Message(
    sender = Sender.Yusuke,
    text = "Tricking people and using that as blackmail… These bastards are true cowards.",
  ),
  Message(
    sender = Sender.Ann,
    text = "It’s kinda scary to think people like that are all around us in this city...",
  ),
  Message(
    sender = Sender.Ryuji,
    text = "Well guys, we gotta brace ourselves. We’re up against a serious criminal here.",
  ),
)