package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.db.PlayerDatabase
import java.lang.Exception
import java.text.SimpleDateFormat

class MMute (name: String, permission: String,private val plugin: Man10BungeePlugin) : Command(name, permission) {
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args.isNullOrEmpty()){

            sender.sendMessage(*ComponentBuilder("§d§l/mmute <mcid> <期間+(d/h/m)> <ミュート理由>").create())
            sender.sendMessage(*ComponentBuilder("§d§l期間をマイナスにするとミュート期間が縮みます").create())

            return
        }

        //mjail <user> <time> <reason>
        if (args.size == 3){

            val p = plugin.proxy.getPlayer(args[0])

            val unit = args[1][args[1].length-1]

            var time = 0
            try {
                time = args[1].replace(unit.toString(),"").toInt()
            }catch (e: Exception){
                sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                return
            }

            if (p == null){
                val mcid = args[0]

                val didFrozen =  when(unit){

                    'd' -> Man10BungeePlugin.playerDatabase.addTime(PlayerDatabase.Punishment.MUTE,mcid,0,0,time)
                    'h' -> Man10BungeePlugin.playerDatabase.addTime(PlayerDatabase.Punishment.MUTE,mcid,0,time,0)
                    'm' -> Man10BungeePlugin.playerDatabase.addTime(PlayerDatabase.Punishment.MUTE,mcid,time,0,0)

                    else -> {
                        sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                        return
                    }
                }

                if (!didFrozen){
                    sender.sendMessage(*ComponentBuilder("§c§l存在しないユーザーです").create())
                    return
                }

                sender.sendMessage(*ComponentBuilder("§c§l${mcid}をミュートしました！").create())

                return

            }

            val pd = plugin.playerDataDic[p.uniqueId]!!

            if (!pd.isMuted() && time <0){
                sender.sendMessage(*ComponentBuilder("§c§lこのユーザーは既にミュートを解除されています！").create())
                return
            }

            when(unit){

                'd' ->pd.addMuteTime(0,0,time)
                'h' ->pd.addMuteTime(0,time,0)
                'm' ->pd.addMuteTime(time,0,0)

                else -> {
                    sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                    return
                }


            }


            if (time <0){

                if (!pd.isMuted()){
                    sender.sendMessage(*ComponentBuilder("§e§l${p.name}はミュート解除されました").create())
                    p.sendMessage(*ComponentBuilder("§e§lあなたはミュート解除されました").create())
                    plugin.playerDataDic[p.uniqueId] = pd
                    return
                }

                sender.sendMessage(*ComponentBuilder("§c§l${p.name}を「${args[2]}」の理由でミュート期間を短くしました").create())

                p.sendMessage(*ComponentBuilder("§c§lミュート期間が縮まりました！").create())
                p.sendMessage(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(pd.muteUntil)}").create())

            }else{
                sender.sendMessage(*ComponentBuilder("§c§l${p.name}を「${args[2]}」の理由でミュートしました！").create())

                p.sendMessage(*ComponentBuilder("§c§lあなたは「${args[2]}」の理由でミュートされました！").create())
                p.sendMessage(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(pd.muteUntil)}").create())

            }

            plugin.playerDataDic[p.uniqueId] = pd
        }



    }
}