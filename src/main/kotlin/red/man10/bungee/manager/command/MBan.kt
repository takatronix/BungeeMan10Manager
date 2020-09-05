package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import java.lang.Exception
import java.text.SimpleDateFormat

class MBan (name: String, permission: String,private val plugin: Man10BungeePlugin) : Command(name, permission) {

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args.isNullOrEmpty()){

            sender.sendMessage(*ComponentBuilder("§d§l/mban <mcid> <期間+(d/h/m)> <Ban理由>").create())
            sender.sendMessage(*ComponentBuilder("§d§l期間をマイナスにすると期間が縮みます").create())

            return
        }

        //mban <user> <time> <reason>
        if (args.size == 3){

            val p = plugin.proxy.getPlayer(args[0])

            if (p == null){
                sender.sendMessage(*ComponentBuilder("§c§lそのユーザーは現在オンラインではありません！").create())
                return
            }

            val pd = plugin.playerDataDic[p.uniqueId]!!

            val unit = args[1][args[1].length-1]

            var time = 0
            try {
                time = args[1].replace(unit.toString(),"").toInt()
            }catch (e: Exception){
                sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                return
            }

            if (!pd.isBanned() && time <0){
                sender.sendMessage(*ComponentBuilder("§c§lこのユーザーは既にBAN解除されています！").create())
                return
            }

            when(unit){

                'd' ->pd.addBanTime(0,0,time)
                'h' ->pd.addBanTime(0,time,0)
                'm' ->pd.addBanTime(time,0,0)

                else -> {
                    sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                    return
                }


            }

            if (time <0){

                if (!pd.isBanned()){
                    ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${p.name}はBAN解除されました").create())
                    plugin.playerDataDic[p.uniqueId] = pd
                    return
                }

                ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${p.name}は「${args[2]}」の理由により、BAN解除日が早まりました").create())
                ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(pd.banUntil)}").create())

            }else{
                ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${p.name}は「${args[2]}」の理由により、BANされました！").create())
                ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(pd.banUntil)}").create())

            }

            plugin.playerDataDic[p.uniqueId] = pd
        }

    }
}