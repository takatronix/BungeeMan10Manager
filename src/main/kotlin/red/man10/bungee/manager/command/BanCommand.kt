package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.playerDataDic
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendGlobalMessage
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import red.man10.bungee.manager.PlayerData
import java.text.SimpleDateFormat

object BanCommand : Command("mban","bungeemanager.ban"){

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender==null)return


        //mban <forest611> <100d> <reason>
        if (!args.isNullOrEmpty() && args.size == 3){

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            if (pData != null){
                val data = playerDataDic[pData.uniqueId]!!

                punishment(data, args, sender)
                return

            }

            val pair = PlayerData.get(args[0])

            if (pair ==null){
                sendMessage(sender,"§4存在しないユーザーです")
                return
            }

            Thread{
                punishment(pair.first, args, sender)
            }.start()

            return

        }

        sendMessage(sender,"§d§l/mban <mcid> <期間+(d/h/m/0k/reset(解除))> <Ban理由>")
        sendMessage(sender,"§d§l期間をマイナスにすると期間が縮みます")

    }

    private fun punishment(data:PlayerData,args: Array<out String>,sender: CommandSender){

        if (args[1] == "reset"){

            data.resetBan()
            playerDataDic[data.uuid] = data
            sendGlobalMessage("§c§l${data.mcid}はBAN解除されました")
            return
        }


        val unit = args[1][args[1].length - 1]

        val time: Int
        try {
            time = args[1].replace(unit.toString(), "").toInt()
        } catch (e: Exception) {
            sendMessage(sender,"§c§l時間の指定方法が不適切です")
            return
        }

        if (!data.isBanned() && time <0){
            sendMessage(sender,"§c§lこのユーザーは既にBAN解除されています！")
            return
        }

        when(unit){

            'd' ->data.addBanTime(0,0,time)
            'h' ->data.addBanTime(0,time,0)
            'm' ->data.addBanTime(time,0,0)
            'k' ->data.addBanTime(0,0,383512)

            else -> {
                sendMessage(sender,"§c§l時間の指定方法が不適切です")
                return
            }

        }

        if (!data.isBanned()){
            sendGlobalMessage("§c§l${data.mcid}はBAN解除されました")
            playerDataDic[data.uuid] = data
            return
        }

        sendGlobalMessage("§c§l${data.mcid}は「${args[2]}」の理由により、BANされました！")
        sendGlobalMessage("§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(data.banUntil)}")
        if (unit == 'k'){
            sendGlobalMessage("§c1050年地下行きっ・・・・・・・・！")
        }
        playerDataDic[data.uuid] = data

        val p = ProxyServer.getInstance().getPlayer(data.mcid)

        if (p !=null){
            p.disconnect(*ComponentBuilder("§4§lYou are banned. : あなたはこのサーバーからBanされています").create())
            return
        }

    }

}