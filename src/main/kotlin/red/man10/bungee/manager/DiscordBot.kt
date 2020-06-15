package red.man10.bungee.manager
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.security.auth.login.LoginException
import net.dv8tion.jda.api.entities.TextChannel

class DiscordBot(plugin:Plugin) : ListenerAdapter() {
    private var plugin:Plugin = plugin

    lateinit var jda: JDA
    var token:String? = null

    var chatChannelID:Long = 0
    var logChannelID:Long = 0
    var systemChannelID:Long = 0
    var notificationChannelID:Long = 0

    var chatChannel:TextChannel? = null
    var systemChannel:TextChannel? = null
    var logChannel:TextChannel? = null
    var notificationChannel:TextChannel? = null

    fun chat(text:String){
        chatChannel?.sendMessage(text)
    }
    fun log(text:String){
        logChannel?.sendMessage(text)
    }
    fun system(text:String){
        systemChannel?.sendMessage(text)
    }
    fun notification(text:String){
        notificationChannel?.sendMessage(text)
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
            jda = JDABuilder(AccountType.BOT).setToken(token).build()
            jda.addEventListener(this)
            plugin.log("token:${token}")
            plugin.log("chatChannelID:${chatChannelID}")
            plugin.log("logChannelID:${logChannelID}")
            plugin.log("systemChannelID:${systemChannelID}")
            plugin.log("notificationChannelID:${notificationChannelID}")
            chatChannel = jda.getTextChannelById(this.chatChannelID)
            logChannel = jda.getTextChannelById(this.logChannelID)
            systemChannel = jda.getTextChannelById(this.systemChannelID)
            notificationChannel = jda.getTextChannelById(this.notificationChannelID)
        } catch (e: LoginException) {
            e.printStackTrace()
            plugin.error(e.localizedMessage)
            return
        }

        plugin.log("discord setup done!")
    }
    override fun onReady(event: ReadyEvent) {
        plugin.log("Discord bot ready")
        /*
        plugin.textChannel = plugin.jda.getTextChannelById(plugin.channelID)
        if (plugin.textChannel != null) {
            plugin.textChannel!!.sendMessage(":ballot_box_with_check: サーバーが起動しました").queue()
        }*/

    }
}