package red.man10.bungee.manager

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import kotlin.random.Random

class Man10Broadcast {
    val prefix = "§b[§a§l●§f§l●§d§l●§r饅頭放送§a§lM§f§lH§d§lK§r§b]"

    var broadcastList = mutableListOf<String>()

    fun runMHK() {
        ProxyServer.getInstance().scheduler.runAsync(plugin) {
            while (true) {
                Thread.sleep(1000 * 60 * 5)

                val i = Random.nextInt(broadcastList.size + 1)

                sendBroadcast(broadcastList[i])
            }
        }
    }

    fun sendBroadcast(text: String) {
        ProxyServer.getInstance().broadcast(*ComponentBuilder("${prefix}§7${text}").create())
    }
}