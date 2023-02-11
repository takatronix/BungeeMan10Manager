package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.PlayerData
import red.man10.bungee.manager.db.ScoreDatabase

object WarnCommand : Command("mwarn", "bungeemanager.warn") {
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null) return


        //mwarn <forest611> <100d> <reason>
        if (!args.isNullOrEmpty() && args.size == 3) {

            val pData = ProxyServer.getInstance().getPlayer(args[0])

            if (pData != null) {
                val data = Man10BungeePlugin.playerDataDic[pData.uniqueId]!!

                punishment(data, args, sender)
                return

            }

            val pair = PlayerData.get(args[0])

            if (pair == null) {
                Man10BungeePlugin.sendMessage(sender, "§4存在しないユーザーです")
                return
            }

            Thread {
                punishment(pair.first, args, sender)
            }.start()


            return

        }

        Man10BungeePlugin.sendMessage(sender, "§d§l/mwarn <mcid> <減らすスコア> <警告理由>")


    }

    private fun punishment(data: PlayerData, args: Array<out String>, sender: CommandSender) {

        val score = args[1].toIntOrNull() ?: return
        val reason = args[2]

        Man10BungeePlugin.sendGlobalMessage("§c${data.mcid}は「${reason}」の理由により${score}ポイント引かれ、警告されました！")
        ScoreDatabase.giveScore(data.mcid, -score, "${reason}により警告", sender)

        Man10BungeePlugin.discord.jail("${data.mcid}は「${reason}」の理由により${score}ポイント引かれ、警告されました！(処罰者:${sender.name})")

    }


}