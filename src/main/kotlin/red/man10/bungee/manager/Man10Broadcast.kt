package red.man10.bungee.manager

import net.md_5.bungee.api.ProxyServer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendGlobalMessage
import kotlin.random.Random

object Man10Broadcast {
    val prefix = "§b[§a§l●§f§l●§d§l●§r饅頭放送§a§lM§f§lH§d§lK§r§b]"

    var broadcastDelay = 5
    var broadcastList = mutableListOf<String>()

    fun runMHK() {
        ProxyServer.getInstance().scheduler.runAsync(plugin) {
            while (true) {
                Thread.sleep((1000 * 60 * broadcastDelay).toLong())

                val i = Random.nextInt(broadcastList.size)

                sendGlobalMessage("${prefix}§7${broadcastList[i]}")
            }
        }
    }
}