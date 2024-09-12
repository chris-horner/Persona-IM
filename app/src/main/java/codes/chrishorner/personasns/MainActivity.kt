package codes.chrishorner.personasns

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.collections.immutable.toImmutableList

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      val transcriptState = rememberTranscriptState()
      var season by remember { mutableStateOf(Season.NONE) }

      RootContainer {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(color = PersonaRed)
        ) {
          BackgroundParticles(season)

          Image(
            painter = painterResource(R.drawable.bg_splatter_background),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
              .statusBarsPadding()
              .offset(y = (-16).dp)
          )

          val entries = transcriptState.entries
          Transcript(entries)

          Row(modifier = Modifier.statusBarsPadding()
          ) {
            SeasonMenu(
              hostElement = {
                Image(
                  painter = painterResource(R.drawable.logo_im),
                  contentDescription = null,
                  modifier = Modifier.height(100.dp)
                )
              },
              onSeasonChange = { season = it},
              modifier = Modifier.offset(x = 8.dp, y = (-4).dp),
            )

            Portraits(
              senders = Sender.entries.minus(Sender.Ren).toImmutableList(),
              modifier = Modifier.weight(1f),
            )
          }

          NextButton(
            onClick = { transcriptState.advance() },
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
      .scale(scale)
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
