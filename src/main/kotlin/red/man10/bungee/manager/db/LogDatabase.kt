package red.man10.bungee.manager.db

import net.md_5.bungee.api.connection.ProxiedPlayer
import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap

object LogDatabase {

    val connectData = HashMap<ProxiedPlayer, ConnectionData>()


    fun messageLog(p:ProxiedPlayer, message:String){

        MySQLManager.executeQueue("INSERT INTO message_log (uuid, mcid, message, date) " +
                "VALUES ('${p.uniqueId}', '${p.name}', '$message', DEFAULT);")

    }

    fun commandLog(p:ProxiedPlayer, command: String){

        MySQLManager.executeQueue("INSERT INTO command_log (uuid,mcid,message,date) " +
                "VALUES ('${p.uniqueId}','${p.name}','$command',DEFAULT);")

    }

    fun connect(p:ProxiedPlayer){

        val data = ConnectionData()

        data.server = p.server.info.name
        data.connect = Timestamp(Date().time)

        connectData[p] = data
    }

    fun connectionLog(p:ProxiedPlayer){

        val data = connectData[p]?:return

        data.disconnect = Timestamp(Date().time)

        MySQLManager.executeQueue("INSERT INTO connection_log " +
                "(ip, uuid, server, connected_time, disconnected_time, connection_seconds, mcid) " +
                "VALUES (" +
                "'${p.socketAddress}', " +
                "'${data.server}', " +
                "'${data.connect}', " +
                "'${data.disconnect}', " +
                "'${data.getConnectionSeconds()}', " +
                "'${p.uniqueId}');")

    }


    class ConnectionData{

        var server = ""
        lateinit var connect : Timestamp
        lateinit var disconnect : Timestamp

        fun getConnectionSeconds():Int{
            return disconnect.compareTo(connect)
        }

    }

}