package com.mtislab.core.designsystem.components.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.offline_instructions_title
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.headers.CelvoDetailHeader
import com.mtislab.core.designsystem.theme.CelvoGreen500Alpha15
import com.mtislab.core.designsystem.theme.CelvoPurple500
import com.mtislab.core.designsystem.theme.CelvoPurple500Alpha15
import com.mtislab.core.designsystem.theme.CelvoYellow
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

/**
 * "How to install your eSIM" guide, opened from the home screen help (?) button.
 *
 * Shows a single, fully-expanded guide for the current platform only — Android
 * devices see the Android steps, iPhones see the iOS steps ([currentInstallGuidePlatform]).
 */
@Composable
fun InstallInstructionsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    platform: InstallGuidePlatform = currentInstallGuidePlatform()
) {
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            CelvoDetailHeader(
                title = stringResource(Res.string.offline_instructions_title),
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 32.dp)
            ) {
                InstallGuideCard(platform = platform)
            }
        }
    }
}


// ─── Guide card ──────────────────────────────────────────────────────────────

@Composable
private fun InstallGuideCard(platform: InstallGuidePlatform) {
    val colors = MaterialTheme.colorScheme.extended
    val content = remember(platform) { guideContent(platform) }

    CelvoCard(
        shape = RoundedCornerShape(26.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        // Header — mascot + title (always expanded, no toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CelvoGreen500Alpha15),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = colors.success
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "How to install your eSIM",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = content.platformName,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " · 3 quick steps",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = colors.divider
        )

        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp)
        ) {
            var stepNo = 0
            content.groups.forEachIndexed { groupIndex, group ->
                if (groupIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                SectionLabel(group.label)
                Spacer(modifier = Modifier.height(14.dp))
                group.steps.forEachIndexed { stepIndex, step ->
                    stepNo++
                    StepRow(
                        number = stepNo,
                        step = step,
                        isLast = stepIndex == group.steps.lastIndex
                    )
                }
            }

            ImportantNote(notes = IMPORTANT_NOTES)
        }
    }
}


// ─── Section label ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    val colors = MaterialTheme.colorScheme.extended
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.uppercase(),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = colors.textTertiary
        )
        Spacer(modifier = Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = colors.divider
        )
    }
}


// ─── Step row (numbered badge + connector line) ──────────────────────────────

@Composable
private fun StepRow(
    number: Int,
    step: GuideStep,
    isLast: Boolean
) {
    val colors = MaterialTheme.colorScheme.extended
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Badge + connector lane
        Column(
            modifier = Modifier.width(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CelvoGreen500Alpha15),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = colors.success
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(colors.divider)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = step.title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.desc,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                color = colors.textSecondary
            )
            if (step.handoff) {
                Spacer(modifier = Modifier.height(10.dp))
                HandoffPill()
            }
        }
    }
}


// ─── "Leave the app" pill ────────────────────────────────────────────────────

@Composable
private fun HandoffPill() {
    val colors = MaterialTheme.colorScheme.extended
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CelvoPurple500Alpha15)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(CelvoPurple500),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(12.dp)
                    .rotate(-45f)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "You’ll leave the app — that’s normal",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.textLink
        )
    }
}


// ─── Important note ──────────────────────────────────────────────────────────

@Composable
private fun ImportantNote(notes: List<String>) {
    val colors = MaterialTheme.colorScheme.extended
    val dark = isSystemInDarkTheme()
    val amberText = if (dark) CelvoYellow else Color(0xFF9A6B12)
    val amberGlyph = Color(0xFF7A5410)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CelvoYellow.copy(alpha = if (dark) 0.12f else 0.20f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CelvoYellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = amberGlyph,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSans
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "IMPORTANT",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                color = amberText
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            notes.forEach { note ->
                Row {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(amberText.copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}


// ─── Content ─────────────────────────────────────────────────────────────────

private data class GuideStep(
    val title: String,
    val desc: String,
    val handoff: Boolean = false
)

private data class GuideGroup(
    val label: String,
    val steps: List<GuideStep>
)

private data class GuideContent(
    val platformName: String,
    val groups: List<GuideGroup>
)

private val IMPORTANT_NOTES = listOf(
    "Install while connected to Wi-Fi.",
    "Don’t delete the eSIM if you plan to top up later."
)

private fun guideContent(platform: InstallGuidePlatform): GuideContent = when (platform) {
    InstallGuidePlatform.ANDROID -> GuideContent(
        platformName = "Android",
        groups = listOf(
            GuideGroup(
                label = "In the app",
                steps = listOf(
                    GuideStep("Open My eSIMs and tap Install", "Or “Install eSIM” right after you buy."),
                    GuideStep("Tap Continue to open settings", "Your phone will ask to open eSIM settings.", handoff = true)
                )
            ),
            GuideGroup(
                label = "On your phone",
                steps = listOf(
                    GuideStep("Tap Download", "Your plan details are already filled in."),
                    GuideStep("Wait a few seconds", "Keep the screen open while the eSIM downloads."),
                    GuideStep("Name it “Travel”", "Optional — then tap Done to finish.")
                )
            ),
            GuideGroup(
                label = "Before you travel",
                steps = listOf(
                    GuideStep("Turn on the Celvo line", "Settings → Network & internet → SIMs."),
                    GuideStep("Turn on Roaming on arrival", "Data starts automatically when you land.")
                )
            )
        )
    )

    InstallGuidePlatform.IOS -> GuideContent(
        platformName = "iPhone",
        groups = listOf(
            GuideGroup(
                label = "In the app",
                steps = listOf(
                    GuideStep("Open My eSIMs and tap Install", "Or “Install eSIM” right after you buy."),
                    GuideStep("Tap Continue to open Settings", "iPhone will ask to open Settings.", handoff = true)
                )
            ),
            GuideGroup(
                label = "On your iPhone",
                steps = listOf(
                    GuideStep("Add the cellular plan", "On “Add eSIM”, tap Continue → details are pre-filled."),
                    GuideStep("Label the plan", "Name it e.g. “Travel”, then tap Continue."),
                    GuideStep("Use Celvo for Cellular Data", "Keep your own number for calls.")
                )
            ),
            GuideGroup(
                label = "Before you travel",
                steps = listOf(
                    GuideStep("Turn On This Line", "Settings → Cellular → your Celvo line."),
                    GuideStep("Turn on Data Roaming", "Data starts the moment you land.")
                )
            )
        )
    )
}
