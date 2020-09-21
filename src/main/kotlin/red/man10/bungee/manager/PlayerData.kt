package red.man10.bungee.manager

import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.db.MySQLManager
import java.sql.Time
import java.util.*

class History{
    lateinit var time: Time             //  イベントの発生した時刻
    lateinit var message: String        //  プレイヤーの入力メッセージ
}


class PlayerData(player:ProxiedPlayer, var plugin: Man10BungeePlugin) {
    var uuid: UUID = player.uniqueId
    var mcid: String = player.name

    var freezeUntil: Date? = null      //      拘束期限
    var muteUntil: Date? = null        //      ミュート期限
    var jailUntil: Date? = null        //      ジェイル期限
    var banUntil: Date? = null         //      BAN期限

    var score:Int = 0                  //      スコア

    fun isFrozen() : Boolean{
        if(freezeUntil == null)return false

        if ((freezeUntil!!.time - Date().time)<0){
            jailUntil = null
            return false
        }

        return true
    }
    fun isMuted() : Boolean{
        if(muteUntil == null)return false

        if ((muteUntil!!.time - Date().time)<0){
            jailUntil = null
            return false
        }

        return true
    }
    fun isJailed() : Boolean{
        if(jailUntil == null)return false

        if ((jailUntil!!.time - Date().time)<0){
            jailUntil = null
            return false
        }

        return true
    }
    fun isBanned() : Boolean{
        if(banUntil == null)return false

        if ((banUntil!!.time - Date().time)<0){
            jailUntil = null
            return false
        }

        return true
    }
    //      ミュート時間を追加
    fun addMuteTime(min:Int=30,hour:Int=0,day:Int=0):Date?{
        if(muteUntil == null){
            muteUntil = Date()      //  現在時刻を設定
        }

        muteUntil = addDate(muteUntil!!,min,hour,day)

        return muteUntil
    }

    fun addFrozenTime(min:Int=30,hour:Int=0,day:Int=0):Date?{
        if(freezeUntil == null){
            freezeUntil = Date()      //  現在時刻を設定
        }

        freezeUntil = addDate(freezeUntil!!,min,hour,day)

        return freezeUntil
    }

    fun addJailTime(min:Int=30,hour:Int=0,day:Int=0){
        if (jailUntil == null){
            jailUntil = Date()
        }

        jailUntil = addDate(jailUntil!!,min,hour,day)
    }

    fun addBanTime(min:Int=30,hour:Int=0,day:Int=0){
        if (banUntil == null){
            banUntil = Date()
        }

        banUntil = addDate(banUntil!!,min,hour,day)
    }


    fun addDate(date:Date,min:Int,hour:Int,day:Int):Date{

        val calender = Calendar.getInstance()

        calender.time = date
        calender.add(Calendar.MINUTE,min)
        calender.add(Calendar.HOUR,hour)
        calender.add(Calendar.DATE,day)

        return calender.time
    }


    init {
        load()

        plugin.logger.info("Loaded ${mcid}'s player data ")
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

        val rs = mysql.query("SELECT * from player_data where '$uuid';")

        if (rs == null || !rs.next()){

            mysql.execute("INSERT INTO player_data (uuid, mcid, freeze_until, mute_until, jail_until, ban_until, score) " +
                    "VALUES ('$uuid', '$mcid', null, null, null, null, DEFAULT)")

            plugin.logger.info("create $mcid's data.")

            return
        }

        jailUntil = rs.getDate("jail_until")?:null
        banUntil = rs.getDate("ban_until")?:null
        freezeUntil = rs.getDate("freeze_until")?:null
        muteUntil = rs.getDate("mute_until")?:null

        score = rs.getInt("score")


    }

    //      ログインしてからのCommand/Chat履歴
    private val commandHistory = mutableListOf<History>()
    private val messageHistory = mutableListOf<History>()


}