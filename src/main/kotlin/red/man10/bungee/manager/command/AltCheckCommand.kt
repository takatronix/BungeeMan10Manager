package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import red.man10.bungee.manager.db.MySQLManager
import java.util.*

object AltCheckCommand : Command("malt","bungeemanager.alt") {

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (args == null)return
        if (sender == null)return

        if (args.isEmpty()){
            sendMessage(sender,"/malt sub <mcid> サブ垢の可能性があるアカウントを検索します")
            sendMessage(sender,"/malt user <mcid> 過去のIPアドレスなどを検索します")
            sendMessage(sender,"/malt ip <mcid/ipアドレス(XXX.XXX.XXX.XXX)> 指定IPと同じIPのアカウントを検索します")
            sendMessage(sender,"/malt ban <mcid/ipアドレス> <理由> 指定したIP/プレイヤーのIPをBanします")
            sendMessage(sender,"/malt unban <ipアドレス> 指定したIPのIPをUnBanします")

            return
        }

        when (args[0]){

            "sub" ->{
                if (args.size<2)return

                val searchPlayer = args[1]
                Thread{

                    val db = MySQLManager(plugin,"AltCheck")

                    val rs = db.query("select  mcid,ip,count(*) from connection_log " +
                            "where ip in (select ip from connection_log " +
                            "where mcid = '${searchPlayer}' group by mcid,ip order by ip) group by mcid,ip;")?:return@Thread

                    val playerSet = mutableSetOf<String>()

                    while (rs.next()){ playerSet.add(rs.getString("mcid")) }

                    rs.close()
                    db.close()

                    playerSet.remove(searchPlayer)

                    sendMessage(sender,"§d§l検索ユーザー:${searchPlayer}")
                    sendMessage(sender,"§d§lサブ垢の可能性があるプレイヤー")

                    for (p in playerSet){ sendMessage(sender,"§c§l${p}") }

                }.start()
            }

            "user" ->{
                if (args.size<2)return

                val p = args[1]
                Thread{

                    val db = MySQLManager(plugin,"AltCheck")

                    val rs = db.query("select mcid from connection_log where ip in " +
                            "(select ip from connection_log where mcid in " +
                            "(select mcid from connection_log where ip in " +
                            "(select ip from connection_log where mcid='${p}')));")?:return@Thread

                    val pairSet = mutableSetOf<Pair<String,String>>()

                    while (rs.next()){ pairSet.add(Pair(rs.getString("mcid"),rs.getString("ip"))) }

                    rs.close()
                    db.close()

                    sendMessage(sender,"§d§l検索ユーザー:${p}")
                    sendMessage(sender,"§d§l検索ユーザーから過去のIPなどを検索")

                    for (pair in pairSet){ sendMessage(sender,"§c§l${pair.first} / ${pair.second}") }

                }.start()
            }


            "ip" ->{

                if (args.size<2)return

                val p = plugin.proxy.getPlayer(args[1])

                val ip = if (p == null) args[1] else getAddress(p)

                Thread{

                    val db = MySQLManager(plugin,"AltCheck")
                    val rs = db.query("select mcid from connection_log where ip='${ip}'")?:return@Thread

                    val playerList = mutableSetOf<String>()

                    while (rs.next()){ playerList.add(rs.getString("mcid")) }

                    rs.close()
                    db.close()

                    sendMessage(sender,"§d§l検索IP:${ip}")
                    sendMessage(sender,"§d§l同じIPのプレイヤー")

                    for (player in playerList){ sendMessage(sender,"§c§l${player}") }


                }.start()

            }

            "ban" ->{

                //malt ban mcid/ip reason

                if (args.size<3){
                    sendMessage(sender,"/malt ban <mcid/ip> <reason>")
                    return
                }

                val p = plugin.proxy.getPlayer(args[1])

                val ip = if (p == null) args[1] else getAddress(p)
                val reason = args[2]

                Thread{

                    val db = MySQLManager(plugin,"AltCheck")
                    val rs = db.query("select * from ban_ip_list where ip_address='${ip}';")

                    if (rs!=null && rs.next()){
                        sendMessage(sender,"§cIP:\"${ip}\"は、「${rs.getString("reason")}」の理由ですでにBanされています")
                        rs.close()
                        db.close()
                        return@Thread
                    }

                    db.execute("INSERT INTO ban_ip_list (ip_address, date, reason) " +
                            "VALUES ('${ip}', DEFAULT, '$reason')")

                    Man10BungeePlugin.banIpList.add(ip)

                    //ログインしてるユーザーをキック
                    for (player in ProxyServer.getInstance().players){
                        if (getAddress(player) == ip){
                            player.disconnect(*ComponentBuilder("あなたを「$reason」の理由でBanしました").create())
                        }
                    }

                    sendMessage(sender,"IP:\"${ip}\"を、「$reason」の理由でIPBanしました")
                    plugin.discord.jail("IP:\"${ip}\"を、「$reason」の理由でIPBanしました(処罰者:${sender.name})")


                }.start()
            }

            "unban" ->{

                if (args.size<2)return

                val ip = args[1]

                Thread{

                    val db = MySQLManager(plugin,"AltCheck")
                    val rs = db.query("select * from ban_ip_list where ip_address='${ip}';")

                    if (rs==null || !rs.next()){
                        sendMessage(sender,"IP:${ip}はBanされていません")
                        db.close()
                        return@Thread
                    }

                    db.execute("DELETE FROM ban_ip_list where ip_address='${ip}';")

                    Man10BungeePlugin.banIpList.remove(ip)

                    sendMessage(sender,"IP:\"${ip}\"のIPBanを解除しました")
                    plugin.discord.jail("IP:\"${ip}\"のIPBanを解除しました(解除:${sender.name})")

                }.start()

            }

            "reload" ->{

                Thread{
                    Man10BungeePlugin.banIpList = getBanIPList()
                }.start()
            }
        }

    }

    fun getAddress(p: ProxiedPlayer):String{
        val address = p.socketAddress.toString().replace("/","")
        return address.split(":")[0]
    }

    fun getBanIPList():MutableList<String>{

        val db = MySQLManager(plugin,"AltCheck")
        val rs = db.query("select ip_address from ban_ip_list;")?:return Collections.emptyList()

        val list = mutableListOf<String>()

        while (rs.next()){
            list.add(rs.getString(1))
        }

        rs.close()
        db.close()
        return list
    }
}