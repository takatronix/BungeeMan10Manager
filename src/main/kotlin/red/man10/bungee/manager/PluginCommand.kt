package red.man10.bungee.manager

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command


public class PluginCommand: Command {

    constructor(name:String,permission:String): super(name,permission)

    override fun execute(commandSender: CommandSender, strings: Array<String?>?) {
        commandSender.sendMessage(*ComponentBuilder("Hello world!").color(ChatColor.GREEN).create())
    }
}
