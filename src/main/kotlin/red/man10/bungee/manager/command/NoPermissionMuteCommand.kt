package red.man10.bungee.manager.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.Man10BungeePlugin.Companion.sendMessage

object NoPermissionMuteCommand : Command("servermute","bungeemanager.nopemissionmute") {
    override fun execute(sender: CommandSender?, args: Array<out String>?) {

        if (sender == null)return

        if (!args.isNullOrEmpty() && args.size == 2){

            when(args[0]){
                "mute"->{
                    val player = ProxyServer.getInstance().getPlayer(args[1])
                    val senderPlayer = ProxyServer.getInstance().getPlayer(sender.name)?:return
                    if (player == null){
                        sendMessage(sender,"§4プレイヤーが存在しません")
                        return
                    }
                    if (Man10BungeePlugin.mutedPlayers[player.uniqueId]?.contains(senderPlayer.uniqueId) == true){
                        sendMessage(sender,"§4既にミュートしています §d/servermute reset ${player.name}で解除しましょう")
                        return
                    }
                    if (!Man10BungeePlugin.mutedPlayers.containsKey(player.uniqueId)){
                        Man10BungeePlugin.mutedPlayers[player.uniqueId] = arrayListOf()
                    }
                    Man10BungeePlugin.mutedPlayers[player.uniqueId]!!.add(senderPlayer.uniqueId)
                    sendMessage(sender,"§b${player.name}をミュートしました")
                    return
                }

                "reset"->{
                    val player = ProxyServer.getInstance().getPlayer(args[1])
                    val senderPlayer = ProxyServer.getInstance().getPlayer(sender.name)?:return
                    if (player == null){
                        sendMessage(sender,"§4プレイヤーが存在しません")
                        return
                    }
                    val mute = Man10BungeePlugin.mutedPlayers[player.uniqueId]
                    if (mute == null || !mute.contains(senderPlayer.uniqueId)){
                        sendMessage(sender,"§4そのプレイヤーをミュートしていません")
                        return
                    }
                    mute.remove(senderPlayer.uniqueId)
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