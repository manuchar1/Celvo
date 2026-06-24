package com.mtislab.core.designsystem.components.payment

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CelvoWalletTokenJsonTest {

    private val samplePaymentData: JsonObject = Json.parseToJsonElement(
        """
        {
          "data": "encrypted-base64-blob",
          "signature": "signature-base64",
          "header": {
            "publicKeyHash": "pkh-base64",
            "ephemeralPublicKey": "epk-base64",
            "transactionId": "abc123"
          },
          "version": "EC_v1"
        }
        """.trimIndent()
    ).jsonObject

    @Test
    fun `wallet token has exactly the three top-level keys the backend expects`() {
        val token = buildCelvoWalletTokenJson(
            paymentData = samplePaymentData,
            network = "MasterCard",
            displayName = "MasterCard 1234",
            type = 2,
            transactionIdentifier = "Simulated Identifier"
        )

        assertEquals(
            setOf("paymentData", "paymentMethod", "transactionIdentifier"),
            token.keys,
            "Backend / Georgian Card reject any extra or missing top-level keys."
        )
    }

    @Test
    fun `paymentMethod type is a JSON number and not a string label`() {
        val token = buildCelvoWalletTokenJson(
            paymentData = samplePaymentData,
            network = "Visa",
            displayName = "Visa 4242",
            type = 2,
            transactionIdentifier = "tx-1"
        )

        val typeElement = token["paymentMethod"]!!.jsonObject["type"]!!.jsonPrimitive
        // JsonPrimitive.isString is false for numeric values — the gateway
        // validates strictly and would reject "PKPaymentMethodTypeCredit".
        assertTrue(!typeElement.isString, "paymentMethod.type must be a JSON number")
        assertEquals(2, typeElement.content.toInt())
    }

    @Test
    fun `paymentData is embedded as a nested object and not a base64 string`() {
        val token = buildCelvoWalletTokenJson(
            paymentData = samplePaymentData,
            network = "Visa",
            displayName = "Visa 4242",
            type = 1,
            transactionIdentifier = "tx-2"
        )

        val embedded = token["paymentData"]!!.jsonObject
        assertEquals("EC_v1", embedded["version"]!!.jsonPrimitive.content)
        assertEquals(
            "abc123",
            embedded["header"]!!.jsonObject["transactionId"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun `nil-equivalent network and displayName become empty strings`() {
        val token = buildCelvoWalletTokenJson(
            paymentData = samplePaymentData,
            network = "",
            displayName = "",
            type = 0,
            transactionIdentifier = "tx-3"
        )

        val method = token["paymentMethod"]!!.jsonObject
        assertEquals(JsonPrimitive(""), method["network"])
        assertEquals(JsonPrimitive(""), method["displayName"])
    }

    @Test
    fun `serialized form round-trips and stays a flat 3-key envelope`() {
        val token = buildCelvoWalletTokenJson(
            paymentData = samplePaymentData,
            network = "MasterCard",
            displayName = "MC 9999",
            type = 2,
            transactionIdentifier = "tx-4"
        )

        val serialized = token.toString()
        val reparsed = Json.parseToJsonElement(serialized).jsonObject

        assertEquals(
            setOf("paymentData", "paymentMethod", "transactionIdentifier"),
            reparsed.keys
        )
        // Guard against a regression to the old { "token": {...} } envelope.
        assertTrue(
            "token" !in reparsed.keys,
            "wallet token must not be wrapped in an outer `token` envelope"
        )
    }
}
