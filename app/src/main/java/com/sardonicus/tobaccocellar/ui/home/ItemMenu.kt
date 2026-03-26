package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.items.RatingPopup
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

@Composable
fun ItemMenu(
    viewModel: HomeViewModel,
    activeItemId: () -> Int,
    onEditClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickEditState by viewModel.quickEditState.collectAsState()

    var quickEdit by rememberSaveable { mutableStateOf(false) }
    val onQuickEdit: () -> Unit = { quickEdit = !quickEdit }

    var showRatingPop by rememberSaveable { mutableStateOf(false) }
    val onShowRatingPop: (Boolean) -> Unit = { showRatingPop = it }

    LaunchedEffect(Unit) {
        delay(150)
        viewModel.setQuickEditItem(activeItemId())
    }

    AnimatedContent(
        targetState = quickEdit,
        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
        modifier = modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.listMenuScrim)
    ) { quickMenu ->
        Row(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!quickMenu) {
                TextButton(
                    onClick = {
                        onEditClick()
                        onMenuDismiss()
                    },
                    modifier = Modifier,
                     //   .padding(end = 8.dp),
                ) {
                    Text(
                        text = "Edit Item",
                        modifier = Modifier,
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                TextButton(
                    onClick = onQuickEdit,
                    modifier = Modifier,
                ) {
                    Text(
                        text = "Quick Edit",
                        modifier = Modifier,
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Box (
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onQuickEdit() }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                    )
                }

                val rating = remember(quickEditState.rating) { formatDecimal(quickEditState.rating) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onShowRatingPop(true) }
                            .padding(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = rating.ifBlank { "-.-" },
                            autoSize = TextAutoSize.StepBased(10.sp, 14.sp, 0.1.sp),
                            modifier = Modifier,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 1.em
                        )

                        Image(
                            painter = painterResource(id = R.drawable.star_filled),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalCustomColors.current.starRating),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier,
                        )

                    }
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        CustomCheckbox(
                            checked = quickEditState.favorite,
                            onCheckedChange = viewModel::updateQuickFavorite,
                            checkedIcon = R.drawable.heart_filled_24,
                            uncheckedIcon = R.drawable.heart_outline_24,
                            modifier = Modifier
                                .padding(vertical = 8.dp),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContentColor = LocalCustomColors.current.favHeart,
                            )
                        )

                        CustomCheckbox(
                            checked = quickEditState.disliked,
                            onCheckedChange = viewModel::updateQuickDislike,
                            checkedIcon = R.drawable.heartbroken_filled_24,
                            uncheckedIcon = R.drawable.heartbroken_outlined_24,
                            modifier = Modifier
                                .padding(vertical = 8.dp),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContentColor = LocalCustomColors.current.disHeart,
                            )
                        )
                    }
                }

                TextButton(
                    onClick = viewModel::saveQuickEdits,
                    modifier = Modifier,
                ) {
                    Text(
                        text = "Save",
                        modifier = Modifier,
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.SemiBold,
                      //  fontSize = 14.sp
                    )
                }
            }
        }

        if (showRatingPop) {
            RatingPopup(
                currentRating = quickEditState.rating,
                onDismiss = { onShowRatingPop(false) },
                onRatingSelected = {
                    viewModel.updateQuickRating(it)
                    onShowRatingPop(false)
                }
            )
        }
    }
}