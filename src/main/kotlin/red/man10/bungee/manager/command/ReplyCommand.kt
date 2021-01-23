package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import red.man10.bungee.manager.Man10BungeePlugin


class ReplyCommand(plugin: Man10BungeePlugin, name: String) : TellCommand(plugin, name) {
    override val plugin: Man10BungeePlugin = plugin

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        val recieverName = getHistory(sender!!.name)

        // 引数が無いときは、現在の会話相手を表示して終了する。
        if (args?.size == 0) {
            if (recieverName != null) {
                sendMessage(sender, "§dCurrent Conversation Partner： $recieverName");
            } else {
                sendMessage(
                    sender, """
    §c現在の会話相手はいません。
    §cThere is no current conversation partner.
    """.trimIndent()
                )
            }
            return
        }

        // 送信先プレイヤーの取得。取得できないならエラーを表示して終了する。

        // 送信先プレイヤーの取得。取得できないならエラーを表示して終了する。
        if (recieverName == null) {
            sendMessage(
                sender, "§cメッセージ送信先が見つかりません。"
            )
            return
        }
        val reciever = plugin.proxy.getPlayer(
            getHistory(sender.name)
        )
        if (reciever == null) {
            sendMessage(
                sender, """
     §cメッセージ送信先が見つかりません。
     §cThe destination for the message was not found.
     """.trimIndent()
            )
            return
        }

        // 送信メッセージの作成

        // 送信メッセージの作成
        val str = StringBuilder()
        for (i in 0 until args?.size!!) {
            str.append(args[i] + " ")
        }
        val message = str.toString().trim { it <= ' ' }

        // 送信

        // 送信
        sendPrivateMessage(sender, reciever, message)
    }
}