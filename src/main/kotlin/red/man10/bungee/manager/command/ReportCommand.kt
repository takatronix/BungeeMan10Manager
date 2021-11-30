package red.man10.bungee.manager.command

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

object ReportCommand : Command("report","bungeemanager.report"){

    private val cooldownMap = HashMap<CommandSender,Pair<Int,Date>>()
    private const val maxReport = 3

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

        val pair = cooldownMap[sender]

        if (pair!=null){

            val now = LocalDateTime.now()
            val lastReportTime = LocalDateTime.ofInstant(pair.second.toInstant(), ZoneId.systemDefault())

            val diff = ChronoUnit.MINUTES.between(lastReportTime,now)
            println("${diff},${pair.first}")
            if (diff<5 && pair.first>= maxReport){
                sendMessage(sender,"§c§l一定時間内にレポートコマンドを使う回数が多すぎます！")
                return
            }

            cooldownMap[sender] = Pair(pair.first+1,Date())
        }else{
            cooldownMap[sender] = Pair(1,Date())
        }


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