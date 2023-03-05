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


class Man10BungeePlugin : Plugin(), Listener, IDiscordEvent {

    companion object {
        private const val prefix = "§f[§dMan§f10§aBot§f]"

        lateinit var plugin: Man10BungeePlugin

        var playerDataDic = ConcurrentHashMap<UUID, PlayerData>()

        val lastConnectTime = HashMap<UUID, Date>()

        var cancelSendingChatServer = mutableListOf<String>()
        var cancelReceivingChatServer = mutableListOf<String>()

        var banIpList = mutableListOf<String>()

        var msbMessage = ""

        lateinit var jailServerName: String
        private val loginServerName = "login"

        //        //      オンラインのプレイヤーの情報
        var JapanizerDictionary = HashMap<String, String>()
        var enableJapanizer: Boolean = true
        val discord = DiscordBot()

        //Thread Pool
        private val es = Executors.newCachedThreadPool()

        fun msg(p: ProxiedPlayer, text: String) {
            p.sendMessage(*ComponentBuilder(text).create())
        }

        fun msg(c: CommandSender, text: String) {
            c.sendMessage(*ComponentBuilder(text).create())
        }

        fun globalMessage(text: String) {
            plugin.log("[Global]$text")

            val outText = if (text.length > 256) "※省略されました" else text

            for (player in ProxyServer.getInstance().players) {
                player.sendMessage(TextComponent(outText))
            }
        }
    }

    override fun onEnable() { // Plugin startup logic

        plugin = this

        log("started")
        loadConfig()
        proxy.pluginManager.registerListener(this, this)

        discord.system("サーバー開始しました")

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
        for (command in arrayOf("tell", "msg", "message", "m", "w", "t")) {
            proxy.pluginManager.registerCommand(this, TellCommand(this, command))
        }
        //reply commandを置き換える
        for (command in arrayOf("reply", "r")) {
            proxy.pluginManager.registerCommand(this, ReplyCommand(this, command))
        }

        banIpList = AltCheckCommand.getBanIPList()

        MySQLManager.setupBlockingQueue(this, "Man10BungeeDiscord")

        discord.chat(":ballot_box_with_check:**サーバーが起動しました**")

    }

    override fun onDisable() {
        discord.system("サーバーシャットダウンしました")
        discord.chat(":octagonal_sign:**サーバーがシャットダウンしました**")
        discord.shutdown()
    }

    //region ログ関数
    fun log(text: String) {
        logger.info("$prefix$text")
        discord.admin(text)
    }

    fun warning(text: String) {
        logger.warning("$prefix$text")
        discord.admin("[Warning]$text")
    }

    fun error(text: String) {
        logger.severe("${prefix}§c$text")
        discord.admin("[Error]$text")
    }

    fun sendToJail(player: ProxiedPlayer) {
        if (player.server.info.name == jailServerName) return
        val target = ProxyServer.getInstance().getServerInfo(jailServerName)
        player.connect(target)
    }
    //endregion

    private fun loadConfig() {
        val config = ConfigFile(this).getConfig()

        if (config == null) {
            error("コンフィグの読み込みに失敗しました")
            return
        }

        try {

            msbMessage = config.getString("msbMessage") ?: ""

            enableJapanizer = config.getBoolean("japanizer")
            jailServerName = config.getString("jail.server", "jail")

            ////////////////////////////////////////////
            //      discord bot initialization
            discord.token = config.getString("Discord.Token")
            discord.guildID = config.getLong("Discord.Guild")
            discord.chatChannelID = config.getLong("Discord.ChatChannel")
            discord.systemChannelID = config.getLong("Discord.SystemChannel")
            discord.notificationChannelID = config.getLong("Discord.NotificationChannel")
            discord.logChannelID = config.getLong("Discord.LogChannel")
            discord.adminChannelID = config.getLong("Discord.AdminChannel")
            discord.reportChannelID = config.getLong("Discord.ReportChannel")
            discord.jailChannelID = config.getLong("Discord.JailChannel")
            discord.discordEvent = this
            discord.setup()
            //////////////////////////////////////////////
            //      Server chat setting
            cancelSendingChatServer = config.getStringList("Chat.CancelSendingChatServer") ?: mutableListOf()
            cancelReceivingChatServer = config.getStringList("Chat.CancelReceivingChatServer") ?: mutableListOf()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            error(e.localizedMessage)
        }
    }


    //  Event called when a player sends a message to a server.
    //  プレイヤーがサーバーにメッセージを送信したときに呼び出されるイベント。
    @EventHandler
    fun onChat(e: ChatEvent) {

        ////////////////////////////////////////////////////
        //      プレイヤーデータがない場合処理を行わない
        val p = e.sender
        if (p !is ProxiedPlayer) return
        val data = playerDataDic[p.uniqueId]
        if (data == null) {
            e.isCancelled = true
            return
        }

        val server = p.server.info.name
        val normalMessage = e.message
        var noColorMessage = removeColorCode(e.message)?:e.message

        val isNumber = normalMessage.toIntOrNull() != null

        //////////////////////
        //認証をしていないユーザーは、認証処理にとばす
        if (!data.isAuth) {

            e.isCancelled = true

            if (!PlayerData.checkCode(p, e.message)) {
                PlayerData.showAuthenticationMsg(p)
                e.isCancelled = true
                return
            }

            msg(p, "§a認証できました！(Authentication Success!)")
            msg(p, "§a§lようこそman10サーバーへ！")
            msg(p, "§a§l5秒後にロビーにテレポートします！")

            //新規ユーザー作成処理
            es.execute {

                data.create()

                val count = PlayerData.countPlayers()

                Thread.sleep(5000)

                globalMessage(
                    "§b§l${p.name}§e§lさんがMan10サーバーに初参加しました！ " +
                            "§b§l${count}§e§l人目のプレイヤーです！"
                )

                discord.chat("**${p.name}**さんがMan10サーバーに初参加しました！ **${count}**人目のプレイヤーです！")

                if (server != "man10"){
                    p.connect(ProxyServer.getInstance().getServerInfo("man10"))
                }
            }
            return
        }


        ///////////////////////////////////////////////
        //      同じメッセージを連続して送れないように
        if (data.lastChatMessage == normalMessage && !isNumber) {
            e.isCancelled = true
            msg(p, "§c§l同じメッセージを連続して送ることはできません")
            return
        }

        ////////////////////////////////////////////////////
        //      メッセージ整形:ローマ字
        if (enableJapanizer) {
            val jmsg = Japanizer.japanize(noColorMessage, JapanizeType.GOOGLE_IME, JapanizerDictionary)
            if (jmsg != "") noColorMessage += " §6($jmsg)"
        }

        ////////////////////////////////////////////////////
        //      整形: [@man10]forest611:おはまん！
        val chatMessage = "§f[§3@${p.server.info.name}§f]${p.name}§b:§f$noColorMessage"

        //discord用メッセージ
        val discordMessage = "<${p.name}@${p.server.info.name}> ${removeColorCode(noColorMessage)}"

        ////////////////////////////////////////////////////
        //   ミュートされている場合チャット＆コマンドも禁止
        if (data.isMuted()) {
            warning("[Muted] <${e.sender}> ($chatMessage)")
            msg(p, "§eあなたはミュートされています!!")
            e.isCancelled = true
            return
        }

        ////////////////////////////////////////////////////
        //   ジェイルされている場合コマンド実行禁止
        if (data.isJailed()) {
            warning("[Jailed] <${e.sender}> ($chatMessage)")
            if (e.isProxyCommand || e.isCommand) {
                msg(p, "§eあなたは島流しにあっています!!")
                e.isCancelled = true
                return
            }

            //Jailにいなかったら飛ばす
            if (p.server.info.name != jailServerName) {
                sendToJail(p)
            }
            return
        }

        //数字だったらここで終了
        if (isNumber){
            return
        }

        //////////////////////////////////////////////////////////////
        //     同一サーバにいないプレイヤーにチャットを送る (ユーザーがJailじゃなかった場合)
        if (!(e.isCommand || e.isProxyCommand) && !data.isJailed() && !cancelSendingChatServer.contains(server)) {
            for (player in ProxyServer.getInstance().players) {
                if (player.server.info.name != server && !cancelReceivingChatServer.contains(player.server.info.name)) {
                    msg(player, chatMessage)
                }
            }
        }
        //////////////////////////////////////////////////////
        //      コマンド類、数字のみのチャットはDiscordへ通知しない
        if (e.isCommand || e.isProxyCommand) {
            log("[Command] <${e.sender}> $noColorMessage")

            if (!p.hasPermission("bungeemanager.op")) {
                if (noColorMessage.contains("/${noColorMessage}")) {
                    e.isCancelled = true
                }
            }

            data.saveCommand(e.message)
        } else {
            log(chatMessage)//ログを残す

            if (!cancelSendingChatServer.contains(server)) {
                discord.chat(discordMessage)
            }

            data.saveMessage(e.message)
        }
    }

    //  Event called as soon as a connection has a ProxiedPlayer and is ready to be connected to a server.
    //  (4)接続に ProxiedPlayer があり、サーバーに接続できる状態になるとすぐに呼び出されるイベント。
    @EventHandler
    fun onPostLogin(e: PostLoginEvent) {

        val p = e.player

        es.execute {
            val uuid = p.uniqueId

            val data = PlayerData(p)

            if (data.isMSB()) {
                p.disconnect(*ComponentBuilder(msbMessage).create())
                return@execute
            }

            //Banされてたら切断する
            if (data.isBanned() && banIpList.contains(AltCheckCommand.getAddress(p))) {
                p.disconnect(
                    *ComponentBuilder(
                        "§4§lYou are banned. : あなたはこのサーバーからBanされています\n " +
                                "§a身に覚えがない場合は、Man10公式Discordの#reportにお申し出ください。\n" +
                                "If you do not remember it, please report it to #report on the official Man10 Discord."
                    ).create()
                )
                return@execute
            }

            if (data.isJailed()) {
                es.execute {
                    Thread.sleep(5000)

                    val reason = data.getJailReason()

                    if (reason != null) {
                        msg(p, "§c§lあなたは「${reason}」により、現在島にいます！")
                        p.sendTitle(proxy.createTitle().title(*ComponentBuilder("§c§lあなたは島にいます").create()))
                    }
                }
            }

            val score = ScoreDatabase.getScore(p.uniqueId)

            val loginMessage = when {

                score >= 4000 -> "§a§l${p.name}§f§lがMan10Networkにログインしました §d§lスコア:${score}ポイント"
                score >= 2000 -> "§a${p.name}がMan10Networkにログインしました スコア:${score}ポイント"
                score >= 1000 -> "§e${p.name}がMan10Networkにログインしました §dスコア:${score}ポイント"
                score < -100 -> "§c${p.name}がMan10Networkにログインしました スコア:${score}ポイント"
                score < 1000 -> "§e${p.name}がMan10Networkにログインしました スコア:${score}ポイント"
                else -> "§e${p}がMan10Networkにログインしました スコア:${score}ポイント"

            }

            globalMessage(loginMessage)
            discord.admin("**$p is connected**")
            discord.chat("**${p.name}がログインしました**")

            playerDataDic[uuid] = data

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

        if (playerDataDic[p.uniqueId] != null) {

            val score = ScoreDatabase.getScore(p.uniqueId)

            val logoutMessage = when {
                score >= 4000 -> "§a§l${p}§f§lがMan10Networkから§d§lログアウトしました"
                score >= 2000 -> "§a${p}がMan10Networkにからログアウトしました"
                score < -100 -> "§c${p}がMan10Networkからログアウトしました"
                score < 1000 -> "§e${p}がMan10Networkからログアウトしました"
//                score>=10000 -> ""
                else -> "§e${p}がMan10Networkからログアウトしました"

            }

            globalMessage(logoutMessage)
            discord.admin("**$p is disconnected**")
            discord.chat("**${p}がログアウトしました**")

            playerDataDic.remove(p.uniqueId)
        }

    }

    //  Called when deciding to connect to a server.
    //  サーバーへの接続を決定する際に呼び出されます。
    @EventHandler
    fun onServerConnect(e: ServerConnectEvent) {

        val p = e.player
        val last = lastConnectTime[p.uniqueId]?.time ?: 0L

        if ((Date().time - last) < 5000 && !p.hasPermission("bungeemanager.jail")) {
            if (e.reason == ServerConnectEvent.Reason.JOIN_PROXY) {
                return
            }
            e.isCancelled = true
            msg(p, "§cしばらくお待ちください")
            return
        }

        val data = playerDataDic[p.uniqueId]

        if (data == null && e.reason != ServerConnectEvent.Reason.JOIN_PROXY) {
            msg(p, "§c§lあなたは初ログインの認証ができていない可能性があります")
            e.target = proxy.getServerInfo(loginServerName)
            PlayerData.showAuthenticationMsg(p)
            return
        }

        if (data != null && !data.isAuth) {
            msg(p, "§c§lあなたは初ログインの認証ができていない可能性があります")
            e.target = proxy.getServerInfo(loginServerName)
            PlayerData.showAuthenticationMsg(p)
            return
        }

        if (data != null && data.isJailed()) {
            e.target = proxy.getServerInfo(jailServerName)
        }

        if (e.reason != ServerConnectEvent.Reason.JOIN_PROXY) {
            lastConnectTime[p.uniqueId] = Date()
        }

        log("(5)ServerConnectEvent player:${p} target:${e.target} reason:${e.reason} mods:${e.player.modList}")

        ConnectionDatabase.connectServer(p, e.target.name)
    }

    //切断されたらログを残す
    @EventHandler
    fun onServerDisconnect(e: ServerDisconnectEvent) {
        log("ServerDisconnectEvent player:${e.player} target:${e.target} ${e.target.name} ${e.target}")
        ConnectionDatabase.disconnectServer(e.player, e.target.name)

    }

    private fun removeColorCode(msg: String?): String? {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', msg))
    }

    override fun onDiscordReadyEvent(event: ReadyEvent) {
        discord.admin("Discord Ready")
    }

    override fun onDiscordMessageReceivedEvent(event: MessageReceivedEvent) {

    }


}
