/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.api

import kotlinx.serialization.json.Json

/**
 * Kanonische Json-Konfiguration der Content-API.
 *
 * Als eigenständiges Objekt extrahiert, damit Parsing-Verhalten unit-testbar
 * ist (ApiJsonParsingTest), ohne Retrofit/OkHttp zu benötigen.
 *
 * isLenient = true (FIX BUG 2026-09-01): Die API liefert `lesson` als
 * STRING ("0"), das DTO erwartet Int?. Ohne isLenient wirft kotlinx-
 * serialization eine SerializationException ("Unexpected JSON token"),
 * runCatching im Repository fängt sie → "Lessons could not be loaded" —
 * AUCH bei erreichbarem Server. isLenient koertiert "0" → 0.
 */
val ApiJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}
