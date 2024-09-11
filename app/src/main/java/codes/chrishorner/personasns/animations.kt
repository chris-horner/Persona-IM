package codes.chrishorner.personasns

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.Easing

/**
 * Increase this value to slow animations down.
 */
const val AnimationDurationScale = 1

// Temp work around for https://issuetracker.google.com/issues/354405919
val BetterEaseOutBack: Easing = Easing { fraction ->
  try {
    EaseOutBack.transform(fraction)
  } catch (e: IllegalArgumentException) {
    1f
  }
}