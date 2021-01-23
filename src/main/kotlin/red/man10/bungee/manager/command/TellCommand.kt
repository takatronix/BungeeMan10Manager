package red.man10.bungee.manager.command

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import java.util.*


open class TellCommand(plugin: Man10BungeePlugin, name: String) : Command(name) {
    var viewlist: ArrayList<UUID>? = ArrayList()
    open val plugin: Man10BungeePlugin
    var history = HashMap<String, String>()

    init {
        this.plugin = plugin
    }

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        // 引数が足らないので、Usageを表示して終了する。
        if (args?.size!! <= 1) {
            sendMessage(sender!!, "§c/$name <player> <message> : Send private message.")
            return
        }

        // 自分自身には送信できない。
        if (args[0] == sender?.name) {
            sendMessage(
                sender, """
    §c自分自身にはプライベートメッセージを送信することができません。
    §cCannot send a private message to myself.""${'"'}
    """.trimIndent()
            )
            return
        }

        // 送信先プレイヤーの取得。取得できないならエラーを表示して終了する。

        // 送信先プレイヤーの取得。取得できないならエラーを表示して終了する。
        val reciever = plugin.proxy.getPlayer(args[0])
        if (reciever == null) {
            sendMessage(
                sender!!, """
     §cメッセージ送信先が見つかりません。
     §cThe destination for the message was not found.
     """.trimIndent()
            )
            return
        }

        // 送信メッセージの作成

        // 送信メッセージの作成
        val str = StringBuilder()
        for (i in 1 until args.size) {
            str.append(args[i] + " ")
        }
        val message = str.toString().trim { it <= ' ' }

        // 送信

        // 送信
        sendPrivateMessage(sender!!, reciever, message)
    }

    var dic = HashMap<String, String>()

    /**
     * プライベートメッセージを送信する
     *
     * @param sender   送信者
     * @param reciever 受信者名
     * @param message  メッセージ
     */
     fun sendPrivateMessage(sender: CommandSender, reciever: ProxiedPlayer, message: String?) {
        // Japanizeの付加
        var msg = ChatColor.translateAlternateColorCodes('&', message)
        var msgs = ""
        msgs = Japanizer.japanize(msg, JapanizeType.GOOGLE_IME, dic)
        if (!msgs.equals("", ignoreCase = true)) {
            msg = "$msg §6($msgs)"
        }

        // フォーマットの適用
        var senderServer = "console"
        if (sender is ProxiedPlayer) {
            senderServer = sender.server.info.name
        }
        val endmsg =
            "§7[" + sender.name + "@" + senderServer + " > " + reciever.name + "@" + reciever.server.info.name + "] §f" + msg
        // メッセージ送信
        sendMessage(sender, endmsg)
        sendMessage(reciever, endmsg)
        //履歴をput
        putHistory(reciever.name, sender.name)
        // コンソールに表示設定なら、コンソールに表示する
        plugin.logger.info(endmsg)
    }


    /**
     * 指定した対象にメッセージを送信する
     *
     * @param reciever 送信先
     * @param message  メッセージ
     */
    fun sendMessage(reciever: CommandSender, message: String?) {
        if (message == null) return
        reciever.sendMessage(*ComponentBuilder(message).create())
    }

    fun putHistory(reciever: String?, sender: String?) {
        history[reciever!!] = sender!!
    }

    fun getHistory(reciever: String?): String? {
        return history[reciever]
    }
}