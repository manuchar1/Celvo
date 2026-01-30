package com.mtislab.core.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

@Composable
actual fun rememberBrowserOpener(): (String) -> Unit {
    return remember {
        { urlString ->
            val nsUrl = NSURL(string = urlString)
            val safariViewController = SFSafariViewController(uRL = nsUrl)

            val window = UIApplication.sharedApplication.windows.firstOrNull {
                (it as? UIWindow)?.isKeyWindow() == true
            } as? UIWindow

            val rootViewController = window?.rootViewController

            if (rootViewController != null) {
                var topController = rootViewController
                while (topController?.presentedViewController != null) {
                    topController = topController.presentedViewController
                }

                topController?.presentViewController(safariViewController, animated = true, completion = null)
            } else {
                if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
                    UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any?>(), null)
                }
            }
        }
    }
}