package red.man10.bungee.manager
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.event.EventHandler
import red.man10.bungee.manager.db.MySQLManager


class Man10BungeePlugin : Plugin() ,Listener{

    companion object {
        private const val prefix = "§f[§dMan§f10§aBot§f]"
    }

    var enableJapanizer:Boolean? = false
    var discord = DiscordBot(this)

    override fun onEnable() { // Plugin startup logic
        log("started")
        loadConfig()
        proxy.pluginManager.registerListener(this, this)
        discord.system("Started.")

        MySQLManager.setupBlockingQueue(this,"Man10BungeeDiscord")

    }

    override fun onDisable() {
        discord.shutdown()
    }


    fun log(text: String){
        logger.info("${Companion.prefix}$text")
    }
    fun error(text: String){
        logger.severe("${Companion.prefix}§c$text")
    }

    private fun loadConfig(){
        var config = ConfigFile(this).getConfig()
        try {
            this.enableJapanizer = config?.getBoolean("japanizer")
            ////////////////////////////////////////////
            //      discord bot initialization
            discord.token = config?.getString("Discord.Token")
            discord.guildID = config?.getLong("Discord.Guild")!!
            discord.chatChannelID = config?.getLong("Discord.ChatChannel")
            discord.systemChannelID = config.getLong("Discord.SystemChannel")
            discord.notificationChannelID = config.getLong("Discord.NotificationChannel")
            discord.logChannelID = config.getLong("Discord.LogChannel")
            discord.adminChannelID = config.getLong("Discord.AdminChannel")
            discord.plugin = this
            discord.setup()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            error(e.localizedMessage)
        }
    }


    //  Event called to represent a player first making their presence and username known.
    //  プレイヤーの存在とユーザー名を最初に知ってもらうために呼び出されたイベント。
    @EventHandler
    fun onPreLogin(e: PreLoginEvent) {
        logger.info("PreLoginEvent connection:${e.connection}")
    }

    //  Event called as soon as a connection has a ProxiedPlayer and is ready to be connected to a server.
    //  接続に ProxiedPlayer があり、サーバーに接続できる状態になるとすぐに呼び出されるイベント。
    @EventHandler fun  onPostLogin(e: PostLoginEvent){
        logger.info("${e.player.name} is logged in")
        for (player in ProxyServer.getInstance().players) {
            player.sendMessage(TextComponent(e.getPlayer().getName().toString() + " has joined the network."))
        }
    }

    //  Event called when a player sends a message to a server.
    //  プレイヤーがサーバーにメッセージを送信したときに呼び出されるイベント。
    @EventHandler
    fun onChat(e: ChatEvent) {
        logger.info("chatEvent ${e.message} isCommand:${e.isCommand} sender:${e.sender} receiver:${e.receiver}")

        discord.chat("${e.sender}@${e.message}")
    }

    //  Event called to represent an initial client connection.
    //  クライアントの初期接続を表すために呼び出されるイベント。
    @EventHandler
    fun onClientConnect(e: ClientConnectEvent) {
        logger.info("ClientConnectEvent listener:${e.listener} sockAddress:${e.socketAddress}")

        discord.log("connect")
    }


    //  Called when a player has left the proxy,
    //  it is not safe to call any methods that perform an action on the passed player instance.
    //  プレイヤーがプロキシから離れたときに呼び出されますが、
    //  渡されたプレイヤーのインスタンスに対してアクションを実行するメソッドを呼び出すのは安全ではありません。
    @EventHandler
    fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
        logger.info("PlayerDisconnectEvent ${e.player} ")
    }

    //      Event called to represent a player first making their presence and username known.
    //       プレイヤーの存在とユーザー名を最初に知ってもらうために呼び出されたイベント。
    @EventHandler
    fun onPlayerHandshake(e: PlayerHandshakeEvent) {
        logger.info("PlayerHandshakeEvent connection:${e.connection} handshake:${e.handshake}")
    }

    //  Event called when a plugin message is sent to the client or server.
    //  プラグインメッセージがクライアントまたはサーバに送信されたときに呼び出されるイベント
    @EventHandler
    fun onPluginMessage(e: PluginMessageEvent) {
        logger.info("PluginMessageEvent tag:${e.tag} sender:${e.sender}")
    }

    //  Called when the proxy is pinged with packet 0xFE from the server list.
    //  プロキシがサーバリストからパケット0xFEでpingされたときに呼び出される。
    @EventHandler
    fun onProxyPing(e: ProxyPingEvent) {
        logger.info("ProxyPingEvent connection:${e.connection} response:${e.response}")
    }

    //  Called when somebody reloads BungeeCord
    //  誰かがBungeeCordをリロードしたときに呼び出される
    @EventHandler
    fun onProxyReload(e: ProxyReloadEvent) {
        logger.info("ProxyReloadEvent sender:${e.sender}")

    }

    //  Not to be confused with ServerConnectEvent,
    //  this event is called once a connection to a server is fully operational,
    //  and is about to hand over control of the session to the player.
    //  ServerConnectEventと混同されないように、このイベントは、サーバーへの接続が完全に動作し、
    // セッションの制御をプレイヤーに引き渡そうとしているときに呼び出されます。
    @EventHandler
    fun onServerConnected(e: ServerConnectedEvent) {
        logger.info("ServerConnectedEvent playser:${e.player} server:${e.server}")
    }

    //  Called when deciding to connect to a server.
    //  サーバーへの接続を決定する際に呼び出されます。
    @EventHandler
    fun onServerConnect(e: ServerConnectEvent) {
        logger.info("ServerConnectEvent player:${e.player} reason:${e.reason}")
    }

    @EventHandler
    fun onServerDisconnect(e: ServerDisconnectEvent) {
        logger.info("ServerDisconnectEvent player:${e.player} target:${e.target}")
    }

    //  Represents a player getting kicked from a server
    //  サーバーからキックされるプレイヤーを表します。
    @EventHandler
    fun onServerKick(e: ServerKickEvent) {
        logger.info("ServerKickEvent ${e.player}")
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
        logger.info("TabCompleteEvent sender:${e.sender} receiver:${e.receiver}")
    }

    //  An event which occurs in the communication between two nodes.
    //  2つのノード間の通信で発生するイベント。
    @EventHandler
    fun onTargeted(e: TargetedEvent) {
        logger.info("TargetedEvent sender:${e.sender} receiver:${e.receiver}")
    }




}
