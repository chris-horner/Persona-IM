package codes.chrishorner.personasns

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

fun randomBetween(start: Float, end: Float): Float {
  return start + Random.nextFloat() * (end - start)
}

fun randomBetween(start: Dp, end: Dp): Dp {
  return randomBetween(start.value, end.value).dp
}

fun Density.randomPxBetween(start: Dp, end: Dp): Float {
  return randomBetween(start, end).toPx()
}