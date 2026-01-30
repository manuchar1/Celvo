import SwiftUI
import ComposeApp


@main
struct iOSApp: App {

    init() {
        InitKoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    DeepLinkHandler.shared.handleDeepLink(url: url.absoluteString)
                }
        }
    }
}
