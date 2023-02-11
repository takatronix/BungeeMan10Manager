package red.man10.bungee.manager

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import red.man10.bungee.manager.Man10BungeePlugin.Companion.plugin
import javax.security.auth.login.LoginException


interface IDiscordEvent {
    fun onDiscordReadyEvent(event: ReadyEvent)
    fun onDiscordMessageReceivedEvent(event: MessageReceivedEvent)
}

class DiscordBot : ListenerAdapter() {

    private lateinit var jda: JDA
    lateinit var token: String

    private var guild: Guild? = null

    var guildID: Long = 0

    var chatChannelID: Long = 0
    var logChannelID: Long = 0
    var systemChannelID: Long = 0
    var notificationChannelID: Long = 0
    var adminChannelID: Long = 0
    var reportChannelID: Long = 0
    var jailChannelID: Long = 0

    private var chatChannel: TextChannel? = null
    private var systemChannel: TextChannel? = null
    private var logChannel: TextChannel? = null
    private var notificationChannel: TextChannel? = null
    private var adminChannel: TextChannel? = null
    private var reportChannel: TextChannel? = null
    private var jailChannel: TextChannel? = null

    lateinit var discordEvent: IDiscordEvent

    //      チャットチャンネル出力
    fun chat(text: String) {
        if (text.indexOf("/") == 0) return
        chatChannel?.sendMessage(text)?.queue()?:return
    }

    //      ログチャンネル出力
    fun log(text: String) {
        logChannel?.sendMessage(text)?.queue()?:return
    }

    //      システム出力
    fun system(text: String) {
        systemChannel?.sendMessage(text)?.queue()?:return
    }

    //      通知
    fun notification(text: String) {
        notificationChannel?.sendMessage(text)?.queue()?:return
    }

    //      Admin用
    fun admin(text: String) {
        adminChannel?.sendMessage(text)?.queue()?:return
    }

    //      Report
    fun report(text: String) {
        reportChannel?.sendMessage(text)?.queue()?:return
    }

    //      処罰系
    fun jail(text: String) {
        jailChannel?.sendMessage(text)?.queue()?:return
    }

    fun shutdown() {
        jda.shutdown()
        plugin.log("discord shutdown")
    }

    fun setup() {
        plugin.log("discord setup")

        if (!::token.isInitialized) {
            plugin.error("Discord token is not initialized.")
            return
        }
        try {

//            jda = JDABuilder(AccountType.BOT).setToken(token).addEventListeners(this).build()
            jda = JDABuilder.createDefault(token).build()
            jda.awaitReady()

            jda.addEventListener(this)

            guild = jda.getGuildById(this.guildID)
            chatChannel = guild?.getTextChannelById(this.chatChannelID)
            logChannel = guild?.getTextChannelById(this.logChannelID)
            systemChannel = guild?.getTextChannelById(this.systemChannelID)
            notificationChannel = guild?.getTextChannelById(this.notificationChannelID)
            adminChannel = guild?.getTextChannelById(this.adminChannelID)
            reportChannel = guild?.getTextChannelById(this.reportChannelID)
            jailChannel = guild?.getTextChannelById(this.jailChannelID)

        } catch (e: LoginException) {
            e.printStackTrace()
            plugin.error(e.localizedMessage)
            return
        }
        plugin.log("discord setup done!")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
//        println("chat event")

        val message = event.message
        val user = message.author

        if (user.isBot) return

        val channel = message.channel

        if (channel.idLong != chatChannelID) return

        val text = message.contentDisplay

        val outText = "§f[§3@Discord§f]${event.member?.nickname ?: user.name}§b:§f$text"
        Man10BungeePlugin.sendGlobalMessage(outText)
    }

    override fun onReady(event: ReadyEvent) {
        discordEvent.onDiscordReadyEvent(event)
    }
}