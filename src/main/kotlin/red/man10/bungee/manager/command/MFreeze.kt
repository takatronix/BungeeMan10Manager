package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import java.lang.Exception
import java.text.SimpleDateFormat

class MFreeze (name: String, permission: String, private val plugin: Man10BungeePlugin) : Command(name, permission) {
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args.isNullOrEmpty()){

            sender.sendMessage(*ComponentBuilder("§d§l/mfreeze <mcid> <期間+(d/h/m)> <フリーズ理由>").create())
            sender.sendMessage(*ComponentBuilder("§d§l期間をマイナスにするとフリーズ期間が縮みます").create())

            return
        }

        //mjail <user> <time> <reason>
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

            if (!pd.isMuted() && time <0){
                sender.sendMessage(*ComponentBuilder("§c§lこのユーザーは既にフリーズを解除されています！").create())
                return
            }

            when(unit){

                'd' ->pd.addFrozenTime(0,0,time)
                'h' ->pd.addFrozenTime(0,time,0)
                'm' ->pd.addFrozenTime(time,0,0)

                else -> {
                    sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                    return
                }


            }

            if (time <0){

                if (!pd.isFrozen()){
                    sender.sendMessage(*ComponentBuilder("§e§l${p.name}はフリーズ解除されました").create())
                    p.sendMessage(*ComponentBuilder("§e§lあなたはフリーズ解除されました").create())
                    plugin.playerDataDic[p.uniqueId] = pd
                    return
                }

                sender.sendMessage(*ComponentBuilder("§c§l${p.name}を「${args[2]}」の理由でフリーズ期間を短くしました").create())

                p.sendMessage(*ComponentBuilder("§c§lフリーズ期間が縮まりました！").create())
                p.sendMessage(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(pd.freezeUntil)}").create())

            }else{
                sender.sendMessage(*ComponentBuilder("§c§l${p.name}を「${args[2]}」の理由でフリーズしました！").create())

                p.sendMessage(*ComponentBuilder("§c§lあなたは「${args[2]}」の理由でフリーズされました！").create())
                p.sendMessage(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(pd.freezeUntil)}").create())

            }

            plugin.playerDataDic[p.uniqueId] = pd
        }



    }
}