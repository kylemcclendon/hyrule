package hyrule

import hookshot.Hookshot
import org.bukkit.plugin.java.JavaPlugin
import quest.Quests

class Hyrule extends JavaPlugin{

  override def onEnable(): Unit ={
    val pm = getServer.getPluginManager
    pm.registerEvents(new Quests(), this)
    pm.registerEvents(new Hookshot(this), this)
    getLogger.info("Hyrule plugin enabled")
  }

  override def onDisable(): Unit = {
    getLogger.info("Hyrule plugin disabled")
  }

}
