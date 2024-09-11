package codes.chrishorner.personasns

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      val transcript = rememberTranscriptState()

      RootContainer {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(color = PersonaRed)
        ) {
          // TODO: Wire up a way to change season.
          BackgroundParticles(season = Season.NONE)

          Image(
            painter = painterResource(R.drawable.bg_splatter_background),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
              .statusBarsPadding()
              .offset(y = (-16).dp)
          )

          val entries = transcript.entries
          Transcript(entries)

          Row(
            modifier = Modifier
              .statusBarsPadding()
          ) {
            Image(
              painter = painterResource(R.drawable.logo_im),
              contentDescription = null,
              modifier = Modifier
                .height(100.dp)
                .offset(x = 8.dp, y = (-4).dp),
            )

            Portraits(Sender.entries.minus(Sender.Ren).toImmutableList())
          }

          NextButton(
            onClick = { transcript.advance() },
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
private fun Transcript(entries: ImmutableList<Entry>) {
  val listState = rememberLazyListState()
  val totalItemCount by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }

  LaunchedEffect(totalItemCount) {
    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
    // animateScrollToItem isn't super smooth, so if the newly added item is visible, then animate
    // manually with a tween.
    if (lastVisibleItem.index == totalItemCount - 1) {
      listState.animateScrollBy(
        value = lastVisibleItem.size.toFloat() + listState.layoutInfo.afterContentPadding,
        animationSpec = tween(durationMillis = 280),
      )
    } else {
      listState.animateScrollToItem(totalItemCount - 1)
    }
  }

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(TranscriptSizes.EntrySpacing),
    state = listState,
    contentPadding = WindowInsets.systemBars
      .add(WindowInsets(top = 100.dp, bottom = 100.dp))
      .asPaddingValues(),
    modifier = Modifier.fillMaxSize()
  ) {
    itemsIndexed(
      items = entries,
      key = { _, entry -> entry.message.text },
    ) { index, entry ->
      if (entry.message.sender == Sender.Ren) {
        Reply(
          entry = entry,
          modifier = Modifier.drawConnectingLine(entry, entries.getOrNull(index + 1))
        )
      } else {
        Entry(
          entry,
          modifier = Modifier.drawConnectingLine(entry, entries.getOrNull(index + 1))
          // Need to draw _down_ from the current item to properly draw _behind_.
        )
      }
    }

    item {
      TypingIndicator()
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
