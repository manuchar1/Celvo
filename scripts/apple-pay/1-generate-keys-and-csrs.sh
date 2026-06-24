#!/usr/bin/env bash
#
# Step 1: Generate private keys and CSRs for both Apple Pay certificates.
#
# After running this script you will have:
#   merchant-identity.merchant.com.mtislab.celvo.key      (RSA 2048, encrypted with passphrase)
#   merchant-identity.merchant.com.mtislab.celvo.csr      (upload to Apple)
#   payment-processing.merchant.com.mtislab.celvo.key     (EC prime256v1, encrypted)
#   payment-processing.merchant.com.mtislab.celvo.csr     (upload to Apple)
#
# IMPORTANT: write down the passphrases. You will need them later and you must
# share them with BOG together with the encrypted .pem keys.
#
# Apple's requirements (per BOG email):
#   - Merchant Identity certificate => RSA, minimum 2048-bit
#   - Payment Processing certificate => Elliptic Curve, prime256v1
#

set -euo pipefail

MERCHANT_ID="merchant.com.mtislab.celvo"
OUT_DIR="$(cd "$(dirname "$0")" && pwd)/output"
mkdir -p "$OUT_DIR"
cd "$OUT_DIR"

echo "==> Output directory: $OUT_DIR"
echo

############################################
# 1. MERCHANT IDENTITY CERTIFICATE (RSA 2048)
############################################
MI_KEY="merchant-identity.${MERCHANT_ID}.key"
MI_CSR="merchant-identity.${MERCHANT_ID}.csr"

echo "==> [1/2] Generating Merchant Identity RSA 2048 key (you will set a passphrase)"
openssl genrsa -aes256 -out "$MI_KEY" 2048

echo
echo "==> Generating Merchant Identity CSR"
echo "    Suggested values when prompted:"
echo "      Country Name:          GE"
echo "      State or Province:     Tbilisi"
echo "      Locality:              Tbilisi"
echo "      Organization Name:     MTIS Lab (or your legal entity name)"
echo "      Organizational Unit:   ."
echo "      Common Name:           ${MERCHANT_ID}"
echo "      Email Address:         zakariadzemanuchar@gmail.com"
echo "      A challenge password:  . (leave blank)"
echo "      An optional company:   . (leave blank)"
echo
openssl req -new -key "$MI_KEY" -out "$MI_CSR"

echo
############################################
# 2. PAYMENT PROCESSING CERTIFICATE (EC prime256v1)
############################################
PP_KEY="payment-processing.${MERCHANT_ID}.key"
PP_CSR="payment-processing.${MERCHANT_ID}.csr"

echo "==> [2/2] Generating Payment Processing EC prime256v1 key (unencrypted first)"
openssl ecparam -name prime256v1 -genkey -noout -out "$PP_KEY"

echo "==> Encrypting Payment Processing key with AES-256 (set a passphrase)"
openssl ec -aes256 -in "$PP_KEY" -out "$PP_KEY"

echo
echo "==> Generating Payment Processing CSR"
echo "    Use the SAME values as before."
echo
openssl req -new -key "$PP_KEY" -out "$PP_CSR"

echo
echo "============================================================"
echo "DONE. Generated files in: $OUT_DIR"
echo
echo "Next step:"
echo "  1. Open https://developer.apple.com/account/resources/identifiers/list/merchant"
echo "  2. Open the merchant ID: ${MERCHANT_ID}"
echo "  3. 'Apple Pay Payment Processing Certificate' => Create Certificate"
echo "       => upload: ${PP_CSR}"
echo "       => Download => save as: payment-processing.cer"
echo "  4. 'Apple Pay Merchant Identity Certificate' => Create Certificate"
echo "       => upload: ${MI_CSR}"
echo "       => Download => save as: merchant-identity.cer"
echo
echo "  5. Place both .cer files into: $OUT_DIR"
echo "  6. Run: ./2-build-chains-and-package.sh"
echo "============================================================"
