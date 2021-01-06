package red.man10.bungee.manager

import net.md_5.bungee.api.connection.ProxiedPlayer
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import red.man10.bungee.manager.db.MySQLManager
import java.sql.Time
import java.sql.Timestamp
import java.util.*

class History{
    lateinit var time: Time             //  イベントの発生した時刻
    lateinit var message: String        //  プレイヤーの入力メッセージ
}


class PlayerData(val player: ProxiedPlayer) {
    var uuid: UUID = player.uniqueId
    var mcid: String = player.name

    var freezeUntil: Date? = null      //      拘束期限
    var muteUntil: Date? = null        //      ミュート期限
    var jailUntil: Date? = null        //      ジェイル期限
    var banUntil: Date? = null         //      BAN期限

    private var score:Int = 0                  //      スコア

    private val connectData = HashMap<ProxiedPlayer, ConnectionData>() //接続時間

    //      ログインしてからのCommand/Chat履歴
    private val commandHistory = mutableListOf<History>()
    private val messageHistory = mutableListOf<History>()


    fun isFrozen() : Boolean{
        if(freezeUntil == null)return false

        return true
    }
    fun isMuted() : Boolean{
        if(muteUntil == null)return false

        return true
    }
    fun isJailed() : Boolean{
        if(jailUntil == null)return false

        return true
    }
    fun isBanned() : Boolean{
        if(banUntil == null)return false

        return true
    }
    //      ミュート時間を追加
    fun addMuteTime(min:Int=30,hour:Int=0,day:Int=0){

        muteUntil = addDate(muteUntil,min,hour,day)
        save()
    }

    fun addFrozenTime(min:Int=30,hour:Int=0,day:Int=0){

        freezeUntil = addDate(freezeUntil,min,hour,day)
        save()
    }

    fun addJailTime(min:Int=30,hour:Int=0,day:Int=0){

        jailUntil = addDate(jailUntil,min,hour,day)
        save()
    }

    fun addBanTime(min:Int=30,hour:Int=0,day:Int=0){

        banUntil = addDate(banUntil,min,hour,day)
        save()

    }


    fun addDate(date:Date?,min:Int,hour:Int,day:Int): Date? {

        val calender = Calendar.getInstance()

        calender.time = date?:Date()
        calender.add(Calendar.MINUTE,min)
        calender.add(Calendar.HOUR,hour)
        calender.add(Calendar.DATE,day)

        val time = calender.time

        if (time.time<Date().time){
            return null
        }

        return time
    }

    fun addScore(int: Int){
        score += int
        setScore(score)
    }

    fun setScore(int:Int){
        score = int
        save()
    }

    fun getScore():Int{
        return score
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

        val rs = mysql.query("SELECT * from player_data where uuid='$uuid';")

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

    fun save(){

        MySQLManager.executeQueue("UPDATE player_data SET " +
                "mcid='$mcid'," +
                "freeze_until='${freezeUntil?.time}'," +
                "mute_until='${muteUntil?.time}'," +
                "jail_until='${jailUntil?.time}'," +
                "ban_until='${banUntil?.time}'," +
                "score=$score " +
                "where uuid='${uuid}';")

    }

    fun saveCommand(command:String){
        MySQLManager.executeQueue("INSERT INTO command_log (uuid, mcid, command, date) VALUES ('$uuid', '$mcid', '$command', '${Date().time}');")
    }

    fun saveMessage(message:String){
        MySQLManager.executeQueue("INSERT INTO message_log (uuid, mcid, message, date) VALUES ('$uuid', '$mcid', '$message', '${Date().time}');")
    }

    fun connect(){

        val data = ConnectionData()

        data.server = player.server.info.name?:""
        data.connect = Timestamp(Date().time)

        connectData[player] = data
    }

    fun disconnect(){

        val data = connectData[player]?:return

        data.disconnect = Timestamp(Date().time)

        MySQLManager.executeQueue("INSERT INTO connection_log " +
                "(ip, uuid, server, connected_time, disconnected_time, connection_seconds, mcid) " +
                "VALUES (" +
                "'${player.socketAddress}', " +
                "'${data.server}', " +
                "'${data.connect}', " +
                "'${data.disconnect}', " +
                "'${data.getConnectionSeconds()}', " +
                "'${player.uniqueId}');")

    }


    class ConnectionData{

        var server = ""
        lateinit var connect : Timestamp
        lateinit var disconnect : Timestamp

        fun getConnectionSeconds():Int{
            return disconnect.compareTo(connect)
        }

    }


    companion object{

        private val mysql = MySQLManager(plugin,"BungeeManager Get UUID")

        @Synchronized //mcidからuuidを取得する
        fun getUUID(mcid:String):UUID?{

            var uuid = plugin.proxy.getPlayer(mcid).uniqueId

            if (uuid ==null){

                val rs = mysql.query("select uuid from player_data where mcid='$mcid';")

                if (rs ==null){
                    mysql.close()
                    return null
                }

                rs.next()
                uuid = UUID.fromString(rs.getString("uuid"))
                rs.close()
                mysql.close()
            }

            return uuid
        }

        //プレイヤー名からユーザーデータを取り出す
        fun getData(mcid:String): PlayerData? {
            return Man10BungeePlugin.playerDataDic[getUUID(mcid)?:return null]
        }

    }

}