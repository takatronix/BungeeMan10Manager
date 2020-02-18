package red.man10.bungee

import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler

class TemplatePlugin : Plugin() ,Listener{
    override fun onEnable() { // Plugin startup logic

        logger.info("hello kotlin")
        //      イベント登録
        proxy.pluginManager.registerListener(this, this)
        //      コマンド登録
        proxy.pluginManager.registerCommand(this,TemplateCommand("test","red.man10.template.test"))
    }

    override fun onDisable() { // Plugin shutdown logic
    }

    //      ログインイベント
    @EventHandler fun  onLogin(e: PostLoginEvent){
        logger.info("${e.player.name} is logged in")
    }

}
