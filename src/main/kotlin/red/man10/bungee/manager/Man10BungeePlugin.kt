package red.man10.bungee.manager

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import red.man10.bungee.manager.Man10Broadcast.broadcastDelay
import red.man10.bungee.manager.Man10Broadcast.broadcastList
import red.man10.bungee.manager.Man10Broadcast.runMHK
import red.man10.bungee.manager.command.*
import red.man10.bungee.manager.db.MySQLManager
import red.man10.bungee.manager.db.ScoreDatabase
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.collections.HashMap


class Man10BungeePlugin : Plugin() ,Listener,IDiscordEvent{

    companion object {
        private const val prefix = "§f[§dMan§f10§aBot§f]"

        lateinit var plugin: Man10BungeePlugin

        var playerDataDic = ConcurrentHashMap<UUID,PlayerData>()

        val lastConnectTime = HashMap<UUID,Date>()

        var cancelSendingChatServer = mutableListOf<String>()
        var cancelReceivingChatServer = mutableListOf<String>()

        fun sendMessage(p:ProxiedPlayer,text: String){
            p.sendMessage(*ComponentBuilder(text).create())
        }

        fun sendMessage(c:CommandSender,text: String){
            c.sendMessage(*ComponentBuilder(text).create())
        }

        fun sendGlobalMessage(text:String){
            plugin.log("[Global]$text")

            val outText = if (text.length>256) "※省略されました" else text

            for (player in ProxyServer.getInstance().players) {
                player.sendMessage(TextComponent(outText))
            }
        }
    }

    //region 設定
    var jailServerName: String? = null
    var loginServerName = "login"

    // 自動メッセージ
    var blockAutoMessages: MutableList<String>? = mutableListOf("うんこ")
    val blockCommand = mutableListOf("pl","help")

    //      オンラインのプレイヤーの情報
    var dic = HashMap<String?, String?> ()
    var enableJapanizer:Boolean? = false
    var discord = DiscordBot()
    var enableSendMessageToOtherServer = true

    private val es = Executors.newCachedThreadPool()

    override fun onEnable() { // Plugin startup logic
        log("started")
        loadConfig()
        proxy.pluginManager.registerListener(this, this)

        discord.system("サーバー開始しました")

        plugin = this

        proxy.pluginManager.registerCommand(this, JailCommand)
        proxy.pluginManager.registerCommand(this, MuteCommand)
        proxy.pluginManager.registerCommand(this, BanCommand)
        proxy.pluginManager.registerCommand(this, FreezeCommand)
        proxy.pluginManager.registerCommand(this, ReportCommand)
        proxy.pluginManager.registerCommand(this, ChatSettingCommand)

        //tell commandを置き換える
        for (command in arrayOf(
            "tell", "msg", "message", "m", "w", "t")) {
            proxy.pluginManager.registerCommand(this, TellCommand(this, command))
        }
        //reply commandを置き換える
        for (command in arrayOf("reply", "r")) {
            proxy.pluginManager.registerCommand(this, ReplyCommand(this, command))
        }

        MySQLManager.setupBlockingQueue(this,"Man10BungeeDiscord")

        discord.chat(":ballot_box_with_check:**サーバーが起動しました**")

        runMHK()

    }

    override fun onDisable() {
        discord.system("サーバーシャットダウンしました")
        discord.chat(":octagonal_sign:**サーバーがシャットダウンしました**")
        discord.shutdown()
    }

    //region ログ関数
    fun log(text: String){
        logger.info("$prefix$text")
        discord.admin(text)
    }

    fun warning(text: String){
        logger.warning("$prefix$text")
        discord.admin("[Warning]$text")
    }

    fun error(text: String){
        logger.severe("${prefix}§c$text")
        discord.admin("[Error]$text")
    }

    //    public fun sendToServer(player:ProxiedPlayer,server: String){
//        val target = ProxyServer.getInstance().getServerInfo(server)
//        player.connect(target)
//    }
    fun sendToJail(player:ProxiedPlayer){
        if (player.server.info.name == jailServerName)return
        val target = ProxyServer.getInstance().getServerInfo(jailServerName)
        player.connect(target)
    }
    //endregion

    private fun loadConfig(){
        val config = ConfigFile(this).getConfig()
        try {
            this.enableJapanizer = config?.getBoolean("japanizer")
            this.jailServerName = config?.getString("jail.server","jail")

            this.blockAutoMessages = config?.getStringList("BlockAutoMessages")
            ////////////////////////////////////////////
            //      discord bot initialization
            discord.token = config?.getString("Discord.Token")
            discord.guildID = config?.getLong("Discord.Guild")!!
            discord.chatChannelID = config.getLong("Discord.ChatChannel")
            discord.systemChannelID = config.getLong("Discord.SystemChannel")
            discord.notificationChannelID = config.getLong("Discord.NotificationChannel")
            discord.logChannelID = config.getLong("Discord.LogChannel")
            discord.adminChannelID = config.getLong("Discord.AdminChannel")
            discord.reportChannelID = config.getLong("Discord.ReportChannel")
            discord.plugin = this
            discord.discordEvent = this
            discord.setup()
            /////////////////////////////////////////////
            //      Man10Broadcast initialization
            broadcastDelay = config.getInt("BroadcastDelay")
            broadcastList = config.getStringList("Broadcast")
            //////////////////////////////////////////////
            //      Server chat setting
            cancelSendingChatServer = config.getStringList("Chat.CancelSendingChatServer")?: mutableListOf()
            cancelReceivingChatServer = config.getStringList("Chat.CancelReceivingChatServer")?: mutableListOf()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            error(e.localizedMessage)
        }
    }


    //  Event called to represent a player first making their presence and username known.
    //  (3)プレイヤーの存在とユーザー名を最初に知ってもらうために呼び出されたイベント。
//    @EventHandler
//    fun onPreLogin(e: PreLoginEvent) {
////        log("(3)PreLoginEvent connection:${e.connection} ${e.connection.name} uuid:${e.connection.uniqueId}")
//    }

    //  Event called as soon as a connection has a ProxiedPlayer and is ready to be connected to a server.
    //  (4)接続に ProxiedPlayer があり、サーバーに接続できる状態になるとすぐに呼び出されるイベント。
    @EventHandler
    fun  onPostLogin(e: PostLoginEvent){
//        log("(4) PostLogin ${e.player} locale:${e.player.locale} ${e.player.socketAddress}")

        val p = e.player

        es.execute {
            val uuid = p.uniqueId

            val data  = PlayerData(p)

            //Banされてたら切断する
            if (data.isBanned()){

                p.disconnect(*ComponentBuilder("§4§lYou are banned. : あなたはこのサーバーからBanされています").create())
                return@execute
            }

            sendGlobalMessage("§e${p}がMan10Networkにログインしました スコア:${ScoreDatabase.getScore(p.uniqueId)}ポイント")
            discord.admin("**$p is connected**")
            discord.chat("**${p}がログインしました**")

            playerDataDic[uuid] = data

//            //      ログインしたユーザーがジェイル民なら転送
//            if(data.isJailed()){
//
//                Thread.sleep(5000)
//
//                proxy.scheduler.run {
//                    sendToJail(p)
//                }
//
//                warning("${p}はログインしたがジェイルに転送された")
//            }

        }
    }

    //  Event called when a player sends a message to a server.
    //  プレイヤーがサーバーにメッセージを送信したときに呼び出されるイベント。
    @EventHandler
    fun onChat(e: ChatEvent) {

        ////////////////////////////////////////////////////
        //      プレイヤーデータがない場合処理を行わない
        val p = e.sender
        if (p !is ProxiedPlayer)return
        val data = playerDataDic[p.uniqueId]
        if (data == null){
            e.isCancelled = true
            return
        }

        val server = p.server.info.name

        if (!data.isAuth){

            e.isCancelled = true

            if (!PlayerData.checkCode(p,e.message)){
                PlayerData.showAuthenticationMsg(p)
                e.isCancelled = true
                return
            }

            sendMessage(p,"§a認証できました！")
            sendMessage(p,"§aAuthentication Success!")
            sendMessage(p,"§a&lようこそman10サーバーへ！")

            es.execute {

                data.create()

                val count = PlayerData.countPlayers()

                sendGlobalMessage("§b§l${p.name}§e§lさんがMan10サーバーに初参加しました！ " +
                        "§b§l${count}§e§l人目のプレイヤーです！")

                discord.chat("**${p.name}**さんがMan10サーバーに初参加しました！ **${count}**人目のプレイヤーです！")

                p.connect(ProxyServer.getInstance().getServerInfo("man10"))

            }

            return
        }

        var message = removeColorCode(e.message)

        ////////////////////////////////////////////////////
        //      チャットアプリによる自動メッセージSPAM防止
        if (blockAutoMessages != null) {
            if (blockAutoMessages!!.contains(message)) {
                e.isCancelled = true
                sendMessage(p, "§cチャットアプリによる自動メッセージをブロックしました!!")
                return
            }
        }

        ////////////////////////////////////////////////////
        //      メッセージ整形:ローマ字
        if(enableJapanizer!!){
            val jmsg = Japanizer.japanize(message, JapanizeType.GOOGLE_IME ,dic)
            if(jmsg != "") message += " §6($jmsg)"
        }

        ////////////////////////////////////////////////////
        //      整形: takatronix@lobby>ohaman(おはまん)
        val chatMessage = "§f[§3@${p.server.info.name}§f]${p.name}§b:§f$message"

        //discord用メッセージ
        val discordMessage = "<${p.name}@${p.server.info.name}> ${removeColorCode(message)}"

        ////////////////////////////////////////////////////
        //   ミュートされている場合チャット＆コマンドも禁止
        if(data.isMuted()){
            warning("[Muted] <${e.sender}> ($chatMessage)")
            sendMessage(p,"§eあなたはミュートされています!!")
            e.isCancelled = true
            return
        }

        ////////////////////////////////////////////////////
        //   ジェイルされている場合コマンド実行禁止
        if (data.isJailed()){
            warning("[Jailed] <${e.sender}> ($chatMessage)")
            if(e.isProxyCommand  || e.isCommand){
                sendMessage(p,"§eあなたはJailされています!!")
                e.isCancelled = true
                return
            }

            if (p.server.info.name != jailServerName){
                sendToJail(p)
            }
            return
        }

        ////////////////////////////////////////////////////
        //   拘束中の場合コマンド実行禁止
        if (data.isFrozen()){
            warning("[Frozen] ($chatMessage)")
            if(e.isProxyCommand  || e.isCommand){
                sendMessage(p,"§eあなたはフリーズされています!!")
                e.isCancelled = true
                return
            }
            return
        }

        //////////////////////////////////////////////////////////////
        //     同一サーバにいないプレイヤーにチャットを送る (ユーザーがJailじゃなかった場合)
        if(enableSendMessageToOtherServer && !(e.isCommand || e.isProxyCommand) && !data.isJailed() &&
            !cancelSendingChatServer.contains(server)) {
            for (player in ProxyServer.getInstance().players) {
                if (player.server.info.name != server && !cancelReceivingChatServer.contains(player.server.info.name)) {
                    sendMessage(player, chatMessage)
                }
            }
        }
        //////////////////////////////////////////////////////
        //      コマンド類はDiscordへ通知しない
        if(e.isCommand || e.isProxyCommand){
            log("[Command] <${e.sender}> $message")

            if (!p.hasPermission("bungeemanager.op")){
                if (message!!.contains("/${message}")){
                    e.isCancelled = true
                }
            }

            data.saveCommand(e.message)
        }else{
            log(chatMessage)//ログを残す

            if (!cancelSendingChatServer.contains(server)){
                discord.chat(discordMessage)
            }

            data.saveMessage(e.message)
        }
    }


    //  Called when a player has left the proxy,
    //  it is not safe to call any methods that perform an action on the passed player instance.
    //  プレイヤーがプロキシから離れたときに呼び出されますが、
    //  渡されたプレイヤーのインスタンスに対してアクションを実行するメソッドを呼び出すのは安全ではありません。
    @EventHandler
    fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
        logger.info("(x)PlayerDisconnectEvent ${e.player} ")

        val p = e.player

        if (playerDataDic[p.uniqueId] != null){
            sendGlobalMessage("§e${p}がMan10Networkからログアウトしました")
            discord.admin("**$p is disconnected**")
            discord.chat("**${p}がログアウトしました**")

            playerDataDic.remove(p.uniqueId)
        }

    }

    //  Called when somebody reloads BungeeCord
    //  誰かがBungeeCordをリロードしたときに呼び出される
    @EventHandler
    fun onProxyReload(e: ProxyReloadEvent) {
        log("ProxyReloadEvent sender:${e.sender}")
      //  discord.admin("ProxyReloadEvent sender:${e.sender}")
    }

    //  Not to be confused with ServerConnectEvent,
    //  this event is called once a connection to a server is fully operational,
    //  and is about to hand over control of the session to the player.
    //  ServerConnectEventと混同されないように、このイベントは、サーバーへの接続が完全に動作し、
    // セッションの制御をプレイヤーに引き渡そうとしているときに呼び出されます。
    @EventHandler
    fun onServerConnected(e: ServerConnectedEvent) {

        val p = e.player
        log("ServerConnectedEvent player:${p.name} server:${e.server.info.name}")

    }

    //  Called when deciding to connect to a server.
    //  サーバーへの接続を決定する際に呼び出されます。
    @EventHandler
    fun onServerConnect(e: ServerConnectEvent) {

        val p = e.player
        val last = lastConnectTime[p.uniqueId]?.time?:0L

        if ((Date().time - last) < 5000 && !p.hasPermission("bungeemanager.jail")){
            if (e.reason == ServerConnectEvent.Reason.JOIN_PROXY){ return }
            e.isCancelled = true
            sendMessage(p,"§cしばらくお待ちください")
            return
        }

        val data = playerDataDic[p.uniqueId]

        if (data==null && e.reason != ServerConnectEvent.Reason.JOIN_PROXY){
            sendMessage(p,"§c§lあなたは初ログインの認証ができていない可能性があります")
            e.target = proxy.getServerInfo(loginServerName)
            return
        }

        if (data!= null && !data.isAuth){
            sendMessage(p,"§c§lあなたは初ログインの認証ができていない可能性があります")
            e.target = proxy.getServerInfo(loginServerName)
            return
        }

        if(data !=null &&data.isJailed()) { e.target = proxy.getServerInfo(jailServerName) }

        if (e.reason != ServerConnectEvent.Reason.JOIN_PROXY){lastConnectTime[p.uniqueId] = Date()}

        log("(5)ServerConnectEvent player:${p} target:${e.target} reason:${e.reason} mods:${e.player.modList}")
    }

    @EventHandler
    fun onServerDisconnect(e: ServerDisconnectEvent) {
        log("ServerDisconnectEvent player:${e.player} target:${e.target} ${e.target.name} ${e.target}")
    }

    //  Represents a player getting kicked from a server
    //  サーバーからキックされるプレイヤーを表します。
    @EventHandler
    fun onServerKick(e: ServerKickEvent) {
        log("ServerKickEvent ${e.player}")
    }
    //  Called when a player has changed servers.
    //  プレイヤーがサーバーを変更したときに呼び出されます。
    @EventHandler
    fun onServerSwitch(e: ServerSwitchEvent) {
        logger.info("ServerSwitchEvent player:${e.player} from:${e.from}")
    }

    //  An event which occurs in the communication between two nodes.
    //  2つのノード間の通信で発生するイベント。
    @EventHandler
    fun onTargeted(e: TargetedEvent) {
        logger.info("TargetedEvent sender:${e.sender} receiver:${e.receiver}")
    }

    private fun removeColorCode(msg: String?): String? {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', msg))
    }

    override fun onDiscordReadyEvent(event: ReadyEvent){
        discord.admin("Discord Ready")
    }

    override fun onDiscordMessageReceivedEvent(event: MessageReceivedEvent) {

    }

}
