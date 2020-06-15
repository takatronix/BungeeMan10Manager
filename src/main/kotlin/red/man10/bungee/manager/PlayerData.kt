package red.man10.bungee.manager

import com.mojang.brigadier.Command
import com.sun.org.apache.xpath.internal.operations.Bool
import java.sql.Time
import java.util.*

class PlayerCommand{
    lateinit var time: Time             //  イベントの発生した時刻
    lateinit var message: String        //  プレイヤーの入力メッセージ
}

enum class PlayerStatus{
    OK,
    Muted,
    Jailed,
    Kicked,
    Banned,
}

class PlayerData {
    lateinit var uuid: UUID
    lateinit var mcid: String


    //   チャットとコマンドを履歴に登録する
    //   条件によって Mute/Jail/Kickを返す

    fun Add(commandOrChat:String): PlayerStatus{
        
        return PlayerStatus.OK;
    }

    //      ログインしてからのCommand/Chat履歴
    lateinit var commandHistory: MutableList<PlayerCommand>
}