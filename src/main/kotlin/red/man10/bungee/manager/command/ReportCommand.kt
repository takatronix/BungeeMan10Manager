package red.man10.bungee.manager.command

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.msg
import java.text.SimpleDateFormat
import java.util.*

object ReportCommand : Command("report","bungeemanager.report"){

    private val lastSendReport = HashMap<CommandSender,String>()

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (args == null || args.size < 2){
            msg(sender,"§a§l〜サーバーへ報告をする〜")
            msg(sender,"§a/report <タイトル> <本文>")
            msg(sender,"§d不具合のの報告や、あらしをしていた場合に報告をしてください。")
            msg(sender,"§dいたずら目的で使ってはいけません。")
            msg(sender,"§dうっかり途中で送信してしまった場合は、")
            msg(sender,"§d同じタイトルで、つづきを書いてください。")
            msg(sender,"§dレポートの内容は、即座にサーバー運営が見れるチャンネルに転送されます。")
            return
        }

        val last = lastSendReport[sender]

        val msgBuilder = StringBuilder()

        for (i in 1 until args.size){
            msgBuilder.append(args[i]).append(" ")
        }

        val msg = msgBuilder.toString()

        if (last != null &&last == msg){
            msg(sender,"§c§l同じ内容を複数回レポートすることはできません")
            return
        }

        lastSendReport[sender] = msg

        val title = args[0] + "(${(Japanizer.japanize(args[0], JapanizeType.GOOGLE_IME , Man10BungeePlugin.JapanizerDictionary)?:"")})"
        val body = msg + "(${(Japanizer.japanize(msg, JapanizeType.GOOGLE_IME ,Man10BungeePlugin.JapanizerDictionary)?:"")})"

        val text = StringBuilder()

        text.append("送信者:${sender.name}\n")
        text.append("送信日:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}\n")
        text.append("タイトル:${title}\n")
        text.append("本文:${body}")

        Man10BungeePlugin.discord.report(text.toString())


        msg(sender,"§f§l送信したタイトル:${title}")
        msg(sender,"§f§l送信した内容:${body}")
        msg(sender,"§a§l送信しました！ご協力ありがとうございます！")

    }


}