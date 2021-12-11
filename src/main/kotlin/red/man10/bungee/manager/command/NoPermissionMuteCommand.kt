package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage

object NoPermissionMuteCommand : Command("servermute","bungeemanager.nopemissionmute") {
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return
        if (sender !is ProxiedPlayer)return

        if (!args.isNullOrEmpty() && args.size == 2){

            when(args[0]){
                "mute"->{
                    val player = ProxyServer.getInstance().getPlayer(args[1])

                    if (player == null){
                        sendMessage(sender,"§c存在しないプレイヤー、もしくはオフラインのプレイヤーです")
                        return
                    }

                    if (!Man10BungeePlugin.mutedPlayers.containsKey(player.uniqueId)){
                        Man10BungeePlugin.mutedPlayers[player.uniqueId] = arrayListOf()
                    }

                    if (Man10BungeePlugin.mutedPlayers[player.uniqueId]!!.contains(sender.uniqueId)){
                        sendMessage(sender,"§c既にミュートしています §d/servermute reset ${player.name}で解除しましょう")
                        return
                    }

                    Man10BungeePlugin.mutedPlayers[player.uniqueId]!!.add(sender.uniqueId)
                    sendMessage(sender,"§b${player.name}をミュートしました")
                    return
                }

                "reset"->{
                    val player = ProxyServer.getInstance().getPlayer(args[1])

                    if (player == null){
                        sendMessage(sender,"§c存在しないプレイヤー、もしくはオフラインのプレイヤーです")
                        return
                    }

                    val mute = Man10BungeePlugin.mutedPlayers[player.uniqueId]

                    if (mute == null || !mute.contains(sender.uniqueId)){
                        sendMessage(sender,"§cそのプレイヤーをミュートしていません")
                        return
                    }

                    mute.remove(sender.uniqueId)

                    Man10BungeePlugin.mutedPlayers[player.uniqueId] = mute

                    sendMessage(sender,"§b${player.name}をミュート解除しました")
                    return
                }
            }
        }else{
            sendMessage(sender,"§d/servermute mute <プレイヤー名> プレイヤーをミュートします")
            sendMessage(sender,"§d/servermute reset <プレイヤー名> プレイヤーのミュートを解除します")
            return
        }
    }
}