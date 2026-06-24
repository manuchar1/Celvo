#!/usr/bin/env bash
#
# Step 2: After downloading both .cer files from Apple, this script:
#   - Converts them from DER to PEM
#   - Downloads the correct Apple intermediate + root CAs for each chain
#     (Payment Processing and Merchant Identity are issued by DIFFERENT CAs!)
#   - Builds the certificate chains (Leaf -> Intermediate -> Root)
#   - Produces the exact files BOG requested:
#         merchant.identity.pem
#         merchant.identity.pk.ENCRYPTED.pem
#         payment.processing.pem
#         payment.processing.pk.ENCRYPTED.pem
#
# Chain overview:
#   Payment Processing leaf -> WWDR CA G2 -> Apple Root CA G3
#   Merchant Identity  leaf -> WWDR CA G3 -> Apple Root CA (original)
#
# Expected inputs in ./output/:
#   merchant-identity.cer        (downloaded from Apple)
#   payment-processing.cer       (downloaded from Apple)
#   merchant-identity.merchant.com.mtislab.celvo.key   (from step 1)
#   payment-processing.merchant.com.mtislab.celvo.key  (from step 1)
#

set -euo pipefail

MERCHANT_ID="merchant.com.mtislab.celvo"
OUT_DIR="$(cd "$(dirname "$0")" && pwd)/output"
BUNDLE_DIR="$OUT_DIR/bundle-for-bog"
mkdir -p "$BUNDLE_DIR"
cd "$OUT_DIR"

MI_LEAF_DER="merchant-identity.cer"
PP_LEAF_DER="payment-processing.cer"
MI_KEY="merchant-identity.${MERCHANT_ID}.key"
PP_KEY="payment-processing.${MERCHANT_ID}.key"

for f in "$MI_LEAF_DER" "$PP_LEAF_DER" "$MI_KEY" "$PP_KEY"; do
  if [[ ! -f "$f" ]]; then
    echo "ERROR: missing required file: $OUT_DIR/$f"
    exit 1
  fi
done

############################################
# 1. Download Apple's intermediate + root CAs (both chains)
############################################
# Source: https://www.apple.com/certificateauthority/
#
# Payment Processing leaf is issued by WWDR G2,   which is signed by Apple Root CA - G3
# Merchant Identity  leaf is issued by WWDR G3,   which is signed by Apple Root CA (original)
echo "==> Downloading Apple intermediates and roots"
curl -fsSL -o AppleWWDRCAG2.cer  https://www.apple.com/certificateauthority/AppleWWDRCAG2.cer
curl -fsSL -o AppleWWDRCAG3.cer  https://www.apple.com/certificateauthority/AppleWWDRCAG3.cer
curl -fsSL -o AppleRootCA-G3.cer https://www.apple.com/certificateauthority/AppleRootCA-G3.cer
curl -fsSL -o AppleRootCA.cer    https://www.apple.com/appleca/AppleIncRootCertificate.cer

############################################
# 2. Convert all certs from DER to PEM
############################################
echo "==> Converting certificates from DER to PEM"
openssl x509 -inform DER -in "$MI_LEAF_DER"     -out merchant-identity.leaf.pem
openssl x509 -inform DER -in "$PP_LEAF_DER"     -out payment-processing.leaf.pem
openssl x509 -inform DER -in AppleWWDRCAG2.cer  -out AppleWWDRCAG2.pem
openssl x509 -inform DER -in AppleWWDRCAG3.cer  -out AppleWWDRCAG3.pem
openssl x509 -inform DER -in AppleRootCA-G3.cer -out AppleRootCA-G3.pem
openssl x509 -inform DER -in AppleRootCA.cer    -out AppleRootCA.pem

############################################
# 3. Build chains (Leaf first => Intermediate => Root)
############################################
echo "==> Building Merchant Identity chain (Leaf -> WWDR G3 -> Apple Root CA)"
cat merchant-identity.leaf.pem AppleWWDRCAG3.pem AppleRootCA.pem \
    > "$BUNDLE_DIR/merchant.identity.pem"

echo "==> Building Payment Processing chain (Leaf -> WWDR G2 -> Apple Root CA G3)"
cat payment-processing.leaf.pem AppleWWDRCAG2.pem AppleRootCA-G3.pem \
    > "$BUNDLE_DIR/payment.processing.pem"

############################################
# 4. Convert encrypted private keys to PKCS8 (PEM, encrypted)
#    BOG asked specifically for *.pk.ENCRYPTED.pem
############################################
echo
echo "==> Converting Merchant Identity key to encrypted PKCS8 PEM"
echo "    You will be asked for the ORIGINAL key passphrase, then a NEW encryption password."
echo "    Use the SAME passphrase for both (BOG expects one password per cert)."
openssl pkcs8 -topk8 -v1 PBE-SHA1-3DES \
  -in "$MI_KEY" \
  -out "$BUNDLE_DIR/merchant.identity.pk.ENCRYPTED.pem"

echo
echo "==> Converting Payment Processing key to encrypted PKCS8 PEM"
openssl pkcs8 -topk8 -v1 PBE-SHA1-3DES \
  -in "$PP_KEY" \
  -out "$BUNDLE_DIR/payment.processing.pk.ENCRYPTED.pem"

############################################
# 5. Verification
############################################
echo
echo "==> Verifying Merchant Identity chain"
openssl verify -CAfile AppleRootCA.pem    -untrusted AppleWWDRCAG3.pem "$BUNDLE_DIR/merchant.identity.pem"  || true
echo "==> Verifying Payment Processing chain"
openssl verify -CAfile AppleRootCA-G3.pem -untrusted AppleWWDRCAG2.pem "$BUNDLE_DIR/payment.processing.pem" || true

echo
echo "============================================================"
echo "DONE. Files for BOG are in: $BUNDLE_DIR"
echo
ls -la "$BUNDLE_DIR"
echo
echo "Send to BOG:"
echo "  - merchant.identity.pem"
echo "  - merchant.identity.pk.ENCRYPTED.pem"
echo "  - payment.processing.pem"
echo "  - payment.processing.pk.ENCRYPTED.pem"
echo "  - Passphrase for both encrypted keys (send through a SECURE channel,"
echo "    NOT the same email — e.g. password manager link, signed encrypted message)"
echo "  - Apple Merchant Identifier:  ${MERCHANT_ID}"
echo "  - Apple Merchant Domain:      api.celvoapp.com"
echo "  - Apple Merchant Display Name: <the display name registered on Apple Developer Portal>"
echo "============================================================"
