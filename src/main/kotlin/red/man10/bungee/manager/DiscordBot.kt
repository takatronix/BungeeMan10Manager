package red.man10.bungee.manager

import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import javax.security.auth.login.LoginException


class DiscordBot(plugin: Man10BungeePlugin) : ListenerAdapter() {
    private var plugin = plugin

    lateinit var jda: JDA
    var token:String? = null

    var guild:Guild? = null;

    var guildID:Long = 0
    var chatChannelID:Long = 0
    var logChannelID:Long = 0
    var systemChannelID:Long = 0
    var notificationChannelID:Long = 0
    var adminChannelID:Long = 0

    var adminChannel:GuildChannel? = null
    var chatChannel:TextChannel? = null
    var systemChannel:TextChannel? = null
    var logChannel:TextChannel? = null
    var notificationChannel:TextChannel? = null

    fun chat(text:String){
        chatChannel?.sendMessage(text)
        plugin.log("[discord.chat]$text")
    }
    fun log(text:String){
        logChannel?.sendMessage(text)
        plugin.log("[discord.log]$text")
    }
    fun system(text:String){
        systemChannel?.sendMessage(text)
        plugin.log("[discord.system]$text")
    }
    fun notification(text:String){
        notificationChannel?.sendMessage(text)
        plugin.log("[discord.notification]$text")
    }
    fun admin(text:String){
        //adminChannel?.sendMessage(text)
        plugin.log("[discord.notification]$text")
    }
    init{

    }
    fun shutdown(){
        jda.shutdown()
        plugin.log("discord shutdown")
    }

    fun setup(){
        plugin.log("discord setup")

        if(token == null){
            plugin.error("Discord token is not initialized.")
            return
        }
        try {

       //     jda = JDABuilder(AccountType.BOT).setToken(token).addEventListeners(this).build()

            var jda = JDABuilder(token).addEventListeners(this).build()
            jda.awaitReady()
      //      var builder = DefaultShardManagerBuilder().setToken(token!!)

            guild = jda.getGuildById(this.guildID);
            /*
            chatChannel = jda.getTextChannelById(this.chatChannelID)
            logChannel = jda.getTextChannelById(this.logChannelID)
            systemChannel = jda.getTextChannelById(this.systemChannelID)
            notificationChannel = jda.getTextChannelById(this.notificationChannelID)
            adminChannel = jda.getTextChannelById(this.adminChannelID)
*/
            chatChannel = guild?.getTextChannelById(this.chatChannelID)
            logChannel = guild?.getTextChannelById(this.logChannelID)
            systemChannel = guild?.getTextChannelById(this.systemChannelID)
            notificationChannel = guild?.getTextChannelById(this.notificationChannelID)
            adminChannel = guild?.getTextChannelById(this.adminChannelID)

        } catch (e: LoginException) {
            e.printStackTrace()
            plugin.error(e.localizedMessage)
            return
        }

        plugin.log("discord setup done!")
    }
    override fun onReady(event: ReadyEvent) {
        plugin.log("Discord bot ready")
        this.chat("chat channel connected")
        this.log("log channel connected")
        this.system("system channel connected")
        this.notification("notification channel connected")
        this.chat("chat channel connected")
        /*
        plugin.textChannel = plugin.jda.getTextChannelById(plugin.channelID)
        if (plugin.textChannel != null) {
            plugin.textChannel!!.sendMessage(":ballot_box_with_check: サーバーが起動しました").queue()
        }*/

    }
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message
        plugin.log("${msg.contentRaw}")
        if (msg.contentRaw.equals("!ping")) {
            val channel = event.channel
            val time = System.currentTimeMillis()
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                    .queue { response: Message -> response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue() }
        }
    }
}