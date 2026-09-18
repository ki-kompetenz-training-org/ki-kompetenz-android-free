/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL

/**
 * Update-Check fuer Direkt-APK-Nutzer (FREE): Nutzer aus dem /app-Funnel
 * haben keinen Store — sie sehen nie, dass es eine neue Version gibt.
 * Der F-Droid-Repo-Index ist ohnehin oeffentlich; ein GET beim Oeffnen
 * des Profils reicht. Best-effort: null bei jedem Fehler (still).
 */
object UpdateCheck {

    const val REPO_BASE = "https://fdroid.contextual-intelligence.org/fdroid/repo"
    const val PACKAGE = "ai.ki_kompetenz_training_org.free"

    /** Hoechster versionCode + dessen versionName aus index-v1.json; null falls nicht auffindbar. */
    fun parseLatestVersion(json: String, pkg: String): Pair<Int, String>? = runCatching {
        val entries = Json.parseToJsonElement(json).jsonObject["packages"]
            ?.jsonObject?.get(pkg)?.jsonArray ?: return null
        entries.mapNotNull { e ->
            val m = e.jsonObject
            m["versionCode"]?.jsonPrimitive?.intOrNull?.let { code ->
                code to (m["versionName"]?.jsonPrimitive?.contentOrNull ?: code.toString())
            }
        }.maxByOrNull { it.first }
    }.getOrNull()

    /** Laedt den Index und parst die neueste Version; null offline/defekt. */
    suspend fun fetchLatestVersion(): Pair<Int, String>? = withContext(Dispatchers.IO) {
        runCatching {
            parseLatestVersion(URL("$REPO_BASE/index-v1.json").readText(), PACKAGE)
        }.getOrNull()
    }

    fun apkUrl(versionCode: Int) = "$REPO_BASE/${PACKAGE}_$versionCode.apk"
}
