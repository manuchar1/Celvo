package com.mtislab.core.designsystem.utils

import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.ic_asia
import com.celvo.core.designsystem.resources.ic_europe
import com.celvo.core.designsystem.resources.ic_globe
import com.celvo.core.designsystem.resources.ic_latino_america
import com.celvo.core.designsystem.resources.ic_north_america
import org.jetbrains.compose.resources.DrawableResource

fun getRegionIcon(id: String): DrawableResource {
    return when (id.trim().lowercase()) {
        "asia" -> Res.drawable.ic_asia
        "europe" -> Res.drawable.ic_europe
        "north-america", "na" -> Res.drawable.ic_north_america
        "south-america", "latin-america", "la" -> Res.drawable.ic_latino_america
        "africa" -> Res.drawable.ic_globe
        "middle-east"-> Res.drawable.ic_globe
        else -> Res.drawable.ic_globe
    }
}