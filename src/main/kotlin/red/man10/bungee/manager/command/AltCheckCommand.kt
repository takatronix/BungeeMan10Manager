package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import red.man10.bungee.manager.db.MySQLManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object AltCheckCommand : Command("malt","bungeemanager.alt") {

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (args == null)return
        if (sender == null)return


        when (args[0]){

            "user" ->{
                val p = plugin.proxy.getPlayer(args[1])
               val ip = getAddress(p)

                Thread{

                    val db = MySQLManager(plugin,"AltCheck")
                    val rs = db.query("select * from connection_log where ip in (select ip from connection_log" +
                            " where mcid in (select mcid from connection_log where ip='${ip}'))")?:return@Thread

                    val playerList = mutableSetOf<String>()

                    while (rs.next()){ playerList.add(rs.getString("mcid")) }

                    rs.close()
                    db.close()

                    sendMessage(sender,"§c§l検索ユーザー:${p.name} 検索IP:${ip}")
                    sendMessage(sender,"§c§lサブ垢の可能性があるプレイヤー")

                    for (player in playerList){ sendMessage(sender,"§c§l${player}") }

                }.start()
            }

            "ip" ->{
                val ip = args[1]

                Thread{

                    val db = MySQLManager(plugin,"AltCheck")
                    val rs = db.query("select mcid from connection_log where ip='${ip}'")?:return@Thread

                    val playerList = mutableSetOf<String>()

                    while (rs.next()){ playerList.add(rs.getString("mcid")) }

                    rs.close()
                    db.close()

                    sendMessage(sender,"§c§l検索IP:${ip}")
                    sendMessage(sender,"§c§l同じIPのプレイヤー")

                    for (player in playerList){ sendMessage(sender,"§c§l${player}") }


                }.start()

            }

        }

    }

    private fun getAddress(p:ProxiedPlayer):String{
        val address = p.socketAddress.toString().replace("/","")
        return address.split(":")[0]
    }
}