package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.playerDataDic
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.globalMessage
import red.man10.bungee.manager.Man10BungeePlugin.Companion.msg
import red.man10.bungee.manager.PlayerData
import red.man10.bungee.manager.db.ScoreDatabase
import java.text.SimpleDateFormat

object JailCommand : Command("mjail", "bungeemanager.jail") {

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null) return

        if (args.isNullOrEmpty()){
            msg(sender, "§d§l/mjail <mcid> <期間+(d/h/m/0k/reset(解除))> <Jail理由>")
            msg(sender, "§d§l/mjail <mcid> reset 釈放します")
            return
        }

        //mjail <forest611> <100d> <reason>
        if (args.size == 3 || (args[1] == "reset" && args.size == 2)) {

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            if (pData != null) {
                val data = playerDataDic[pData.uniqueId]!!

                punishment(data, args, sender)
                return
            }

            val pair = PlayerData.get(args[0])

            if (pair == null) {
                msg(sender, "§4存在しないユーザーです")
                return
            }

            Thread {
                punishment(pair.first, args, sender)
            }.start()

            return

        }
    }

    private fun punishment(data: PlayerData, args: Array<out String>, sender: CommandSender) {

        if (args[1] == "reset") {

            data.resetJail()
            playerDataDic[data.uuid] = data
            globalMessage("§c§l${data.mcid}は釈放されました")
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

        if (!data.isJailed() && time < 0) {
            msg(sender, "§c§lこのユーザーは既に釈放されています！")
            return
        }

        when (unit) {

            'd' -> data.addJailTime(0, 0, time)
            'h' -> data.addJailTime(0, time, 0)
            'm' -> data.addJailTime(time, 0, 0)
            'k' -> data.addJailTime(0, 0, 383512)

            else -> {
                msg(sender, "§c§l時間の指定方法が不適切です")
                return
            }

        }

        msg(sender,"§c§l${data.mcid}は「${args[2]}」の理由により、300ポイント引かれ、Jailされました！")

        ScoreDatabase.giveScore(data.mcid, -300, "${args[2]}によりJail", sender)

        playerDataDic[data.uuid] = data

        Man10BungeePlugin.discord.jail("${data.mcid}は「${args[2]}」の理由によりJailされました！(処罰者:${sender.name})")
        Man10BungeePlugin.discord.jail("釈放日:${SimpleDateFormat("yyyy/MM/dd").format(data.jailUntil)}")

        val p = ProxyServer.getInstance().getPlayer(data.mcid)

        if (p != null) {
            msg(p,"§c§lあなたは「${args[2]}」の理由により、300ポイント引かれ、島流しにされました！")
            plugin.sendToJail(p)
            return
        }

    }
}