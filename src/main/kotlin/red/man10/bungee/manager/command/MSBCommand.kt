package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.msbMessage
import red.man10.bungee.manager.Man10BungeePlugin.Companion.msg
import red.man10.bungee.manager.PlayerData
import java.text.SimpleDateFormat

object MSBCommand : Command("msb","bungeemanager.msb"){
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender==null)return


        //msb <forest611> <100d> <reason>
        if (!args.isNullOrEmpty() && args.size == 3){

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            if (pData != null){
                val data = Man10BungeePlugin.playerDataDic[pData.uniqueId]!!

                punishment(data, args, sender)
                return

            }

            Thread{
                val pair = PlayerData.get(args[0])

                if (pair ==null){
                    msg(sender, "§4存在しないユーザーです")
                    return@Thread
                }

                punishment(pair.first, args, sender)
            }.start()

            return

        }

        msg(sender, "§d§l/msb <mcid> <期間+(d/h/m/0k/reset(解除))> <理由>")
        msg(sender, "§d§l/msb <mcid> reset MSB解除")


        return
    }

    private fun punishment(data: PlayerData, args: Array<out String>, sender: CommandSender){

        if (args[1] == "reset"){

            data.resetMSB()
            Man10BungeePlugin.playerDataDic[data.uuid] = data
            msg(sender,"§c§l${data.mcid}はMSB解除されました")
            return
        }


        val unit = args[1][args[1].length - 1]

        val time: Int
        try {
            time = args[1].replace(unit.toString(), "").toInt()
        } catch (e: Exception) {
            msg(sender, "§c§l時間の指定方法が不適切です")
            return
        }

        if (!data.isMSB() && time <0){
            msg(sender, "§c§lこのユーザーは既にMSB解除されています！")
            return
        }

        when(unit){

            'd' ->data.addMSBTime(0,0,time)
            'h' ->data.addMSBTime(0,time,0)
            'm' ->data.addMSBTime(time,0,0)
            'k' ->data.addMSBTime(0,0,383512)

            else -> {
                msg(sender, "§c§l時間の指定方法が不適切です")
                return
            }

        }

        if (!data.isMSB()){
            msg(sender,"§c§l${data.mcid}はMSB解除されました")
            Man10BungeePlugin.playerDataDic[data.uuid] = data
            return
        }

        Man10BungeePlugin.playerDataDic[data.uuid] = data

        Man10BungeePlugin.discord.jail("${data.mcid}は「${args[2]}」の理由によりMSBされました！(処罰者:${sender.name})")
        Man10BungeePlugin.discord.jail("解除日:${SimpleDateFormat("yyyy/MM/dd").format(data.msbUntil)}")

        val p = ProxyServer.getInstance().getPlayer(data.mcid)

        if (p !=null){
            p.disconnect(*ComponentBuilder(msbMessage).create())
            return
        }

    }

}