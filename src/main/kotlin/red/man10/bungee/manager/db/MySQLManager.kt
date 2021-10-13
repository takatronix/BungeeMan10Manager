package red.man10.bungee.manager.db

import net.md_5.bungee.api.plugin.Plugin
import red.man10.bungee.manager.ConfigFile
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Level

/**
 * Created by takatronix on 2017/03/05.
 */


class MySQLManager(private val plugin: Plugin, private val conName: String) {

    var debugMode: Boolean? = false
    private var HOST: String? = null
    private var DB: String? = null
    private var USER: String? = null
    private var PASS: String? = null
    private var PORT: String? = null
    internal var connected = false
    private var st: Statement? = null
    private var con: Connection? = null
    private var MySQL: MySQLFunc? = null


    init {
        this.connected = false
        loadConfig()

        this.connected = Connect(HOST, DB, USER, PASS, PORT)!!

        if (!this.connected) {
            plugin.logger.info("Unable to establish a MySQL connection.")
        }
    }

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////
    fun loadConfig() {
        //   plugin.getLogger().info("MYSQL Config loading");
        val config = ConfigFile(plugin).getConfig()?:return
        HOST = config.getString("MySQL.Host")
        USER = config.getString("MySQL.User")
        PASS = config.getString("MySQL.Password")
        PORT = config.getString("MySQL.Port")
        DB = config.getString("MySQL.Database")
        plugin.logger.info("Config loaded  $HOST / $USER")

    }

    fun commit() {
        try {
            this.con!!.commit()
        } catch (e: Exception) {

        }

    }

    ////////////////////////////////
    //       接続
    ////////////////////////////////
    fun Connect(host: String?, db: String?, user: String?, pass: String?, port: String?): Boolean? {
        this.HOST = host
        this.DB = db
        this.USER = user
        this.PASS = pass
        this.MySQL = MySQLFunc(host!!, db!!, user!!, pass!!, port!!)
        this.con = this.MySQL?.open()
        if (this.con == null) {
            plugin.logger.info("failed to open MYSQL")
            return false
        }

        try {
            this.st = this.con!!.createStatement()
            this.connected = true
            this.plugin.logger.info("[" + this.conName + "] Connected to the database.")
        } catch (e: Exception) {
            this.connected = false
            this.plugin.logger.info("[" + this.conName + "] Could not connect to the database ${e.message}")
        }

        this.MySQL!!.close(this.con)
        return java.lang.Boolean.valueOf(this.connected)
    }

    ////////////////////////////////
    //     行数を数える
    ////////////////////////////////
    fun countRows(table: String): Int {
        var count = 0
        val set = this.query(String.format("SELECT * FROM %s", *arrayOf<Any>(table)))

        try {
            while (set!!.next()) {
                ++count
            }
        } catch (var5: SQLException) {
            plugin.logger.log(Level.SEVERE, "Could not select all rows from table: " + table + ", error: " + var5.errorCode)
        }

        return count
    }

    ////////////////////////////////
    //     レコード数
    ////////////////////////////////
    fun count(table: String): Int {
        var ret = 0
        val set = this.query(String.format("SELECT count(*) from %s", table))

        try {
            ret = set!!.getInt("count(*)")

        } catch (var5: SQLException) {
            plugin.logger.log(Level.SEVERE, "Could not select all rows from table: " + table + ", error: " + var5.errorCode)
            return -1
        }

        return ret
    }

    ////////////////////////////////
    //      実行
    ////////////////////////////////
    fun execute(query: String): Boolean {

        this.MySQL = MySQLFunc(this.HOST!!, this.DB!!, this.USER!!, this.PASS!!, this.PORT!!)
        this.con = this.MySQL!!.open()
        if (this.con == null) {
            plugin.logger.info("failed to open MYSQL")
            return false
        }
        var ret = true
        if (debugMode!!) {
            plugin.logger.info("query:$query")
        }

        try {
            this.st = this.con!!.createStatement()
            this.st!!.execute(query)
        } catch (var3: SQLException) {
            this.plugin.logger.info("[" + this.conName + "] Error executing statement: " + var3.errorCode + ":" + var3.message)
            this.plugin.logger.info(query)
            ret = false

        }

        this.close()
        return ret
    }

    ////////////////////////////////
    //      クエリ
    ////////////////////////////////
    fun query(query: String): ResultSet? {

        this.MySQL = MySQLFunc(this.HOST!!, this.DB!!, this.USER!!, this.PASS!!, this.PORT!!)
        this.con = this.MySQL!!.open()
        var rs: ResultSet? = null
        if (this.con == null) {
            plugin.logger.info("failed to open MYSQL")
            return rs
        }

        if (debugMode!!) {
            plugin.logger.info("[DEBUG] query:$query")
        }

        try {
            this.st = this.con!!.createStatement()
            rs = this.st!!.executeQuery(query)
        } catch (var4: SQLException) {
            this.plugin.logger.info("[" + this.conName + "] Error executing query: " + var4.errorCode)
            this.plugin.logger.info(query)
        }

        //        this.close();

        return rs
    }

    fun close() {

        try {
            this.st?.close()
            this.con?.close()
            this.MySQL?.close(this.con)

        } catch (var4: SQLException) {
        }

    }

    companion object{

        private val blockingQueue = LinkedBlockingQueue<String>()

        /////////////////////////////////////
        //   BlockingQueueのセットアップ
        /////////////////////////////////////
        fun setupBlockingQueue(plugin: Plugin,conName: String){

            Thread {
                val sql = MySQLManager(plugin, conName)
                try {
                    while (true) {
                        val take = blockingQueue.take()
                        val ret = sql.execute(take)
                        if (!ret){ plugin.proxy.logger.warning("クエリの失敗 Query:${take}") }
                    }
                } catch (e: InterruptedException) {

                }
            }.start()
        }

        //キューにクエリを入れる
        fun executeQueue(query:String){
            blockingQueue.add(query)
        }

        fun escapeSQL(query:String):String{
            return query.replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\\x1A", "\\Z")
                .replace("\\x00", "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
        }


    }
}