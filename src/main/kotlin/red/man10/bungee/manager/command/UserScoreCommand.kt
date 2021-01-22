package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.playerDataDic

object UserScoreCommand :Command("score","bungeemanager.score.user"){
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        val data = playerDataDic[ProxyServer.getInstance().getPlayer(sender.name)!!.uniqueId]!!

        if (args.isNullOrEmpty()){

            sender.sendMessage(*ComponentBuilder("§a§l現在のスコア:${data.getScore()}").create())

            return
        }

        when(args[0]){

            "help" ->{
                sender.sendMessage(*ComponentBuilder("§a§lスコアコマンド").create())
                sender.sendMessage(*ComponentBuilder("§a§l/score thank <player> : 相手にThankします").create())
                sender.sendMessage(*ComponentBuilder("§a§l/score fuck <player> : 相手をなじりたいときに使います").create())
                return
            }

            "thank" ->{

                val p = ProxyServer.getInstance().getPlayer(args[1])

                if (p == null){
                    sender.sendMessage(*ComponentBuilder("相手がオフラインです").create())
                    return
                }

                if (sender.name == p.name){
                    sender.sendMessage(*ComponentBuilder("§c自分にはThankできません").create())
                    return
                }

            }

            "fuck" ->{

                val p = ProxyServer.getInstance().getPlayer(args[1])

                if (p == null){

                    sender.sendMessage(*ComponentBuilder("相手がオフラインです").create())

                    return
                }

                data.takeScore(20,"相手にFuckといった",data.mcid)

                playerDataDic[p.uniqueId]!!.saveScore("相手からFuckされた",sender.name,0)

                playerDataDic[data.uuid] = data

                return
            }

        }

        return
    }
}