package red.man10.bungee.manager

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
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
import red.man10.bungee.manager.command.*
import red.man10.bungee.manager.db.ConnectionDatabase
import red.man10.bungee.manager.db.MySQLManager
import red.man10.bungee.manager.db.ScoreDatabase
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors


class Man10BungeePlugin : Plugin() ,Listener,IDiscordEvent{

    companion object {
        private const val prefix = "§f[§dMan§f10§aBot§f]"

        lateinit var plugin: Man10BungeePlugin

        var playerDataDic = ConcurrentHashMap<UUID,PlayerData>()

        val lastConnectTime = HashMap<UUID,Date>()

        var cancelSendingChatServer = mutableListOf<String>()
        var cancelReceivingChatServer = mutableListOf<String>()

        var banIpList = mutableListOf<String>()

        var msbMessage = ""

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
        proxy.pluginManager.registerCommand(this, MSBCommand)
        proxy.pluginManager.registerCommand(this, FreezeCommand)
        proxy.pluginManager.registerCommand(this, ReportCommand)
        proxy.pluginManager.registerCommand(this, ChatSettingCommand)
        proxy.pluginManager.registerCommand(this, WarnCommand)
        proxy.pluginManager.registerCommand(this, AltCheckCommand)

        //tell commandを置き換える
        for (command in arrayOf(
            "tell", "msg", "message", "m", "w", "t")) {
            proxy.pluginManager.registerCommand(this, TellCommand(this, command))
        }
        //reply commandを置き換える
        for (command in arrayOf("reply", "r")) {
            proxy.pluginManager.registerCommand(this, ReplyCommand(this, command))
        }

        banIpList = AltCheckCommand.getBanIPList()

        MySQLManager.setupBlockingQueue(this,"Man10BungeeDiscord")

        discord.chat(":ballot_box_with_check:**サーバーが起動しました**")

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

            msbMessage = config?.getString("msbMessage")?:""

            this.enableJapanizer = config?.getBoolean("japanizer")
            this.jailServerName = config?.getString("jail.server","jail")

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
            discord.jailChannelID = config.getLong("Discord.JailChannel")
            discord.plugin = this
            discord.discordEvent = this
            discord.setup()
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

            if (data.isMSB()){
                p.disconnect(*ComponentBuilder(msbMessage).create())
                return@execute
            }

            //Banされてたら切断する
            if (data.isBanned()){
                p.disconnect(*ComponentBuilder("§4§lYou are banned. : あなたはこのサーバーからBanされています\n " +
                        "§a身に覚えがない場合は、Man10公式Discordの#reportにお申し出ください。\n" +
                        "If you do not remember it, please report it to #report on the official Man10 Discord.").create())
                return@execute
            }

            if (banIpList.contains(AltCheckCommand.getAddress(p))){
                p.disconnect(*ComponentBuilder("§4§lYou are banned. : あなたはこのサーバーからBanされています\n " +
                        "§a身に覚えがない場合は、Man10公式Discordの#reportにお申し出ください。\n" +
                        "If you do not remember it, please report it to #report on the official Man10 Discord.").create())
                return@execute
            }

            if (data.isJailed()){
                es.execute {
                    Thread.sleep(5000)

                    val reason = data.getJailReason()

                    if (reason != null){
                        sendMessage(p,"§c§lあなたは「${reason}」により、現在Jail(刑務所)にいます！")
                        p.sendTitle(proxy.createTitle().title(*ComponentBuilder("§c§lあなたは刑務所にいます").create()))
                    }
                }
            }

            val score = ScoreDatabase.getScore(p.uniqueId)

            val loginMessage = when{
                score>=5000 -> {
                    //1 , 1+(L/3) , 1+(L%3)+2*(L/3)
                    val name = StringBuilder()

                    val l = p.name.length

                    name.append(p.name)
                    name.insert(0,"§d§l§o")
                    name.insert(1+(l/3),"§f§l§o")
                    name.insert(2+(l%3)+2*(l/3),"§a§l§o")

                    "${name}§7§lが§d§l§oMan10§f§l§oNet§a§l§owork§7§lにログインしました §d§lスコア:${score}ポイント"
                }
                score>=4000 -> "§a§l${p.name}§f§lがMan10Networkにログインしました §d§lスコア:${score}ポイント"
                score>=2000 -> "§a${p.name}がMan10Networkにログインしました スコア:${score}ポイント"
                score>=1000 -> "§e${p.name}がMan10Networkにログインしました §dスコア:${score}ポイント"
                score<-100  -> "§c${p.name}がMan10Networkにログインしました スコア:${score}ポイント"
                score<1000  -> "§e${p.name}がMan10Networkにログインしました スコア:${score}ポイント"
//                score>=10000 -> ""
                else -> "§e${p}がMan10Networkにログインしました スコア:${score}ポイント"

            }

            sendGlobalMessage(loginMessage)
            discord.admin("**$p is connected**")
            discord.chat("**${p.name}がログインしました**")

            playerDataDic[uuid] = data

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
            sendMessage(p,"§a§lようこそman10サーバーへ！")

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

        ///////////////////////////////////////////////
        //      同じメッセージを連続して送れないように
        if (data.lastChatMessage == e.message){
            e.isCancelled = true
            sendMessage(p,"§c§l同じメッセージを連続して送ることはできません")
            return
        }


        var message = removeColorCode(e.message)



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

            val score = ScoreDatabase.getScore(p.uniqueId)

            val logoutMessage = when{
                score>=4000 -> "§a§l${p}§f§lがMan10Networkから§d§lログアウトしました"
                score>=2000 -> "§a${p}がMan10Networkにからログアウトしました"
                score<-100  -> "§c${p}がMan10Networkからログアウトしました"
                score<1000  -> "§e${p}がMan10Networkからログアウトしました"
//                score>=10000 -> ""
                else -> "§e${p}がMan10Networkからログアウトしました"

            }

            sendGlobalMessage(logoutMessage)
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
            PlayerData.showAuthenticationMsg(p)
            return
        }

        if (data!= null && !data.isAuth){
            sendMessage(p,"§c§lあなたは初ログインの認証ができていない可能性があります")
            e.target = proxy.getServerInfo(loginServerName)
            PlayerData.showAuthenticationMsg(p)
            return
        }

        if(data !=null &&data.isJailed()) { e.target = proxy.getServerInfo(jailServerName) }

        if (e.reason != ServerConnectEvent.Reason.JOIN_PROXY){lastConnectTime[p.uniqueId] = Date()}

        log("(5)ServerConnectEvent player:${p} target:${e.target} reason:${e.reason} mods:${e.player.modList}")

        ConnectionDatabase.connectServer(p,e.target.name)
    }

    @EventHandler
    fun onServerDisconnect(e: ServerDisconnectEvent) {
        log("ServerDisconnectEvent player:${e.player} target:${e.target} ${e.target.name} ${e.target}")
        ConnectionDatabase.disconnectServer(e.player,e.target.name)

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
//
//
//    private fun levenshtein(str1:String,str2:String):Double{
//
//        var point = 0
//
//        val loop = str1.length.coerceAtMost(str2.length)
//
//        val str1Length = str1.length
//        val str2Length = str2.length
//
//        for (i in 0 until loop){
//
//            if (str1Length<= i && str2Length <=i)break
//
//            if (str1Length> i && str2Length > i){
//                if (str1[i] != str2[i]){
//                    point++
//                }
//                continue
//            }
//
//            if (str1Length>i && str2Length<=i){
//                point++
//                continue
//            }
//
//            if (str1Length<=i && str2Length>i){
//                point++
//                continue
//            }
//        }
//
//        val score = (loop-point).toDouble()/loop.toDouble()
//
//        proxy.logger.info("Levenshtein point:${point}")
//        proxy.logger.info("Levenshtein score:${score}")
//
//        return score
//    }

}
