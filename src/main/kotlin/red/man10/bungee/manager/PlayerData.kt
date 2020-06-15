package red.man10.bungee.manager

import com.mojang.brigadier.Command
import com.sun.org.apache.xpath.internal.operations.Bool
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.db.MySQLManager
import java.sql.Time
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class PlayerCommand{
    lateinit var time: Time             //  イベントの発生した時刻
    lateinit var message: String        //  プレイヤーの入力メッセージ
    lateinit var type:Type              //メッセージのタイプ(チャットかコマンドか)
}

enum class PlayerStatus{
    OK,
    Muted,
    Jailed,
    Kicked,
    Banned,
}

enum class Type{
    COMMAND,
    MESSAGE
}

class PlayerData(player:ProxiedPlayer, private val plugin: Man10BungeePlugin) {
    var uuid: UUID = player.uniqueId
    var mcid: String = player.name


    init {
        load()
    }

    //   チャットとコマンドを履歴に登録する
    //   条件によって Mute/Jail/Kickを返す

    fun Add(commandOrChat:String){

        val pc =PlayerCommand()

        pc.message = commandOrChat
        pc.time = Time(Date().time)

        if (commandOrChat.indexOf("/") == 0){
            pc.type = Type.COMMAND
            commandHistory.add(pc)
        }else {
            pc.type = Type.MESSAGE
            commandHistory.add(pc)
        }

        return
    }

    fun load(){

        val mysql = MySQLManager(plugin,"BungeeManager Loading")

        val rs = mysql.query("SELECT * ")

    }

    //      ログインしてからのCommand/Chat履歴
    lateinit var commandHistory: MutableList<PlayerCommand>
}