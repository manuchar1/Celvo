#!/usr/bin/env bash
#
# Step 3 (optional but recommended): Verify that each encrypted private key
# in bundle-for-bog/ actually matches its corresponding certificate.
# You will be prompted twice for the passphrase (same one you set earlier).
#

set -euo pipefail

OUT_DIR="$(cd "$(dirname "$0")" && pwd)/output"
BUNDLE_DIR="$OUT_DIR/bundle-for-bog"
cd "$OUT_DIR"

check_pair() {
  local label="$1"
  local key_file="$2"
  local cert_file="$3"

  echo "--- $label ---"
  echo "Enter passphrase when prompted:"
  local key_hash
  key_hash=$(openssl pkey -in "$key_file" -pubout | openssl sha256 | awk '{print $2}')
  local cert_hash
  cert_hash=$(openssl x509 -in "$cert_file" -pubkey -noout | openssl sha256 | awk '{print $2}')
  echo "Key pubkey hash:  $key_hash"
  echo "Cert pubkey hash: $cert_hash"
  if [[ "$key_hash" == "$cert_hash" ]]; then
    echo "MATCH ✓"
  else
    echo "MISMATCH ✗ — key does NOT match certificate"
    return 1
  fi
  echo
}

echo "==== CHAIN VERIFICATION ===="
openssl verify -CAfile AppleRootCA.pem    -untrusted AppleWWDRCAG3.pem "$BUNDLE_DIR/merchant.identity.pem"
openssl verify -CAfile AppleRootCA-G3.pem -untrusted AppleWWDRCAG2.pem "$BUNDLE_DIR/payment.processing.pem"
echo

echo "==== KEY / CERT PAIR CHECK ===="
check_pair "Merchant Identity" \
  "$BUNDLE_DIR/merchant.identity.pk.ENCRYPTED.pem" \
  "$BUNDLE_DIR/merchant.identity.pem"

check_pair "Payment Processing" \
  "$BUNDLE_DIR/payment.processing.pk.ENCRYPTED.pem" \
  "$BUNDLE_DIR/payment.processing.pem"

echo "All checks passed ✓"
