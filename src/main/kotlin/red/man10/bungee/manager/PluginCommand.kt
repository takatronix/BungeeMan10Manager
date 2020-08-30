package red.man10.bungee.manager

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command


public class PluginCommand(name: String, permission: String) : Command(name, permission) {


    override fun execute(commandSender: CommandSender, strings: Array<String?>?) {
        commandSender.sendMessage(*ComponentBuilder("Hello world!").color(ChatColor.GREEN).create())
    }
}
