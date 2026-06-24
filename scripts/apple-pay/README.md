# Apple Pay Certificate Generation for BOG

End-to-end procedure to produce the four files BOG (Bank of Georgia) requires
to activate Apple Pay on merchant ID `merchant.com.mtislab.celvo`.

## What BOG asked for

BOG does **not** generate or send a CSR. We must generate everything ourselves
and hand them the final encrypted private keys + signed certificate chains:

| File                                    | What it is                                 |
|-----------------------------------------|--------------------------------------------|
| `merchant.identity.pem`                 | Merchant Identity cert chain (Leaf+WWDR+Root) |
| `merchant.identity.pk.ENCRYPTED.pem`    | Encrypted RSA-2048 private key (PKCS8)     |
| `payment.processing.pem`                | Payment Processing cert chain (Leaf+WWDR+Root) |
| `payment.processing.pk.ENCRYPTED.pem`   | Encrypted EC prime256v1 private key (PKCS8) |
| Passphrase                              | Same passphrase used for both keys         |
| Merchant Identifier (Apple)             | `merchant.com.mtislab.celvo`               |
| Merchant Domain (Apple)                 | The verified domain on Apple Developer Portal |
| Merchant Display Name (Apple)           | The name registered on Apple Developer Portal |

## Procedure

### 1. Generate keys + CSRs

```bash
chmod +x 1-generate-keys-and-csrs.sh 2-build-chains-and-package.sh
./1-generate-keys-and-csrs.sh
```

You will be asked twice for a passphrase (once per key). **Use the same strong
passphrase for both** and store it in a password manager.

### 2. Upload CSRs to Apple Developer Portal

1. Open <https://developer.apple.com/account/resources/identifiers/list/merchant>
2. Select `merchant.com.mtislab.celvo`
3. **Apple Pay Payment Processing Certificate** → *Create Certificate* → upload
   `output/payment-processing.merchant.com.mtislab.celvo.csr` → *Continue* →
   *Download*. Save the file as `output/payment-processing.cer`.
4. **Apple Pay Merchant Identity Certificate** → *Create Certificate* → upload
   `output/merchant-identity.merchant.com.mtislab.celvo.csr` → *Continue* →
   *Download*. Save the file as `output/merchant-identity.cer`.

Apple may ask "Will payments associated with this Merchant ID be processed
exclusively in China mainland?" → answer **No** for Georgia.

### 3. Build chains and package

```bash
./2-build-chains-and-package.sh
```

This downloads Apple's intermediate (WWDR G4) and root (Apple Root CA G3)
certificates, converts everything to PEM, builds Leaf→Intermediate→Root chains
and writes the four BOG-named files into `output/bundle-for-bog/`.

### 4. Send to BOG

Reply to the BOG ticket with the four files attached **and** the merchant
metadata listed in the table above. Send the passphrase via a separate secure
channel — not in the same email.

## Notes / gotchas

- **Algorithms are mandated by Apple**, not BOG: Merchant Identity = RSA 2048,
  Payment Processing = EC prime256v1. The script enforces both.
- **Chain order matters**: Leaf first, then Intermediate (WWDR G4), then Root
  (Apple Root CA G3). The BOG email is explicit about "Leaf first".
- **Apple Pay leaf certs chain to WWDR G4** (Worldwide Developer Relations
  Certification Authority G4), not the older WWDR/G2/G3. The script downloads
  the right one.
- **Do NOT commit anything from `output/`** — see `.gitignore` in this
  directory. Private keys must never enter git history.
- The Merchant Identity certificate Apple issues is valid **25 months**; the
  Payment Processing certificate is valid **25 months**. Set a calendar
  reminder to rotate before expiry.
- If you lose the passphrase you cannot recover it — re-run step 1, get new
  CSRs signed by Apple, and resend everything to BOG.
