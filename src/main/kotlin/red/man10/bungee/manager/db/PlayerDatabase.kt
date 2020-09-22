package red.man10.bungee.manager.db

import red.man10.bungee.manager.Man10BungeePlugin
import red.man10.bungee.manager.db.MySQLManager.Companion.executeQueue
import java.util.*



class PlayerDatabase(val plugin: Man10BungeePlugin) {

//    //Unique ID
//    fun addTime(punishment:Punishment,uuid: UUID,min: Int,hour: Int,day: Int): Boolean {
//
//        if (!hasPlayerData(uuid))return false
//
//        val type = when(punishment){
//            Punishment.JAIL->"jail_until"
//            Punishment.MUTE->"mute_until"
//            Punishment.FREEZE->"freeze_until"
//            Punishment.BAN->"ban_until"
//        }
//
//        executeQueue("UPDATE player_data SET " +
//                "$type = DATE_ADD($type,INTERVAL $min MINUTE )," +
//                "$type = DATE_ADD($type,INTERVAL $hour HOUR )," +
//                "$type = DATE_ADD($type,INTERVAL $day DAY )" +
//                " where uuid='$uuid';")
//
//        return true
//    }

    //Minecraft ID
    fun addTime(punishment:Punishment,mcid: String,min: Int,hour: Int,day: Int): Boolean {

        if (!hasPlayerData(mcid))return false

        val type = when(punishment){
            Punishment.JAIL->"jail_until"
            Punishment.MUTE->"mute_until"
            Punishment.FREEZE->"freeze_until"
            Punishment.BAN->"ban_until"
        }

        executeQueue("UPDATE player_data SET " +
                "$type = DATE_ADD($type,INTERVAL $min MINUTE )," +
                "$type = DATE_ADD($type,INTERVAL $hour HOUR )," +
                "$type = DATE_ADD($type,INTERVAL $day DAY )" +
                " where mcid='$mcid';")

        return true
    }

    fun addScore(mcid: String,score:Int){

        executeQueue("UPDATE player_data SET score=score+$score WHERE mcid=$mcid;")

    }
    fun takeScore(mcid: String,score:Int){

        executeQueue("UPDATE player_data SET score=score-$score WHERE mcid=$mcid;")

    }

    fun getScore(mcid: String):Int{

        val mysql = MySQLManager(plugin,"PlayerDatabase")

        val rs = mysql.query("select score from player_data where mcid='$mcid';")?:return 0

        if (!rs.next())return  0
        val score = rs.getInt("score")

        rs.close()
        mysql.close()
        return score

    }

    fun hasPlayerData(uuid: UUID):Boolean{
        val mysql = MySQLManager(plugin,"PlayerDatabase")

        val rs = mysql.query("select * from player_data where uuid='$uuid';")?:return false

        if (!rs.next())return false

        return true
    }

    fun hasPlayerData(mcid: String):Boolean{
        val mysql = MySQLManager(plugin,"PlayerDatabase")

        val rs = mysql.query("select * from player_data where mcid='$mcid';")?:return false


        val ret = rs.next()

        mysql.close()
        rs.close()

        return ret
    }


    enum class Punishment{
        JAIL,
        MUTE,
        FREEZE,
        BAN
    }
}