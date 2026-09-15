package com.example.xuimanager.data.model

import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.util.Base64

data class RealityTarget(
    val id: String,
    val name: String,
    val target: String,
    val serverNames: List<String>
)

object RealityTargets {

    val targets = listOf(
        RealityTarget(
            id = "samsung",
            name = "Samsung (www.samsung.com:443)",
            target = "www.samsung.com:443",
            serverNames = listOf(
                "www.samsung.com",
                "adn-stg.yourservice.samsung.com",
                "am-images.shop.samsung.com",
                "ap-author.led.samsung.com",
                "ap-author.semiconductor.samsung.com",
                "api-stg.semiconductor.samsung.cn",
                "api.led.samsung.com",
                "api.semiconductor.samsung.cn",
                "api.semiconductor.samsung.com",
                "au-images.shop.samsung.com",
                "au2-images.shop.samsung.com",
                "b2bshop.samsung.com",
                "cdn.samsung.com",
                "cstudio.semiconductor.samsung.com",
                "download.led.samsung.com",
                "download.semiconductor.samsung.com",
                "eu-images.shop.samsung.com",
                "eventadm.semiconductor.samsung.com",
                "eventapi.semiconductor.samsung.com",
                "image.led.samsung.com",
                "image.samsung.com",
                "image.semiconductor.samsung.com",
                "images.samsung.com",
                "led.samsung.com",
                "legal.samsungdm.com",
                "mena-images.shop.samsung.com",
                "org.semiconductor.samsung.com",
                "perf-prod.samsung.com",
                "pre-prod.samsung.com",
                "qa.semiconductor.samsung.com",
                "qapartners.sec.samsung.com",
                "ru-images.shop.samsung.com",
                "samsung.com",
                "search.led.samsung.com",
                "search.semiconductor.samsung.com",
                "semiconductor.samsung.com",
                "sribsrch.ecom-qa.samsung.com",
                "sribsrch.ecom.samsung.com",
                "stg-am-images.shop.samsung.com",
                "stg-au-images.shop.samsung.com",
                "stg-au2-images.shop.samsung.com",
                "stg-eu-images.shop.samsung.com",
                "stg-mena-images.shop.samsung.com",
                "stg-ru-images.shop.samsung.com",
                "streaming.samsung.com",
                "ue-author.semiconductor.samsung.com",
                "vdapi.samsung.com",
                "www-ams.samsung.com",
                "www.samsungebiz.com",
                "www.semiconductor.samsung.com"
            )
        ),
        RealityTarget(
            id = "sony",
            name = "Sony (www.sony.com:443)",
            target = "www.sony.com:443",
            serverNames = listOf(
                "www.sony.co.uk",
                "campaign.odw.sony-europe.com",
                "compliance.sony.de",
                "compliance.sony.eu",
                "global.sony.eu",
                "m.store.sony.com",
                "smb.store.sony.com",
                "sp.sony-europe.com",
                "store.sony.ca",
                "store.sony.com",
                "tw.sony-asia.com",
                "www.compliance.sony.de",
                "www.compliance.sony.eu",
                "www.sony-africa.com",
                "www.sony-asia.com",
                "www.sony-europe.com",
                "www.sony-latin.com",
                "www.sony-mea.com",
                "www.sony.ba",
                "www.sony.be",
                "www.sony.bg",
                "www.sony.ca",
                "www.sony.ch",
                "www.sony.cl",
                "www.sony.co.cr",
                "www.sony.co.id",
                "www.sony.co.il",
                "www.sony.co.in",
                "www.sony.co.kr",
                "www.sony.co.nz",
                "www.sony.co.th",
                "www.sony.com",
                "www.sony.com.ar",
                "www.sony.com.au",
                "www.sony.com.bo",
                "www.sony.com.br",
                "www.sony.com.co",
                "www.sony.com.do",
                "www.sony.com.ec",
                "www.sony.com.gt",
                "www.sony.com.hk",
                "www.sony.com.hn",
                "www.sony.com.mk",
                "www.sony.com.mx",
                "www.sony.com.my",
                "www.sony.com.ni",
                "www.sony.com.pa",
                "www.sony.com.pe",
                "www.sony.com.ph",
                "www.sony.com.sg",
                "www.sony.com.sv",
                "www.sony.com.tr",
                "www.sony.com.tw",
                "www.sony.com.vn",
                "www.sony.cz",
                "www.sony.de",
                "www.sony.dk",
                "www.sony.ee",
                "www.sony.es",
                "www.sony.fi",
                "www.sony.fr",
                "www.sony.hr",
                "www.sony.hu",
                "www.sony.ie",
                "www.sony.it",
                "www.sony.kz",
                "www.sony.lt",
                "www.sony.lu",
                "www.sony.lv",
                "www.sony.nl",
                "www.sony.no",
                "www.sony.pt",
                "www.sony.ro",
                "www.sony.rs",
                "www.sony.se",
                "www.sony.si",
                "www.sony.sk",
                "www.sony.ua",
                "www.sonylatvija.com"
            )
        ),
        RealityTarget(
            id = "nvidia",
            name = "NVIDIA (www.nvidia.com:443)",
            target = "www.nvidia.com:443",
            serverNames = listOf(
                "it.nvidia.com",
                "api-dev.nova.nvidia.com",
                "api-prod.nova.nvidia.com",
                "api-stage.nova.nvidia.com",
                "api.docs.nvidia.com",
                "archive.docs.nvidia.com",
                "biosmod.partners.nvidia.com",
                "biosmod.vendors.nvidia.com",
                "blog.nvidia.com.br",
                "blogs.nvidia.co.jp",
                "blogs.nvidia.co.kr",
                "blogs.nvidia.com.br",
                "blogs.nvidia.com.tw",
                "blogs.nvidia.fr",
                "developer.qa.nvidia.cn",
                "docs.api.nvidia.com",
                "firmwaresign.partners.nvidia.com",
                "fulcrum.partners.nvidia.com",
                "gfwsl.stagegeforce.geforce.com",
                "gx-target-rconfig-frontend-api-cdn.gx-stg.nvidia.com",
                "gx-target-rconfig-frontend-api.gx-stg.nvidia.com",
                "hdprogressive.3dvisionlive.com",
                "hub.partners.nvidia.com",
                "la.blogs.nvidia.com",
                "nvplm.stg.nvidia.com",
                "ops.gx.nvidia.com",
                "partners.legacy.nvidia.com",
                "partners.nvbugs.nvidia.com",
                "services-cdn.gfestage.nvidia.com",
                "shield.nvidia.asia",
                "shield.nvidia.co.jp",
                "shield.nvidia.co.kr",
                "shield.nvidia.hk",
                "shield.nvidia.in",
                "shield.nvidia.jp",
                "stage.partners.nvbugs.nvidia.com",
                "stagepartners.legacy.nvidia.com",
                "stagepartners.nvbugs.nvidia.com",
                "stg.nvbugspro.nvidia.com",
                "support-shield.nvidia.be",
                "support-shield.nvidia.co.kr",
                "support-shield.nvidia.co.uk",
                "support-shield.nvidia.cz",
                "support-shield.nvidia.de",
                "support-shield.nvidia.dk",
                "support-shield.nvidia.es",
                "support-shield.nvidia.fi",
                "support-shield.nvidia.fr",
                "support-shield.nvidia.it",
                "support-shield.nvidia.nl",
                "support-shield.nvidia.no",
                "support-shield.nvidia.pl",
                "support-shield.nvidia.se",
                "us.vrzn.download.nvidia.com",
                "www.developer.nvidia.com",
                "www.frameswingames.com",
                "www.nvidia.com.ua",
                "www.openacc.org",
                "www.pgicompilers.com",
                "www.pgroup.com"
            )
        )
    )
}

object RealityKeyGenerator {

    private val random = SecureRandom()

    fun generateRandomPort(): Int {
        return random.nextInt(45000) + 15000 // 15000..60000
    }

    fun generateShortIds(): List<String> {
        val count = random.nextInt(3) + 2
        val hexChars = "0123456789abcdef"
        val lengths = listOf(4, 6, 8, 12, 16)

        return List(count) {
            val len = lengths[random.nextInt(lengths.size)]
            (1..len).map { hexChars[random.nextInt(hexChars.length)] }.joinToString("")
        }
    }

    fun generateSpiderX(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val length = random.nextInt(8) + 10
        val randomPath = (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "/$randomPath"
    }

    fun generateX25519KeyPair(): Pair<String, String> {
        return try {
            val kpg = KeyPairGenerator.getInstance("X25519")
            val keyPair = kpg.generateKeyPair()

            val privEncoded = keyPair.private.encoded
            val pubEncoded = keyPair.public.encoded

            val rawPriv =
                if (privEncoded.size >= 32) privEncoded.takeLast(32).toByteArray() else privEncoded
            val rawPub =
                if (pubEncoded.size >= 32) pubEncoded.takeLast(32).toByteArray() else pubEncoded

            val privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(rawPriv)
            val publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(rawPub)

            Pair(privateKey, publicKey)
        } catch (e: Exception) {
            val rawPriv = ByteArray(32).also { random.nextBytes(it) }
            val rawPub = ByteArray(32).also { random.nextBytes(it) }
            val privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(rawPriv)
            val publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(rawPub)
            Pair(privateKey, publicKey)
        }
    }
}