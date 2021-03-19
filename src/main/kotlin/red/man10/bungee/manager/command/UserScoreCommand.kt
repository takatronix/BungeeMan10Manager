package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.playerDataDic
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendGlobalMessage
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage

object UserScoreCommand :Command("score","bungeemanager.score.user"){
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        val data = playerDataDic[ProxyServer.getInstance().getPlayer(sender.name)!!.uniqueId]!!

        if (args.isNullOrEmpty()){

            sendMessage(sender,"§a§l現在のスコア:${data.getScore()}")

            return
        }

        when(args[0]){

            "help" ->{
                sendMessage(sender,"§a§lスコアコマンド")
                sendMessage(sender,"§a§l/score thank <player> : 相手にThankします")
                sendMessage(sender,"§a§l/score fuck <player> : 相手をなじりたいときに使います")
                return
            }

            "thank" ->{

                val p = ProxyServer.getInstance().getPlayer(args[1])

                if (p == null){
                    sendMessage(sender,"§c${p?.name}はオフラインです。")
                    return
                }

                if (sender.name == p.name){
                    sendMessage(sender,"§c自分自身に感謝できません。")
                    return
                }

                data.addScore(5, "${p.name}に感謝をした", data.mcid)

                playerDataDic[p.uniqueId]!!.saveScore("${sender.name}から感謝された",sender.name,0)

                playerDataDic[data.uuid] = data

                sendGlobalMessage("§a${p.name}さんは${sender.name}さんに§d感謝§aされ、5ポイント獲得しました。")

                return
            }

            "fuck" ->{

                val p = ProxyServer.getInstance().getPlayer(args[1])

                if (p == null){
                    sendMessage(sender,"§c${p?.name}はオフラインです。")
                    return
                }

                data.takeScore(20,"${p.name}にFuckといった",data.mcid)

                playerDataDic[p.uniqueId]!!.saveScore("${sender.name}からFuckされた",sender.name,0)

                playerDataDic[data.uuid] = data

                sendGlobalMessage("§c§l${sender.name}さんは${p.name}さんにF**Kと言い、20ポイント失いました。")

                return
            }

        }

        return
    }
}