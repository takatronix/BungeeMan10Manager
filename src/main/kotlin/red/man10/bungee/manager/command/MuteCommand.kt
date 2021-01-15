package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import java.text.SimpleDateFormat

object MuteCommand : Command("mmute","bungeemanager.mute"){

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender==null)return


        //mmute <forest611> <100d> <reason>
        if (!args.isNullOrEmpty() && args.size == 3){

            val p = args[0]

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            val data = Man10BungeePlugin.playerDataDic[pData.uniqueId]

            if (data ==null){
                sender.sendMessage(*ComponentBuilder("§4オフラインのユーザーです").create())
                return
            }

            val unit = args[1][args[1].length - 1]

            val time: Int
            try {
                time = args[1].replace(unit.toString(), "").toInt()
            } catch (e: Exception) {
                sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                return
            }

            if (!data.isMuted() && time <0){
                sender.sendMessage(*ComponentBuilder("§c§lこのユーザーは既にミュート解除されています！").create())
                return
            }

            when(unit){

                'd' ->data.addMuteTime(0,0,time)
                'h' ->data.addMuteTime(0,time,0)
                'm' ->data.addMuteTime(time,0,0)

                else -> {
                    sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                    return
                }

            }

            if (!data.isMuted()){
                ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${p}はミュート解除されました").create())
                Man10BungeePlugin.playerDataDic[data.uuid] = data
                return
            }

            sender.sendMessage(*ComponentBuilder("§c§l${args[1]}をミュートしました").create())
            ProxyServer.getInstance().getPlayer(p)!!.sendMessage(*ComponentBuilder("§c§lあなたは「${args[2]}」の理由により、ミュートされました！").create())
            ProxyServer.getInstance().getPlayer(p)!!.sendMessage(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(data.muteUntil)}").create())
            Man10BungeePlugin.playerDataDic[data.uuid] = data


            return

        }

        sender.sendMessage(*ComponentBuilder("§d§l/mmute <mcid> <期間+(d/h/m)> <ミュート理由>").create())
        sender.sendMessage(*ComponentBuilder("§d§l期間をマイナスにすると期間が縮みます").create())

    }

}