package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import java.util.concurrent.ConcurrentHashMap

object DiscordAuthCommand :Command("auth","bungeemanager.auth"){

    private val idMap = ConcurrentHashMap<ProxiedPlayer,Long>()

    override fun execute(sender: CommandSender?, args: Array<out String>?) {

    }

    fun checkMessage(id: Long,p:ProxiedPlayer){

        idMap[p] = id

        

    }
}