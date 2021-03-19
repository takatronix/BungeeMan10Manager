package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.PlayerData

object ScoreCommand : Command("mscore","bungeemanager.score.op"){

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args.isNullOrEmpty()){

            sender.sendMessage(*ComponentBuilder("§a/mscore give <player> <score> <reason>").create())
            sender.sendMessage(*ComponentBuilder("§a/mscore take <player> <score> <reason>").create())
            sender.sendMessage(*ComponentBuilder("§a/mscore set <player> <score>").create())
            sender.sendMessage(*ComponentBuilder("§a/mscore show <player>").create())

            return
        }


        val pData = ProxyServer.getInstance().getPlayer(args[1])

        if (pData != null){
            val data = Man10BungeePlugin.playerDataDic[pData.uniqueId]!!

            setScore(pData,data,sender,args)
            return

        }

//        Thread{
//
//            val pair = PlayerData.get(args[0])
//
//            if (pair ==null){
//                sender.sendMessage(*ComponentBuilder("§4存在しないユーザーです").create())
//                return@Thread
//            }
//
//            setScore(pair.first,sender,args)
//        }.start()


    }

    private fun setScore(p:ProxiedPlayer,data:PlayerData, sender:CommandSender, args: Array<out String>){

        when(args[0]){


            "give" ->{

                data.addScore(args[2].toInt(),args[3],sender.name)

            }

            "take" ->{

                data.takeScore(args[2].toInt(),args[3],sender.name)

            }

            "set" ->{

                data.setScore(args[2].toInt())

            }

            else ->{ }

        }

        sender.sendMessage(*ComponentBuilder("§a${data.mcid}のスコア:${data.getScore()}").create())

        Man10BungeePlugin.playerDataDic[data.uuid] = data

        return

    }
}