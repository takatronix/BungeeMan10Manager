package red.man10.bungee.manager.command.privateMessage;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import red.man10.bungee.manager.Man10BungeePlugin;

public class ReplyCommand extends TellCommand {

    private Man10BungeePlugin plugin;

    public ReplyCommand(Man10BungeePlugin plugin, String name) {
        super(plugin, name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        String recieverName = getHistory(sender.getName());

        // 引数が無いときは、現在の会話相手を表示して終了する。
        if ( args.length == 0 ) {
            if ( recieverName != null ) {
                sendMessage(sender, "§dCurrent Conversation Partner： " + recieverName);
            } else {
                sendMessage(sender, "§c現在の会話相手はいません。\n" +
                        "§cThere is no current conversation partner.");
            }
            return;
        }

        // 送信先プレイヤーの取得。取得できないならエラーを表示して終了する。
        if ( recieverName == null ) {
            sendMessage(sender, ChatColor.RED +
                    "メッセージ送信先が見つかりません。");
            return;
        }
        ProxiedPlayer reciever = plugin.getProxy().getPlayer(
                getHistory(sender.getName()));
        if ( reciever == null ) {
            sendMessage(sender, "§cメッセージ送信先が見つかりません。\n" +
                    "§cThe destination for the message was not found.");
            return;
        }

        // 送信メッセージの作成
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            str.append(args[i] + " ");
        }
        String message = str.toString().trim();

        // 送信
        sendPrivateMessage(sender, reciever, message);
    }
}