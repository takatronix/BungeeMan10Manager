package red.man10.bungee.manager

import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.db.MySQLManager
import java.sql.Time
import java.util.*

class History{
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

enum class Type{
    COMMAND,
    MESSAGE
}

class PlayerData(player:ProxiedPlayer, private val plugin: Man10BungeePlugin) {
    var uuid: UUID = player.uniqueId
    var mcid: String = player.name

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
    //      ミュート時間を追加
    fun addMuteTime(min:Int=30,hour:Int=0,day:Int=0):Date?{
        if(muteUntil == null){
            muteUntil = Date();         //  現在時刻を設定
        }



        //muteUntil += ...

        return muteUntil;
    }


    init {
        load()
    }

    //   チャットとコマンドを履歴に登録する
    //   条件によって Mute/Jail/Kickを返す

    fun add(commandOrChat:String){

        val pc =History()

        pc.message = commandOrChat
        pc.time = Time(Date().time)

        if (commandOrChat.indexOf("/") == 0){
            commandHistory.add(pc)
        }else {
            messageHistory.add(pc)
        }

        return
    }

    fun load(){

        val mysql = MySQLManager(plugin,"BungeeManager Loading")

        val rs = mysql.query("SELECT * ")

    }

    //      ログインしてからのCommand/Chat履歴
    lateinit var commandHistory: MutableList<History>
    lateinit var messageHistory : MutableList<History>
}