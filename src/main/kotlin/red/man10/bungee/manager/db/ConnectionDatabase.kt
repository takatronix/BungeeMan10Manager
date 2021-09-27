package red.man10.bungee.manager.db

import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.db.MySQLManager.Companion.executeQueue
import java.util.*

object ConnectionDatabase {

    private val connectedTime = HashMap<Pair<UUID,String>,Date>()

    fun connectServer(p:ProxiedPlayer,server:String){

        val uuid = p.uniqueId

        if (connectedTime[Pair(p.uniqueId,server)] != null) {
            disconnectServer(p,server)
        }

        val address = p.socketAddress.toString().replace("/","").split(":")

        executeQueue("INSERT INTO connection_log (mcid, uuid, server, connected_time, disconnected_time, connection_seconds, ip, port) " +
                "VALUES ('${p.name}', '${uuid}', '${server}', now(), null, null, '${address[0]}', ${address[1].toInt()})")

        connectedTime[Pair(uuid,server)] = Date()
    }

    fun disconnectServer(p:ProxiedPlayer,server: String){

        val key = Pair(p.uniqueId,server)

        val connected = connectedTime[key]

        if (connected == null){
            plugin.discord.log("接続した時間の取得に失敗[${p.name}]")
            return
        }

        val seconds = (Date().time-connected.time) / 1000

        executeQueue("update connection_log set disconnected_time=now(),connection_seconds=$seconds " +
                "where uuid='${p.uniqueId}' and server='$server' and disconnected_time is null")

        connectedTime.remove(key)

    }

}