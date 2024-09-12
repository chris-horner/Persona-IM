package codes.chrishorner.personasns

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

/**
 * Not something that existed in the game, but this wraps a component so that tapping on it spawns
 * a menu to choose the current season - dictating the particles shown in the app's background.
 */
@Composable
fun SeasonMenu(
  hostElement: @Composable () -> Unit,
  onSeasonChange: (Season) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showPopup by remember { mutableStateOf(false) }
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (pressed) 0.90f else 1f, label = "scale")

  Box(
    contentAlignment = Alignment.TopEnd,
    modifier = modifier,
  ) {
    Box(
      modifier = Modifier
        .scale(scale)
        .clickable(
          interactionSource = interaction,
          indication = null,
          onClick = { showPopup = true },
        ),
    ) {
      hostElement()
    }

    SeasonPopupMenu(
      show = showPopup,
      onDismissRequest = { showPopup = false },
      onSeasonChange = onSeasonChange,
    )
  }
}

@Composable
private fun SeasonPopupMenu(
  show: Boolean,
  onDismissRequest: () -> Unit,
  onSeasonChange: (Season) -> Unit,
) {
  if (!show) return

  Popup(onDismissRequest = onDismissRequest) {
    Column(
      modifier = Modifier
        .menuBackground()
        .padding(horizontal = 40.dp, vertical = 12.dp)
    ) {
      Season.entries.forEach { season ->
        SeasonOption(
          season = season,
          onClick = {
            onDismissRequest()
            onSeasonChange(season)
          },
        )
      }
    }
  }
}

@Composable
private fun SeasonOption(season: Season, onClick: () -> Unit) {
  Text(
    text = season.name.lowercase().replaceFirstChar { it.uppercaseChar() },
    fontSize = 30.sp,
    fontFamily = OptimaNova,
    color = Color.Black,
    modifier = Modifier.clickable { onClick() }
  )
}

private fun Modifier.menuBackground(): Modifier {
  return this.drawBehind {
    val outerBox = GenericShape { size, _ ->
      moveTo(0f, 0f)
      lineTo(size.width - 35.dp.toPx(), 4.dp.toPx())
      lineTo(size.width - 10.7.dp.toPx(), size.height - 6.6.dp.toPx())
      lineTo(35.5.dp.toPx(), size.height)
      close()
    }

    val innerBox = GenericShape { size, _ ->
      moveTo(12.dp.toPx(), 5.dp.toPx())
      lineTo(size.width - 36.dp.toPx(), 9.5.dp.toPx())
      lineTo(size.width - 16.4.dp.toPx(), size.height - 11.7.dp.toPx())
      lineTo(36.5.dp.toPx(), size.height - 3.5.dp.toPx())
      close()
    }

    drawOutline(Outline(outerBox), Color.Black)
    drawOutline(Outline(innerBox), Color.White)
  }
}