package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.playerDataDic
import red.man10.bungee.manager.PlayerData
import java.text.SimpleDateFormat

object BanCommand : Command("mban","bungeemanager.ban"){

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender==null)return


        //mban <forest611> <100d> <reason>
        if (!args.isNullOrEmpty() && args.size == 3){

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            val data = playerDataDic[pData.uniqueId]

            if (data !=null){
                punishment(data, args, sender)
                return
            }

            val pair = PlayerData.get(args[0])

            if (pair ==null){
                sender.sendMessage(*ComponentBuilder("§4存在しないユーザーです").create())
                return
            }

            Thread{
                punishment(pair.first, args, sender)
            }.start()

            return

        }

        sender.sendMessage(*ComponentBuilder("§d§l/mban <mcid> <期間+(d/h/m/reset(解除))> <Ban理由>").create())
        sender.sendMessage(*ComponentBuilder("§d§l期間をマイナスにすると期間が縮みます").create())

    }

    fun punishment(data:PlayerData,args: Array<out String>,sender: CommandSender){

        if (args[1] == "reset"){

            data.resetBan()
            playerDataDic[data.uuid] = data
            ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${data.mcid}はBAN解除されました").create())
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

        if (!data.isBanned() && time <0){
            sender.sendMessage(*ComponentBuilder("§c§lこのユーザーは既にBAN解除されています！").create())
            return
        }

        when(unit){

            'd' ->data.addBanTime(0,0,time)
            'h' ->data.addBanTime(0,time,0)
            'm' ->data.addBanTime(time,0,0)

            else -> {
                sender.sendMessage(*ComponentBuilder("§c§l時間の指定方法が不適切です").create())
                return
            }

        }

        if (!data.isBanned()){
            ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${data.mcid}はBAN解除されました").create())
            playerDataDic[data.uuid] = data
            return
        }

        ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l${data.mcid}は「${args[2]}」の理由により、BANされました！").create())
        ProxyServer.getInstance().broadcast(*ComponentBuilder("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(data.banUntil)}").create())
        playerDataDic[data.uuid] = data

        val p = ProxyServer.getInstance().getPlayer(data.mcid)

        if (p !=null){
            p.disconnect(*ComponentBuilder("§4§lYou are banned. : あなたはこのサーバーからBanされています").create())
            return
        }

    }

}