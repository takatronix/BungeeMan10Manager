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
    TempBanned,
    Banned,
}

class PlayerData {
    lateinit var uuid: UUID
    lateinit var mcid: String

    var freezeUntil: Date? = null      //      拘束期限
    var muteUntil: Date? = null        //      ミュート期限
    var jailUntil: Date? = null        //      ジェイル期限
    var banUntil: Date? = null         //      BAN期限

    fun isFronzen() : Boolean{
        if(freezeUntil == null)
            return false
        return true
    }
    fun isMuted() : Boolean{
        if(muteUntil == null)
            return false
        return true
    }
    fun isJailed() : Boolean{
        if(jailUntil == null)
            return false
        return true
    }
    fun isBanned() : Boolean{
        if(banUntil == null)
            return false
        return true
    }



    //   チャットとコマンドを履歴に登録する
    //   条件によって Mute/Jail/Kickを返す

    fun Add(commandOrChat:String): PlayerStatus{

        return PlayerStatus.OK;
    }

    //      ログインしてからのCommand/Chat履歴
    lateinit var commandHistory: MutableList<PlayerCommand>
}