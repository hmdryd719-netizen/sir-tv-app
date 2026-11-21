package com.example.sirtv

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

object AdBlocker {
    private val adHosts = HashSet<String>()
    private const val PRIMARY_DOMAIN = "sir-tv.vip"

    private val builtInAdHosts = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "juicyads.com",
        "exoclick.com",
        "trafficjunky.com",
        "hilltopads.com",
        "adsterra.com",
        "clickadu.com",
        "monetag.com",
        "revcontent.com",
        "mgid.com",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
        "histats.com",
        "yandexmetrica.com",
        "googletagmanager.com",
        "google-analytics.com",
        "adnxs.com",
        "criteo.com",
        "pubmatic.com",
        "rubiconproject.com",
        "openx.net",
        "serving-sys.com",
        "bidswitch.net",
        "smaato.net",
        "inmobi.com",
        "vungle.com",
        "unityads.unity3d.com"
    )

    fun init(context: Context) {
        adHosts.addAll(builtInAdHosts)

        Thread {
            try {
                context.assets.open("adblock.txt").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            line?.trim()?.let { raw ->
                                if (raw.isNotEmpty() && !raw.startsWith("#") && !raw.startsWith("!")) {
                                    val domain = raw.split("\\s+".toRegex()).last().lowercase()
                                    if (domain.isNotEmpty() && domain != "localhost" && domain != "0.0.0.0") {
                                        adHosts.add(domain)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }.start()
    }

    fun isAd(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase() ?: return false
            if (scheme != "http" && scheme != "https") return false

            val host = uri.host?.lowercase() ?: return false
            if (host == PRIMARY_DOMAIN || host.endsWith(".$PRIMARY_DOMAIN")) {
                return false
            }

            var current = host
            while (current.contains(".")) {
                if (adHosts.contains(current)) {
                    return true
                }
                current = current.substringAfter(".")
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    fun createEmptyResponse(mimeType: String = "text/plain"): WebResourceResponse {
        return WebResourceResponse(mimeType, "utf-8", ByteArrayInputStream("".toByteArray()))
    }
}
