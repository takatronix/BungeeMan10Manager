package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.ConfigFile
import red.man10.bungee.manager.Man10BungeePlugin.Companion.cancelReceivingChatServer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.cancelSendingChatServer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.msg

object ChatSettingCommand : Command("chatset","bungeemanager.chatset") {
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null || sender !is ProxiedPlayer)return
        if (args.isNullOrEmpty()){

            msg(sender,"/chatset cancelsend       他鯖に送らない")
            msg(sender,"/chatset cancelreceive    他鯖のを受け取らない")
            return
        }


        when(args[0]){
            "cancelsend" ->{

                val server = sender.server.info.name

                if (cancelSendingChatServer.contains(server)){

                    cancelSendingChatServer.remove(server)

                    msg(sender,"他鯖にも送るようにしました")

                    saveSendingConfig()

                    return
                }

                cancelSendingChatServer.add(server)

                msg(sender,"他鯖に送らないようにしました")

                saveSendingConfig()

            }
            "cancelreceive" ->{

                val server = sender.server.info.name

                if (cancelReceivingChatServer.contains(server)){

                    cancelReceivingChatServer.remove(server)

                    msg(sender,"他鯖から受け取るようにしました")

                    saveReceivingConfig()

                    return
                }

                cancelReceivingChatServer.add(server)

                msg(sender,"他鯖から受けとらないようにしました")

                saveReceivingConfig()

            }
        }
    }

    private fun saveReceivingConfig(){
        val cfgFile = ConfigFile(plugin)
        val cfg = cfgFile.getConfig()!!

        cfg.set("Chat.CancelReceivingChatServer", cancelReceivingChatServer)

        cfgFile.saveConfig()

    }

    private fun saveSendingConfig(){
        val cfgFile = ConfigFile(plugin)
        val cfg = cfgFile.getConfig()!!

        cfg.set("Chat.CancelSendingChatServer", cancelSendingChatServer)

        cfgFile.saveConfig()

    }
}