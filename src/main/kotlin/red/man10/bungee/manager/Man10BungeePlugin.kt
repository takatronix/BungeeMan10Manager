package red.man10.bungee.manager

import com.github.ucchyocean.lc.japanize.JapanizeType
import com.github.ucchyocean.lc.japanize.Japanizer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap


class Man10BungeePlugin : Plugin() ,Listener,IDiscordEvent{

    companion object {
        private const val prefix = "§f[§dMan§f10§aBot§f]"
        lateinit var plugin: Man10BungeePlugin
    }

    //region 設定
    var jailServerName: String? = null

    //      オンラインのプレイヤーの情報
    var playerDataDic = ConcurrentHashMap<UUID,PlayerData>()
    var dic = HashMap<String?, String?> ()
    var enableJapanizer:Boolean? = false
    var discord = DiscordBot()
    var enableSendMessageToOtherServer = true
    //endregion

    override fun onEnable() { // Plugin startup logic
        log("started")
        loadConfig()
        proxy.pluginManager.registerListener(this, this)

        discord.system("サーバー開始しました")

        plugin = this

    //    MySQLManager.setupBlockingQueue(this,"Man10BungeeDiscord")

    }

    override fun onDisable() {
        discord.system("サーバーシャットダウンしました")
        discord.shutdown()
    }

    //region ログ関数
    fun log(text: String){
        logger.info("${Companion.prefix}$text")
        discord.admin("$text")
    }

    fun warning(text: String){
        logger.warning("${Companion.prefix}$text")
        discord.admin("[Warning]$text")
    }

    fun error(text: String){
        logger.severe("${Companion.prefix}§c$text")
        discord.admin("[Error]$text")
    }
    //endregion

    //region 共通関数
    public fun sendToServer(player:ProxiedPlayer,server: String){
        val target = ProxyServer.getInstance().getServerInfo(server)
        player.connect(target)
    }
    public fun sendToJail(player:ProxiedPlayer){
        val target = ProxyServer.getInstance().getServerInfo(jailServerName)
        player.connect(target)
    }
    //endregion

    private fun loadConfig(){
        var config = ConfigFile(this).getConfig()
        try {
            this.enableJapanizer = config?.getBoolean("japanizer")
            this.jailServerName = config?.getString("jail.server")
            ////////////////////////////////////////////
            //      discord bot initialization
            discord.token = config?.getString("Discord.Token")
            discord.guildID = config?.getLong("Discord.Guild")!!
            discord.chatChannelID = config.getLong("Discord.ChatChannel")
            discord.systemChannelID = config.getLong("Discord.SystemChannel")
            discord.notificationChannelID = config.getLong("Discord.NotificationChannel")
            discord.logChannelID = config.getLong("Discord.LogChannel")
            discord.adminChannelID = config.getLong("Discord.AdminChannel")
            discord.plugin = this
            discord.discordEvent = this;
            discord.setup()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            error(e.localizedMessage)
        }
    }


    //  Event called to represent a player first making their presence and username known.
    //  (3)プレイヤーの存在とユーザー名を最初に知ってもらうために呼び出されたイベント。
    @EventHandler
    fun onPreLogin(e: PreLoginEvent) {
        log("(3)PreLoginEvent connection:${e.connection} ${e.connection.name} uuid:${e.connection.uniqueId}")
    }

    //  Event called as soon as a connection has a ProxiedPlayer and is ready to be connected to a server.
    //  (4)接続に ProxiedPlayer があり、サーバーに接続できる状態になるとすぐに呼び出されるイベント。
    @EventHandler fun  onPostLogin(e: PostLoginEvent){
        log("(4) PostLogin ${e.player} locale:${e.player.locale} ${e.player.socketAddress}")


        logger.info("${e.player.name} is logged in")

        GlobalScope.launch {
            initPlayerData(e.player)


            ///////////////////////////////////////////////////
            //      ログインしたユーザーがジェイル民なら転送
            if(playerDataDic[e.player.uniqueId]?.isJailed()!!){
                sendToJail(e.player)
                warning("${e.player}はログインしたがジェイルに転送された")
            }

        }

        //      グローバル通知
        sendGlobalMessage("${e.player.name} has joined the network.");
    }

    //  Event called when a player sends a message to a server.
    //  プレイヤーがサーバーにメッセージを送信したときに呼び出されるイベント。
    @EventHandler
    fun onChat(e: ChatEvent) {

        log("chatevent")
        ////////////////////////////////////////////////////
        //      プレイヤーデータがない場合処理を行わない
        val p = e.sender
        if (p !is ProxiedPlayer)return
        val data = playerDataDic[p.uniqueId]
        if (data == null){
            e.isCancelled = true
            log("chatevent")
            return
        }

        ////////////////////////////////////////////////////
        //      メッセージ整形:ローマ字
        var message = removeColorCode(e.message)
        if(enableJapanizer!!){
            var jmsg = Japanizer.japanize(message, JapanizeType.GOOGLE_IME ,dic)
            if(jmsg != "") message += "($jmsg)"
        }

        ////////////////////////////////////////////////////
        //      整形: takatronix@lobby>ohaman(おはまん)
        var chatMessage = "${e.sender}@${p.server.info.name}>${message}"

        ////////////////////////////////////////////////////
        //      SPAM判定用に履歴保存
        playerDataDic[p.uniqueId]!!.add(message!!)

        ////////////////////////////////////////////////////
        //   ミュートされている場合チャット＆コマンドも禁止
        if(data.isMuted()){
            warning("[Muted] <${e.sender}> ($chatMessage)")
            sendMessage(data.uuid,"§eYou are muted!!")
            e.isCancelled = true;
            return
        }

        ////////////////////////////////////////////////////
        //   ジェイルされている場合コマンド実行禁止
        if (data.isJailed()){
            warning("[Jailed] <${e.sender}> ($chatMessage)")
            if(e.isProxyCommand  || e.isCommand){
                sendMessage(data.uuid,"§eYou are jailed!!")
                e.isCancelled = true;
                return
            }
            return
        }

        ////////////////////////////////////////////////////
        //   拘束中の場合コマンド実行禁止
        if (data.isFrozen()){
            warning("[Frozen] ($chatMessage)")
            if(e.isProxyCommand  || e.isCommand){
                sendMessage(data.uuid,"§eYou are frozen!!")
                e.isCancelled = true;
                return
            }
            return
        }

        //////////////////////////////////////////////////////
        //     同一サーバにいないプレイヤーにチャットを送る
        if(enableSendMessageToOtherServer){
            for (player in ProxyServer.getInstance().players) {
                if(player.server.info.name != p.server.info.name){
                    sendMessage(player.uniqueId,chatMessage)
                }
            }
        }

        //////////////////////////////////////////////////////
        //      コマンド類はDiscordへ通知しない
        if(e.isCommand || e.isProxyCommand){
            log("[Command] <${e.sender}> $message");
            //  TODO: DBにコマンド履歴を保存
        }else{
            log(chatMessage)
            discord.chat(chatMessage)
            //  TODO: DBにチャット履歴を保存
        }
    }

    //  Event called to represent an initial client connection.
    //  (1) クライアントの初期接続を表すために呼び出されるイベント。
    @EventHandler
    fun onClientConnect(e: ClientConnectEvent) {
        log("(1)ClientConnectEvent listener:${e.listener} sockAddress:${e.socketAddress} ${e.listener}")
        //discord.log("connect")
    }



    //  Called when a player has left the proxy,
    //  it is not safe to call any methods that perform an action on the passed player instance.
    //  プレイヤーがプロキシから離れたときに呼び出されますが、
    //  渡されたプレイヤーのインスタンスに対してアクションを実行するメソッドを呼び出すのは安全ではありません。
    @EventHandler
    fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
        logger.info("(x)PlayerDisconnectEvent ${e.player} ")

        var msg = "${e.player} is disconnected";
        sendGlobalMessage(msg)
        discord.admin(msg)
        discord.chat(msg)
    }

    //      Event called to represent a player first making their presence and username known.
    //      (2)プレイヤーの存在とユーザー名を最初に知ってもらうために呼び出されたイベント。
    @EventHandler
    fun onPlayerHandshake(e: PlayerHandshakeEvent) {
        log("(2)PlayerHandshakeEvent connection:${e.connection} handshake:${e.handshake}")
    }

    //  Event called when a plugin message is sent to the client or server.
    //  プラグインメッセージがクライアントまたはサーバに送信されたときに呼び出されるイベント
    @EventHandler
    fun onPluginMessage(e: PluginMessageEvent) {
 //       logger.info("PluginMessageEvent tag:${e.tag} sender:${e.sender}")
    }

    //  Called when the proxy is pinged with packet 0xFE from the server list.
    //  プロキシがサーバリストからパケット0xFEでpingされたときに呼び出される。
    @EventHandler
    fun onProxyPing(e: ProxyPingEvent) {
   //     logger.info("ProxyPingEvent connection:${e.connection} response:${e.response}")
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
        log("ServerConnectedEvent player:${e.player} server:${e.server} ")
    }

    //  Called when deciding to connect to a server.
    //  サーバーへの接続を決定する際に呼び出されます。
    @EventHandler
    fun onServerConnect(e: ServerConnectEvent) {
        log("(5)ServerConnectEvent player:${e.player} target:${e.target} reason:${e.reason}")
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

    //  Called after a ProxiedPlayer changed one or more of the following (client-side) settings:
    //  View distance Locale Displayed skin parts Chat visibility Chat colors Main hand side (left or right)
    //  ProxiedPlayer が以下の（クライアント側の）設定を変更した後に呼び出されます。
    //  表示距離 ロケール 表示されたスキンパーツ チャットの可視性 チャットの色 メインハンドサイド（左または右)
    @EventHandler
    fun onSettingsChanged(e: SettingsChangedEvent) {
        logger.info("SettingsChangedEvent ${e.player}")
    }
    //  Event called when a player uses tab completion.
    //  プレイヤーがタブ補完を使用したときに呼び出されるイベント
    @EventHandler
    fun onTabComplete(e: TabCompleteEvent) {
        //logger.info("TabCompleteEvent sender:${e.sender} receiver:${e.receiver}")
    }

    //  An event which occurs in the communication between two nodes.
    //  2つのノード間の通信で発生するイベント。
    @EventHandler
    fun onTargeted(e: TargetedEvent) {
        logger.info("TargetedEvent sender:${e.sender} receiver:${e.receiver}")
    }

    fun sendMessage(uuid: UUID ,text:String){
        ProxyServer.getInstance().getPlayer(uuid).sendMessage(TextComponent(text))
    }

    fun sendGlobalMessage(text:String){
        log("[Global]$text")
        for (player in ProxyServer.getInstance().players) {
            player.sendMessage(TextComponent(text))
        }
    }



    private fun removeColorCode(msg: String?): String? {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', msg))
    }

    private fun initPlayerData(p:ProxiedPlayer){
        playerDataDic[p.uniqueId] = PlayerData(p)
    }

    override fun onDiscordReadyEvent(event: ReadyEvent){
        discord.admin("Discord Ready")
    }

    //      Discordからのメッセージイベント -> すべてのプレイヤーに通知
    override  fun onDiscordMessageReceivedEvent(event: MessageReceivedEvent){

        val message = event.message
        val user = message.author
        if (user.isBot) return

        val channel= message.channel
        val text = "§b§l${user.name}@discord §f&l${message.contentDisplay}";
        sendGlobalMessage(text)
    }
}
