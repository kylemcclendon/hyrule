package hookshot

import hyrule.Hyrule
import net.minecraft.server.EntityArrow
//import net.minecraft.server.v1_11_R1.EntityArrow
import org.bukkit.{Bukkit, Location, Material}
import org.bukkit.block.Block
import org.bukkit.entity.{Arrow, Player}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.{BlockIterator, Vector}

class Hookshot(instance: Hyrule) extends Listener{
  var pitch = 0.0F
  var yaw = 0.0F

  @EventHandler
  def shootHookshot(event: PlayerInteractEvent): Unit ={
    if(event.getPlayer.getWorld.getName.equals("Hyrule")){
      if(event.getAction.equals(Action.RIGHT_CLICK_AIR) || event.getAction.equals(Action.RIGHT_CLICK_BLOCK)){
        if((event.getItem != null) && (event.getItem.getItemMeta != null) && (event.getItem.getItemMeta.getDisplayName != null) && (event.getItem.getItemMeta.getDisplayName.equals("Longshot") || event.getItem.getItemMeta.getDisplayName.equals("Hookshot"))){
          event.setCancelled(true)
        }
      }
      else if((event.getAction.equals(Action.LEFT_CLICK_AIR) || event.getAction.equals(Action.LEFT_CLICK_BLOCK)) && (event.getItem != null) && (event.getItem.getItemMeta != null) && (event.getItem.getItemMeta.getDisplayName != null) && (event.getItem.getItemMeta.getDisplayName.equals("Longshot") || event.getItem.getItemMeta.getDisplayName.equals("Hookshot"))){
        val player = event.getPlayer

        val block = getTarget(player, Integer.valueOf(50))
        val bloc = block.getLocation
        val ploc = player.getLocation()
        this.pitch = ploc.getPitch
        this.yaw = ploc.getYaw

        ploc.setY(ploc.getY + 1.62D)
        bloc.setX(bloc.getX + 0.5D)
        bloc.setY(bloc.getY + 0.5D)
        bloc.setZ(bloc.getZ + 0.5D)

        val dx = bloc.getX - ploc.getX
        val dy = bloc.getY - ploc.getY
        val dz = bloc.getZ - ploc.getZ

        val distance = Math.sqrt(dx*dx + dy*dy + dz*dz)

        val vector = new Vector(dx / distance * 4.0D, dy / distance * 4.0D, dz / distance * 4.0D)

        val arrow = player.getWorld.spawnArrow(ploc, vector, 4.0F, 0.0F)
        arrow.setShooter(player)
        event.setCancelled(true)
      }
    }
  }

  def getTarget(player: Player, range: Integer): Block = {
    val iterator = new BlockIterator(player, range.intValue())
    var lastBlock = iterator.next()
    while(iterator.hasNext){
      lastBlock = iterator.next()
      if(!lastBlock.getType.equals(Material.AIR)){
        return lastBlock
      }
    }
    lastBlock
  }

  @EventHandler
  @SuppressWarnings(Array("deprecation"))
  def stick(event: ArrowHitBlockEvent): Unit = {
    val material = event.getBlock.getType
    if(!event.getArrow.getShooter.isInstanceOf[Player]){
      return
    }

    val player = event.getArrow.getShooter.asInstanceOf[Player]

    if(event.getHookType.equals("Hookshot") && getDistance(event.getBlock.getLocation, player.getEyeLocation) > 12.0D){
      return
    }

    if(event.getHookType.equals("Longshot") && getDistance(event.getBlock.getLocation, player.getEyeLocation) > 24.0D){
      return
    }

    if((material.equals(Material.SMOOTH_BRICK) && (event.getBlock.getData == 3)) || material.equals(Material.HAY_BLOCK) || material.equals(Material.CHEST) || material.equals(Material.TRAPPED_CHEST) || (material.equals(Material.LOG_2) && (event.getBlock.getData == 1)) || (material.equals(Material.STAINED_CLAY) && (event.getBlock.getData == 14)) || (material.equals(Material.STAINED_CLAY) && (event.getBlock.getData == 11)) || material.equals(Material.HOPPER)){
      val aloc = event.getArrow.getLocation()
      aloc.setYaw(this.yaw)
      aloc.setPitch(this.pitch)
      player.teleport(aloc)
      event.getArrow.remove()
    }
  }

  @EventHandler
  def hooked(event: ProjectileHitEvent): Unit ={
    if(event.getEntity.getShooter.isInstanceOf[Player] && event.getEntity.getWorld.getName.equalsIgnoreCase("Hyrule")) {
      val player = event.getEntity.getShooter.asInstanceOf[Player]
      val itemStack = player.getInventory.getItemInMainHand
      if(itemStack == null || itemStack.getItemMeta == null || itemStack.getItemMeta.getDisplayName == null){
        return
      }

      if((itemStack.getItemMeta.getDisplayName.equalsIgnoreCase("hookshot") || itemStack.getItemMeta.getDisplayName.equalsIgnoreCase("longshot")) && event.getEntity.isInstanceOf[Arrow]){
        Bukkit.getScheduler.scheduleSyncDelayedTask(this.instance, new Runnable(){
          override def run(): Unit = {
            try{
              val entityArrow = event.getEntity.asInstanceOf[EntityArrow]

//              val entityArrow = event.getEntity.asInstanceOf[org.bukkit.craftbukkit.v1_11_R1.entity.CraftArrow].getHandle

              val fieldX= classOf[EntityArrow].getDeclaredField("h")
              val fieldY= classOf[EntityArrow].getDeclaredField("at")
              val fieldZ= classOf[EntityArrow].getDeclaredField("au")

              fieldX.setAccessible(true)
              fieldY.setAccessible(true)
              fieldZ.setAccessible(true)

              val x = fieldX.getInt(entityArrow)
              val y = fieldY.getInt(entityArrow)
              val z = fieldZ.getInt(entityArrow)

              if(Hookshot.this.isValidBlock(y)){
                val block = event.getEntity.getWorld.getBlockAt(x,y,z)
                Bukkit.getServer.getPluginManager.callEvent(new ArrowHitBlockEvent(event.getEntity.asInstanceOf[Arrow], block, itemStack.getItemMeta.getDisplayName))
              }
            }
            catch{
              case nsfe: NoSuchFieldException =>
                nsfe.printStackTrace()
              case se: SecurityException =>
                se.printStackTrace()
              case iae: IllegalArgumentException =>
                iae.printStackTrace()
              case iAe: IllegalAccessException =>
                iAe.printStackTrace()
            }
          }
        })
        event.getEntity.remove()
      }
    }
  }

  def isValidBlock(y: Int): Boolean = {
    y != -1
  }

  def getDistance(blockLoc: Location, playerLoc: Location): Float = {
    val x2 = Math.pow(blockLoc.getX - playerLoc.getX, 2.0D).toFloat
    val y2 = Math.pow(blockLoc.getY - playerLoc.getY, 2.0D).toFloat
    val z2 = Math.pow(blockLoc.getZ - playerLoc.getZ, 2.0D).toFloat

    Math.sqrt(x2 + y2 + z2).toFloat
  }
}
