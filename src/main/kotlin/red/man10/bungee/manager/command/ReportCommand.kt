package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import java.text.SimpleDateFormat
import java.util.*

object ReportCommand : Command("report","bungeemanager.report"){
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

//        if (sender !is ProxiedPlayer)return

        if (args == null || args.size != 2){
            sendMessage(sender,"§a§l〜サーバーへ報告をする〜")
            sendMessage(sender,"§a/report <タイトル> <本文>")
            sendMessage(sender,"§d不具合のの報告や、あらしをしていた場合に報告をしてください。")
            sendMessage(sender,"§dいたずら目的で使った場合は処罰されます。")
            sendMessage(sender,"§dうっかり途中で送信してしまった場合は、")
            sendMessage(sender,"§d同じタイトルで、書き続けてください。")
            return
        }

        val text = StringBuilder()

        text.append("送信者:${sender.name}\n")
        text.append("送信日:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}\n")
        text.append("タイトル:${args[0]}\n")
        text.append("本文:${args[1]}")

        plugin.discord.report(text.toString())

        sendMessage(sender,"§a§l送信しました！ご協力ありがとうございます！")

    }


}