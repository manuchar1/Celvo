package com.mtislab.core.designsystem.legal

/**
 * Single source of truth for Celvo's published legal documents.
 *
 * These are the canonical, always-live web pages owned by Mtislab LLC. The app
 * only ever LINKS to them (opened in an in-app browser) — it never copies or
 * restates the legal text, so the documents stay in sync automatically.
 *
 * Reference these constants everywhere a legal link is shown; never hardcode the
 * URL string anywhere else.
 */
object LegalLinks {
    const val TERMS_OF_SERVICE = "https://celvoapp.com/terms"
    const val PRIVACY_POLICY = "https://celvoapp.com/privacy"
}
