package codes.chrishorner.personasns

import android.app.Activity
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import codes.chrishorner.personasns.ui.theme.PersonaRed
import kotlin.random.Random

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      RootContainer {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(color = PersonaRed)
        ) {
          Image(
            painter = painterResource(R.drawable.bg_splatter_background),
            contentDescription = null,
            modifier = Modifier
              .statusBarsPadding()
              .offset(y = (-16).dp)
          )

          val scale by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
              tween(durationMillis = 1_000),
              repeatMode = RepeatMode.Reverse
            ),
          )

          val state = rememberLazyListState()
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(28.dp),
            state = state,
            contentPadding = WindowInsets.systemBars
              .add(WindowInsets(top = 100.dp))
              .asPaddingValues(),
            modifier = Modifier.fillMaxSize()
          ) {
            itemsIndexed(Transcript) { index, message ->
              if (message.sender == Sender.Ren) {
                Reply(
                  text = message.text,
                  modifier = Modifier.drawConnectingLine(message, Transcript.getOrNull(index + 1))
                )
              } else {
                Entry(
                  avatarImage = message.sender.image,
                  color = message.sender.color,
                  text = message.text,
                  modifier = Modifier.drawConnectingLine(message, Transcript.getOrNull(index + 1))
                )
              }
            }
          }

          Image(
            painter = painterResource(R.drawable.logo_im),
            contentDescription = null,
            modifier = Modifier
              .statusBarsPadding()
              .height(100.dp)
              .offset(x = 8.dp, y = (-4).dp),
          )

          NextButton(
            onClick = { /*TODO*/ },
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .systemBarsPadding()
              .padding(16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun NextButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (pressed) 0.90f else 1f, label = "scale")

  Box(
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick,
      ),
  ) {
    Image(
      painter = painterResource(R.drawable.next),
      contentDescription = "Next button",
    )
  }
}

private val linePath = Path()
private val startOffset = 44.dp
private val endOffset = 72.dp
private val renOffset = 156.dp

private fun Modifier.drawConnectingLine(message1: Message, message2: Message?): Modifier {
  if (message2 == null) return this

  return drawBehind {
    val xOffset1 = if (message1.sender == Sender.Ren) size.width - renOffset.toPx() else 0f
    val xOffset2 = if (message2.sender == Sender.Ren) size.width - renOffset.toPx() else 0f
    val startY = startOffset.toPx()
    val endY = size.height + endOffset.toPx()

    with(linePath) {
      reset()
      moveTo(message1.linePoints.x1.toPx() + xOffset1, startY)
      lineTo(message1.linePoints.x2.toPx() + xOffset1, startY)
      lineTo(message2.linePoints.x2.toPx() + xOffset2, endY)
      lineTo(message2.linePoints.x1.toPx() + xOffset2, endY)
      close()
    }

    drawPath(linePath, Color.Black)
  }
}

private val OptimaNova = FontFamily(
  Font(R.font.optima_nova_black, weight = FontWeight.Black)
)

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
) {
  val linePoints = randomLinePoints(sender)
}

// https://www.youtube.com/watch?v=Bfqeo056MwA
// https://www.quotev.com/story/9496702/Persona-5-with-YN/43
val Transcript = listOf(
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

class LinePoints(val x1: Dp, val x2: Dp)

fun randomLinePoints(sender: Sender): LinePoints {
  val width = if (sender == Sender.Ren) {
    Random.nextInt(80, 112)
  } else {
    Random.nextInt(30, 52)
  }
  val startX = Random.nextInt(12, 60)
  val endX = startX + width
  return LinePoints(startX.dp, endX.dp)
}

@Composable
private fun Entry(
  @DrawableRes avatarImage: Int,
  color: Color,
  text: String,
  modifier: Modifier = Modifier,
) {
  EntryLayout(
    avatar = { Avatar(avatarImage, color) },
    text = {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        fontFamily = OptimaNova,
        modifier = Modifier
          .drawWithCache {
            val outerBoxStem = Outline(OuterStem())
            val outerBoxShape = OuterBox()
            val outerBox = Outline(outerBoxShape)
            val innerBoxStem = Outline(InnerStem())
            val innerBox = Outline(InnerBox())
            val shadowPaint = Paint().apply {
              this.color = Color.Black
              alpha = 0.3f
              asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
            }

            onDrawBehind {
              drawIntoCanvas { it.drawOutline(outerBox, shadowPaint) }
              drawOutline(outerBox, color = Color.White)
              drawOutline(outerBoxStem, color = Color.White)
              drawOutline(innerBoxStem, color = Color.Black)
              drawOutline(innerBox, color = Color.Black)
            }
          }
          .padding(start = 42.dp, top = 20.dp, end = 32.dp, bottom = 20.dp)
      )
    },
    modifier = modifier.padding(horizontal = 8.dp)
  )
}

@Composable
private fun EntryLayout(
  avatar: @Composable () -> Unit,
  text: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Layout(
    content = {
      avatar()
      text()
    },
    modifier = modifier,
  ) { (avatarMeasurable, textMeasurable), constraints ->
    val textOverlap = 18.dp.roundToPx()
    val textVerticalOffset = 4.dp.roundToPx()

    val avatarPlaceable = avatarMeasurable.measure(constraints)
    val textMaxWidth = constraints.maxWidth - avatarPlaceable.width + textOverlap
    val textConstraints = constraints.copy(maxWidth = textMaxWidth)
    val textPlaceable = textMeasurable.measure(textConstraints)

    val width = avatarPlaceable.width + textPlaceable.width - textOverlap
    val height = maxOf(avatarPlaceable.height, textPlaceable.height)
    layout(width, height) {
      avatarPlaceable.place(0, 0)
      val textY =
        (avatarPlaceable.height - textPlaceable.height - textVerticalOffset).coerceAtLeast(0)
      textPlaceable.place(avatarPlaceable.width - textOverlap, textY)
    }
  }
}

@Composable
private fun Reply(text: String, modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.CenterEnd,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black,
      fontFamily = OptimaNova,
      modifier = Modifier
        .drawWithCache {
          val outerStem = Outline(ReplyOuterStem())
          val outerBoxShape = ReplyOuterBox()
          val outerBox = Outline(outerBoxShape)
          val innerStem = Outline(ReplyInnerStem())
          val innerBox = Outline(ReplyInnerBox())
          val shadowPaint = Paint().apply {
            this.color = Color.Black
            alpha = 0.3f
            asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
          }

          onDrawBehind {
            drawIntoCanvas { it.drawOutline(outerBox, shadowPaint) }
            drawOutline(outerBox, color = Color.Black)
            drawOutline(outerStem, color = Color.Black)
            drawOutline(innerStem, color = Color.White)
            drawOutline(innerBox, color = Color.White)
          }
        }
        .padding(start = 44.dp, top = 20.dp, end = 40.dp, bottom = 20.dp)
    )
  }
}

private val AvatarSize = DpSize(110.dp, 90.dp)

@Composable
private fun Avatar(@DrawableRes avatarImage: Int, color: Color) {
  Box(
    modifier = Modifier
      .size(AvatarSize)
      .drawBehind {
        drawOutline(Outline(AvatarBlackBox()), Color.Black)
        drawOutline(Outline(AvatarWhiteBox()), Color.White)
        drawOutline(Outline(AvatarColoredBox()), color)
      }
      .clip(with(LocalDensity.current) { AvatarClipBox() })
  ) {
    Image(
      painter = painterResource(avatarImage),
      contentDescription = null,
      modifier = Modifier
        .size(80.dp)
        .align(Alignment.TopEnd)
    )
  }
}

@Composable
private fun RootContainer(content: @Composable () -> Unit) {
  val view = LocalView.current
  val window = (view.context as Activity).window
  SideEffect {
    window.statusBarColor = Color.Black.copy(alpha = 0.3f).toArgb()
    window.navigationBarColor = Color.Transparent.toArgb()
  }
  content()
}

private fun DrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

private fun CacheDrawScope.Outline(shape: Shape): Outline {
  return shape.createOutline(size, layoutDirection, this)
}

@Composable
private fun OuterBox(): Shape {
  return LocalDensity.current.OuterBox()
}

private fun Density.OuterBox(): Shape = GenericShape { size, _ ->
  moveTo(31.7.dp.toPx(), 3.1.dp.toPx())
  lineTo(size.width, 0f)
  lineTo(size.width - 23.dp.toPx(), size.height)
  lineTo(15.6.dp.toPx(), size.height - 8.dp.toPx())
  close()
}

private fun Density.InnerBox(): Shape {
  return GenericShape { size, _ ->
    moveTo(33.dp.toPx(), 7.7.dp.toPx())
    lineTo(size.width - 13.dp.toPx(), 3.7.dp.toPx())
    lineTo(size.width - 25.7.dp.toPx(), size.height - 4.6.dp.toPx())
    lineTo(20.4.dp.toPx(), size.height - 12.dp.toPx())
    close()
  }
}

private fun Density.getStemY(boxHeight: Float): Float {
  return if (boxHeight > AvatarSize.height.toPx()) {
    boxHeight - 16.dp.roundToPx()
  } else {
    boxHeight - 4.dp.roundToPx()
  }
}

private fun Density.OuterStem(): Shape = GenericShape { size, _ ->
  val verticalOrigin = getStemY(size.height)

  moveTo(0f, verticalOrigin - 19.2.dp.toPx())
  lineTo(19.5.dp.toPx(), verticalOrigin - 37.2.dp.toPx())
  lineTo(20.8.dp.toPx(), verticalOrigin - 31.5.dp.toPx())
  lineTo(32.4.dp.toPx(), verticalOrigin - 39.3.dp.toPx())
  lineTo(26.6.dp.toPx(), verticalOrigin - 15.8.dp.toPx())
  lineTo(11.7.dp.toPx(), verticalOrigin - 12.6.dp.toPx())
  lineTo(10.dp.toPx(), verticalOrigin - 20.dp.toPx())
  close()
}

private fun Density.InnerStem(): Shape = GenericShape { size, _ ->
  val verticalOrigin = getStemY(size.height)

  moveTo(4.6.dp.toPx(), verticalOrigin - 22.2.dp.toPx())
  lineTo(17.dp.toPx(), verticalOrigin - 33.2.dp.toPx())
  lineTo(19.3.dp.toPx(), verticalOrigin - 28.1.dp.toPx())
  lineTo(30.4.dp.toPx(), verticalOrigin - 32.7.dp.toPx())
  lineTo(27.dp.toPx(), verticalOrigin - 21.4.dp.toPx())
  lineTo(14.4.dp.toPx(), verticalOrigin - 18.6.dp.toPx())
  lineTo(12.8.dp.toPx(), verticalOrigin - 25.4.dp.toPx())
  close()
}

private fun Density.AvatarColoredBox(): Shape = GenericShape { _, _ ->
  moveTo(22.5.dp.toPx(), 28.dp.toPx())
  lineTo(94.4.dp.toPx(), 31.4.dp.toPx())
  lineTo(104.3.dp.toPx(), 67.5.dp.toPx())
  lineTo(40.dp.toPx(), 76.6.dp.toPx())
  close()
}

private fun Density.AvatarWhiteBox(): Shape = GenericShape { _, _ ->
  moveTo(16.4.dp.toPx(), 20.5.dp.toPx())
  lineTo(96.7.dp.toPx(), 30.4.dp.toPx())
  lineTo(106.4.dp.toPx(), 70.dp.toPx())
  lineTo(37.8.dp.toPx(), 80.4.dp.toPx())
  close()
}

private fun Density.AvatarBlackBox(): Shape = GenericShape { _, _ ->
  moveTo(0f, 17.dp.toPx())
  lineTo(100.5.dp.toPx(), 27.2.dp.toPx())
  lineTo(110.dp.toPx(), 72.7.dp.toPx())
  lineTo(33.4.dp.toPx(), 90.dp.toPx())
  close()
}

private fun Density.AvatarClipBox(): Shape = GenericShape { _, _ ->
  moveTo(10.3.dp.toPx(), (-5.6).dp.toPx())
  lineTo(114.7.dp.toPx(), (-5.6).dp.toPx())
  lineTo(114.7.dp.toPx(), 65.6.dp.toPx())
  lineTo(40.dp.toPx(), 76.6.dp.toPx())
  close()
}

private fun Density.ReplyOuterBox(): Shape = GenericShape { size, _ ->
  moveTo(0f, 0f)
  lineTo(size.width - 35.dp.toPx(), 4.dp.toPx())
  lineTo(size.width - 10.7.dp.toPx(), size.height - 6.6.dp.toPx())
  lineTo(35.5.dp.toPx(), size.height)
  close()
}

private fun Density.ReplyInnerBox(): Shape = GenericShape { size, _ ->
  moveTo(12.dp.toPx(), 5.dp.toPx())
  lineTo(size.width - 36.dp.toPx(), 9.5.dp.toPx())
  lineTo(size.width - 16.4.dp.toPx(), size.height - 11.7.dp.toPx())
  lineTo(36.5.dp.toPx(), size.height - 3.5.dp.toPx())
  close()
}

private fun Density.ReplyOuterStem(): Shape = GenericShape { size, _ ->
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

private fun Density.ReplyInnerStem(): Shape = GenericShape { size, _ ->
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
