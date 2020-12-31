package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin

object JailCommand : Command("mjail","bungeemanager.jail"){

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender==null)return
        if (args==null)return

        plugin.log("jail command")
    }

}