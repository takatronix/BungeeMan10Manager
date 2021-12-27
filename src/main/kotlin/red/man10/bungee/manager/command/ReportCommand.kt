package red.man10.bungee.manager.command

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import java.text.SimpleDateFormat
import java.util.*

object ReportCommand : Command("report","bungeemanager.report"){

    private val lastSendReport = HashMap<CommandSender,String>()

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args == null || args.size != 2){
            sendMessage(sender,"§a§l〜サーバーへ報告をする〜")
            sendMessage(sender,"§a/report <タイトル> <本文>")
            sendMessage(sender,"§d不具合のの報告や、あらしをしていた場合に報告をしてください。")
            sendMessage(sender,"§dいたずら目的で使った場合は処罰されます。")
            sendMessage(sender,"§dうっかり途中で送信してしまった場合は、")
            sendMessage(sender,"§d同じタイトルで、書き続けてください。")
            return
        }

        val last = lastSendReport[sender]


        if (last != null &&last == args[1]){
            sendMessage(sender,"§c§l同じ内容を複数回レポートすることはできません")
            return
        }

        lastSendReport[sender] = args[1]

        val title = args[0] + "(${(Japanizer.japanize(args[0], JapanizeType.GOOGLE_IME , plugin.dic)?:"")})"
        val body = args[1] + "(${(Japanizer.japanize(args[1], JapanizeType.GOOGLE_IME ,plugin.dic)?:"")})"

        val text = StringBuilder()

        text.append("送信者:${sender.name}\n")
        text.append("送信日:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}\n")
        text.append("タイトル:${title}\n")
        text.append("本文:${body}")

        plugin.discord.report(text.toString())


        sendMessage(sender,"§f§l送信したタイトル:${title}")
        sendMessage(sender,"§f§l送信した内容:${body}")
        sendMessage(sender,"§a§l送信しました！ご協力ありがとうございます！")

    }


}