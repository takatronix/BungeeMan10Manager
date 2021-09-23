package red.man10.bungee.manager.db

import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ConnectionDatabase {

    private val connectedTime = ConcurrentHashMap<UUID,Date>()

    fun connectServer(p:ProxiedPlayer,server:String){

        val sql = MySQLManager(plugin,"ConnectionLog")

        sql.execute("INSERT INTO connection_log (mcid, uuid, server, connected_time, disconnected_time, connection_seconds, ip) " +
                "VALUES ('${p.name}', '${p.uniqueId}', '${server}', now(), null, null, '${p.socketAddress}')")
        connectedTime[p.uniqueId] = Date()
    }

    fun disconnectServer(p:ProxiedPlayer,server: String){

        val connected = connectedTime[p.uniqueId]

        if (connected == null){
            plugin.discord.log("接続した時間の取得に失敗[${p.name}]")
            return
        }

        val sql = MySQLManager(plugin,"ConnectionLog")

        val seconds = (Date().time-connected.time) / 1000

        sql.execute("update connection_log set disconnected_time=now(),connection_seconds=$seconds " +
                "where mcid='${p.name}' order by id desc limit 1")

        connectedTime.remove(p.uniqueId)

    }

}