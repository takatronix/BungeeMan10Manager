package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.playerDataDic
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendGlobalMessage
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import red.man10.bungee.manager.PlayerData
import java.text.SimpleDateFormat

object MuteCommand : Command("mmute", "bungeemanager.mute") {

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null) return


        //mmute <forest611> <100d> <reason>
        if (!args.isNullOrEmpty() && args.size == 3) {

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            if (pData != null) {
                val data = playerDataDic[pData.uniqueId]!!

                punishment(data, args, sender)
                return

            }

            val pair = PlayerData.get(args[0])

            if (pair == null) {
                sendMessage(sender, "§4存在しないユーザーです")
                return
            }

            Thread {
                punishment(pair.first, args, sender)
            }.start()


            return

        }

        sendMessage(sender, "§d§l/mmute <mcid> <期間+(d/h/m/reset(解除))> <ミュート理由>")
        sendMessage(sender, "§d§l/mmute <mcid> reset ミュート解除")

    }

    private fun punishment(data: PlayerData, args: Array<out String>, sender: CommandSender) {

        if (args[1] == "reset") {

            data.resetMute()
            playerDataDic[data.uuid] = data
            sendGlobalMessage("§c§l${data.mcid}はミュート解除されました")
            return
        }

        val unit = args[1][args[1].length - 1]

        val time: Int
        try {
            time = args[1].replace(unit.toString(), "").toInt()
        } catch (e: Exception) {
            sendMessage(sender, "§c§l時間の指定方法が不適切です")
            return
        }

        if (!data.isMuted() && time < 0) {
            sendMessage(sender, "§c§lこのユーザーは既にミュート解除されています！")
            return
        }

        when (unit) {

            'd' -> data.addMuteTime(0, 0, time)
            'h' -> data.addMuteTime(0, time, 0)
            'm' -> data.addMuteTime(time, 0, 0)

            else -> {
                sendMessage(sender, "§c§l時間の指定方法が不適切です")
                return
            }

        }

        if (!data.isMuted()) {
            sendMessage(sender, "§c§l${data.mcid}はミュート解除されました")
            playerDataDic[data.uuid] = data
            return
        }

        sendMessage(sender, "§c§l${args[0]}をミュートしました")

        val p = ProxyServer.getInstance().getPlayer(data.mcid)

        if (p != null) {
            sendMessage(p, "§c§lあなたは「${args[2]}」の理由により、ミュートされました！")
            sendMessage(p, "§c§l解除日:${SimpleDateFormat("yyyy/MM/dd").format(data.muteUntil)}")
        }

        Man10BungeePlugin.discord.jail("${args[0]}は「${args[2]}」の理由により、ミュートされました！(処罰者:${sender.name})")

        playerDataDic[data.uuid] = data

    }

}