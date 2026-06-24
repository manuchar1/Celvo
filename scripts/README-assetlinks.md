# Android App Links — assetlinks.json

`AndroidManifest.xml` declares `android:autoVerify="true"` for the BOG payment
redirect (`https://api.celvoapp.com/payment/result/...`). For Android to verify
the link and reopen the app instead of the browser, the server must host this
file at exactly:

    https://api.celvoapp.com/.well-known/assetlinks.json

## Steps

1. Get the **SHA-256** fingerprints:
   - **Upload key** (your keystore):
     ```
     keytool -list -v -keystore celvo-upload.jks -alias celvo-upload
     ```
   - **Play App Signing key**: Play Console → your app → Release → Setup →
     App Integrity → "App signing key certificate" → copy the SHA-256.
2. Replace both placeholders in `assetlinks.json` with those fingerprints
   (uppercase hex with colons, e.g. `AB:CD:...`).
3. Serve the file at `/.well-known/assetlinks.json` with
   `Content-Type: application/json`, reachable over HTTPS with no redirect.
4. Verify: https://developers.google.com/digital-asset-links/tools/generator
