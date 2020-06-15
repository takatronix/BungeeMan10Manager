package red.man10.bungee.manager

import com.mojang.brigadier.Command
import com.sun.org.apache.xpath.internal.operations.Bool
import java.sql.Time
import java.util.*

class PlayerCommand{
    lateinit var time: Time             //  イベントの発生した時刻
    lateinit var message: String       //  プレイヤーの入力メッセージ
    lateinit var type:Type
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

class PlayerData {
    lateinit var uuid: UUID
    lateinit var mcid: String


    //   チャットとコマンドを履歴に登録する
    //   条件によって Mute/Jail/Kickを返す

    fun Add(commandOrChat:String,type:Type): PlayerStatus{

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

        return PlayerStatus.OK;
    }

    //      ログインしてからのCommand/Chat履歴
    lateinit var commandHistory: MutableList<PlayerCommand>
}