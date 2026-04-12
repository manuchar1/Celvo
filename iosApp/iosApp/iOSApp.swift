import SwiftUI
import ComposeApp
import GoogleSignIn
import SafariServices

@main
struct iOSApp: App {

    init() {
        InitKoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    if GIDSignIn.sharedInstance.handle(url) {
                        return
                    }

                    // Dismiss SFSafariViewController if it's showing (payment return flow)
                    if url.scheme == "com.mtislab.celvo" && url.host == "payment" {
                        dismissPresentedSafari()
                    }

                    DeepLinkHandler.shared.handleDeepLink(url: url.absoluteString)
                }
        }
    }

    private func dismissPresentedSafari() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else { return }

        var topController = rootViewController
        while let presented = topController.presentedViewController {
            topController = presented
        }

        if topController is SFSafariViewController {
            topController.dismiss(animated: true)
        }
    }
}
