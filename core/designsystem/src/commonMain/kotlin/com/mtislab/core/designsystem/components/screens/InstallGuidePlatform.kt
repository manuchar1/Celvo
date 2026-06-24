package com.mtislab.core.designsystem.components.screens

/**
 * The mobile platform the install guide should render content for.
 *
 * The app shows ONLY the relevant platform's instructions — Android devices
 * see the Android steps, iPhones see the iOS steps. There is no in-app switcher.
 */
enum class InstallGuidePlatform { ANDROID, IOS }

/**
 * Returns the current platform so [InstallInstructionsScreen] can pick the right
 * step-by-step content. Resolved per source set (Android / iOS / JVM-preview).
 */
expect fun currentInstallGuidePlatform(): InstallGuidePlatform
