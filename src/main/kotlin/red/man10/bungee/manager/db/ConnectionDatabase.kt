package red.man10.bungee.manager.db

import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ConnectionDatabase {

    private val connectedTime = ConcurrentHashMap<Pair<UUID,String>,Date>()

    fun connectServer(p:ProxiedPlayer,server:String){

        val uuid = p.uniqueId

        if (connectedTime[Pair(p.uniqueId,server)] != null) {
            disconnectServer(p,server)
        }

        val sql = MySQLManager(plugin,"ConnectionLog")

        val address = p.socketAddress.toString().replace("/","").split(":")

        sql.execute("INSERT INTO connection_log (mcid, uuid, server, connected_time, disconnected_time, connection_seconds, ip, port) " +
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

        val sql = MySQLManager(plugin,"ConnectionLog")

        val seconds = (Date().time-connected.time) / 1000

        sql.execute("update connection_log set disconnected_time=now(),connection_seconds=$seconds " +
                "where mcid='${p.name}' and server='$server' order by id desc limit 1")

        connectedTime.remove(key)

    }

}