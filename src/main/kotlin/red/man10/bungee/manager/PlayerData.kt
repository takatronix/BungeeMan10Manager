package red.man10.bungee.manager

import com.mojang.brigadier.Command
import java.sql.Time
import java.util.*

class PlayerCommand{
    lateinit var time: Time             //  イベントの発生した時刻
    lateinit var message: String        //  プレイヤーの入力メッセージ
}

class PlayerData {
    lateinit var uuid: UUID
    lateinit var mcid: String


    //      ログインしてからのチャット履歴
    lateinit var chatHistory: MutableList<PlayerCommand>
    //      ログインしてからのコマンド履歴
    lateinit var commandHistory: MutableList<PlayerCommand>
}