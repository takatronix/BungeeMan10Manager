package red.man10.bungee.manager

import com.google.common.io.ByteStreams
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class ConfigFile(private var plugin: Plugin) {
    private var file:File
    private lateinit var config:Configuration

    private val filePatch = "config.yml"
    init {
        //  Directory
        if(!plugin.dataFolder.exists())plugin.dataFolder.mkdir()
        this.file = File(plugin.dataFolder, filePatch)
        //  File
        if(!file.exists()){
            try{
                file.createNewFile()
                try{
                    val inputStream:InputStream = getResourceAsStream("config.yml")
                    val outputStream: FileOutputStream = FileOutputStream(file)
                    ByteStreams.copy(inputStream,outputStream)
                }catch (e: IOException){
                    e.printStackTrace()
                    plugin.logger.warning("Unable to create storage file. $filePatch")
                }
            }catch (e: IOException){
                e.printStackTrace()
                plugin.logger.info("failed to create config.yml")
            }
        }

    }

    private fun getResourceAsStream(patch: String): InputStream {
        return plugin.getResourceAsStream(patch)
    }

    fun getConfig():Configuration? {
        return try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(this.file)
            this.config
        }catch (e:IOException){
            e.printStackTrace()
            null
        }
    }

    fun saveConfig(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration::class.java).save(config, File(plugin?.dataFolder, "config.yml"))
        }catch (e:IOException){
            e.printStackTrace()
            plugin.logger.severe("Couldn't save storage file!")
        }
    }

    fun dataFolder():File{
        return plugin.dataFolder
    }

}