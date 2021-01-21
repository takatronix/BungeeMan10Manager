package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin

object ScoreCommand : Command("mscore","bungeemanager.score.op"){

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args.isNullOrEmpty()){

            sender.sendMessage(*ComponentBuilder("§a/mscore give <player> <score>").create())
            sender.sendMessage(*ComponentBuilder("§a/mscore take <player> <score>").create())
            sender.sendMessage(*ComponentBuilder("§a/mscore set <player> <score>").create())
            sender.sendMessage(*ComponentBuilder("§a/mscore show <player>").create())

            return
        }


        val pData = ProxyServer.getInstance().getPlayer(args[1])

        val data = Man10BungeePlugin.playerDataDic[pData.uniqueId]

        if (data == null){
            sender.sendMessage(*ComponentBuilder("§4オフラインのユーザーです").create())
            return
        }

        when(args[0]){


            "give" ->{

                data.addScore(args[2].toInt())

            }

            "take" ->{

                //マイナス値
                data.takeScore(args[2].toInt())

            }

            "set" ->{

                data.setScore(args[2].toInt())

            }

            else ->{ }

        }

        sender.sendMessage(*ComponentBuilder("§a${data.mcid}のスコア:${data.getScore()}").create())

        Man10BungeePlugin.playerDataDic[pData.uniqueId] = data

        return

    }
}