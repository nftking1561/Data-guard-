package com.example.data.model

enum class AppCategory(val displayName: String) {
    SOCIAL("Social"),
    VIDEO("Video & Streaming"),
    MESSAGING("Messaging"),
    CLOUD("Cloud & Backup"),
    BROWSER("Browser"),
    GAMES("Games"),
    PRODUCTIVITY("Productivity"),
    SHOPPING("Shopping"),
    FINANCE("Finance"),
    SYSTEM("System & OS"),
    OTHER("Other");

    companion object {
        fun fromPackage(packageName: String, appName: String): AppCategory {
            val lowerPkg = packageName.lowercase()
            val lowerName = appName.lowercase()
            return when {
                lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("tiktok") ||
                        lowerPkg.contains("video") || lowerPkg.contains("stream") || lowerPkg.contains("spotify") ||
                        lowerName.contains("youtube") || lowerName.contains("netflix") || lowerName.contains("tiktok") -> VIDEO

                lowerPkg.contains("instagram") || lowerPkg.contains("facebook") || lowerPkg.contains("twitter") ||
                        lowerPkg.contains("x.android") || lowerPkg.contains("snapchat") || lowerPkg.contains("reddit") ||
                        lowerPkg.contains("threads") || lowerName.contains("instagram") || lowerName.contains("facebook") -> SOCIAL

                lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("messenger") ||
                        lowerPkg.contains("signal") || lowerPkg.contains("viber") || lowerPkg.contains("discord") ||
                        lowerName.contains("whatsapp") || lowerName.contains("telegram") -> MESSAGING

                lowerPkg.contains("photos") || lowerPkg.contains("drive") || lowerPkg.contains("dropbox") ||
                        lowerPkg.contains("cloud") || lowerPkg.contains("backup") || lowerPkg.contains("onedrive") ||
                        lowerName.contains("photos") || lowerName.contains("drive") -> CLOUD

                lowerPkg.contains("chrome") || lowerPkg.contains("firefox") || lowerPkg.contains("browser") ||
                        lowerPkg.contains("opera") || lowerPkg.contains("edge") || lowerName.contains("browser") ||
                        lowerName.contains("chrome") -> BROWSER

                lowerPkg.contains("game") || lowerPkg.contains("pubg") || lowerPkg.contains("candy") ||
                        lowerPkg.contains("roblox") || lowerPkg.contains("clash") -> GAMES

                lowerPkg.contains("bank") || lowerPkg.contains("pay") || lowerPkg.contains("opay") ||
                        lowerPkg.contains("palmpay") || lowerPkg.contains("kuda") || lowerPkg.contains("wallet") -> FINANCE

                lowerPkg.contains("android") || lowerPkg.contains("google.android.gms") ||
                        lowerPkg.contains("system") || lowerPkg.contains("telephony") -> SYSTEM

                else -> OTHER
            }
        }
    }
}
