package ly.com.tahaben.onboarding_presentaion.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import ly.com.tahaben.core.R
import ly.com.tahaben.core.navigation.Routes
import ly.com.tahaben.core_ui.Black
import ly.com.tahaben.core_ui.LocalSpacing
import ly.com.tahaben.onboarding_presentaion.components.MainScreenCard

@androidx.compose.runtime.Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    tip: String,
    isGrayscaleEnabled: Boolean,
    isInfiniteScrollBlockerEnabled: Boolean,
    isNotificationFilterEnabled: Boolean,
    navController: NavHostController
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.spaceLarge)
    ) {
        Spacer(modifier = Modifier.height(spacing.spaceExtraLarge))
        Text(
            text = stringResource(id = R.string.hello),
            style = MaterialTheme.typography.h1,
            color = Black
        )
        Spacer(modifier = Modifier.height(spacing.spaceMedium))
        Row(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ic_tip),
                contentDescription = stringResource(R.string.tip_icon_description)
            )
            Spacer(modifier = Modifier.width(spacing.spaceExtraSmall))
            Text(
                text = tip,
                style = MaterialTheme.typography.h5,
                color = Black
            )
        }
        Spacer(modifier = Modifier.height(spacing.spaceLarge))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MainScreenCard(
                text = stringResource(R.string.usage),
                status = "",
                iconId = R.drawable.ic_usage,
                onClick = { navController.navigate(Routes.USAGE) })
            MainScreenCard(
                text = stringResource(R.string.notifications_filter),
                status = if (isNotificationFilterEnabled) stringResource(id = R.string.enabled) else stringResource(
                    id = R.string.disabled
                ),
                iconId = R.drawable.ic_notification,
                onClick = { navController.navigate(Routes.NOTIFICATION_FILTER) })
        }
        Spacer(modifier = Modifier.height(spacing.spaceMedium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MainScreenCard(
                text = stringResource(R.string.grayscale),
                status = if (isGrayscaleEnabled) stringResource(id = R.string.enabled) else stringResource(
                    id = R.string.disabled
                ),
                iconId = null,
                onClick = { navController.navigate(Routes.SCREEN_GRAY_SCALE) })
            MainScreenCard(
                text = stringResource(R.string.infinite_scrolling),
                status = if (isInfiniteScrollBlockerEnabled) stringResource(id = R.string.enabled) else stringResource(
                    id = R.string.disabled
                ),
                iconId = null,
                onClick = { navController.navigate(Routes.INFINITE_SCROLLING) })
        }
    }
}