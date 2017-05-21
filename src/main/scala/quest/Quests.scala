package quest

import scala.collection.JavaConverters._
import org.bukkit.{ChatColor, Material}
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.inventory.meta.BookMeta

class Quests extends Listener{
  private val Biggorn = Set("Cucco", "Cojiro", "Odd Mushroom", "Odd Potion", "Poacher's Saw", "Broken Sword", "Prescription", "Eyeball Frog", "World's Finest Eye Drops", "Biggoron's Sword")
//  private val Child = Set("Kokiri Emerald", "Goron Ruby", "Zora Sapphire", "Master Sword")
//  private val Adult = Set("Light Medallion", "Forest Medallion", "Fire Medallion", "Water Medallion", "Shadow Medallion", "Spirit Medallion")

  @EventHandler
  def linkDamage(event: EntityDamageEvent): Unit ={
    if(event.getEntity.isInstanceOf[Player] &&
      event.getEntity.getWorld.getName.equals("Hyrule") &&
      event.getCause == EntityDamageEvent.DamageCause.FALL){
      event.setDamage(1.0D)
    }
  }

  @EventHandler
  def RSChest(event: BlockRedstoneEvent): Unit ={
    val loc = event.getBlock.getLocation()

    if(!loc.getWorld.getName.equalsIgnoreCase("Hyrule")){
      return
    }

    var closest: Option[Player] = None
    var distance = 999.99D

    val players = loc.getWorld.getPlayers.asScala
    for(player: Player <- players){
      val playerLoc = player.getLocation

      val temp = Math.sqrt(Math.pow(playerLoc.getX - loc.getX, 2.0D) +
        Math.pow(playerLoc.getY - loc.getY, 2.0D) +
        Math.pow(playerLoc.getZ - loc.getZ, 2.0D))

      if(temp < distance){
        closest = Some(player)
        distance = temp
      }
    }

    if(closest.isEmpty){
      return
    }

    loc.setY(loc.getY - 3.0D)

    if((loc.getBlock.getType.equals(Material.CHEST) || loc.getBlock.getType.equals(Material.TRAPPED_CHEST)) &&
      event.getNewCurrent > event.getOldCurrent){
      val c: Chest = loc.getBlock.getState.asInstanceOf[Chest]
      val chestInventory = c.getBlockInventory


      if(chestInventory.contains(Material.WRITTEN_BOOK)){
        val im = chestInventory.getItem(0).getItemMeta
        val bm = im.asInstanceOf[BookMeta]
        if(bm != null){
          Resolve(bm.getPage(1), closest.get, bm)
        }
      }
    }
  }

  def Resolve(condition: String, player: Player, bookMeta: BookMeta): Unit ={
    val dirtyParts = condition.split("\n").toList
    val parts: List[String] = dirtyParts.map(p => ChatColor.stripColor(p))

    if(parts.head.equalsIgnoreCase("Quest")){
      if(parts(1).equals("Nothing")){
        val j = player.getInventory.getContents.length

        for(i <- 0 until j){
          val check = Option(player.getInventory.getContents()(i))

          if(check.isDefined){
            if(Biggorn.contains(check.get.getItemMeta.getDisplayName)){
              player.sendMessage(ChatColor.GOLD + "You have already started this quest")
              return
            }
          }
        }

        val itemStack = new ItemStack(Material.FEATHER, 1)
        val itemMeta = itemStack.getItemMeta
        itemMeta.setDisplayName("Cucco")
        itemStack.setItemMeta(itemMeta)

        player.getInventory.setItemInMainHand(itemStack)
        player.sendMessage(ChatColor.AQUA + "Please take this Cucco and make him happy. Waking a heavy sleeper will make him very happy. \n *You borrowed a Pocket Cucco! Be sure to make it very happy!")
      }
      else{
        val qTakes = new Array[Material](3)
        val qTNames = new Array[String](3)
        val qGives = new Array[Material](3)
        val damV = new Array[Int](3)
        val qGNames = new Array[String](3)

        var i = 0
        val parts1Array = parts(1).split(",")
        for(k <- 0 until parts1Array.length){
          val x = parts1Array(k)
          qTakes(i) = Material.getMaterial(x.toUpperCase)
          i += 1
        }

        i = 0
        val parts2Array = parts(2).split(",")
        for(k <- 0 until parts2Array.length){
          val x = parts2Array(k)
          qTNames(i) = x
          i += 1
        }

        i = 0
        val parts3Array = parts(3).split(",")
        for(k <- 0 until parts3Array.length){
          val x = parts3Array(k)
          qGives(i) = Material.getMaterial(x.toUpperCase)
          i += 1
        }

        i = 0
        val parts4Array = parts(4).split(",")
        for(k <- 0 until parts4Array.length){
          val x = parts4Array(k)
          damV(i) = Integer.parseInt(x)
          i += 1
        }

        i = 0
        val parts5Array = parts(5).split(",")
        for(k <- 0 until parts5Array.length){
          val x = parts5Array(k)
          qGNames(i) = x
          i += 1
        }

        var index = 0

        if(player.getInventory.getItemInMainHand.getType.equals(Material.AIR) ||
          player.getInventory.getItemInMainHand.getItemMeta.getDisplayName == null ||
          contains(qTNames, player.getInventory.getItemInMainHand.getItemMeta.getDisplayName) == -1) {
          player.sendMessage(ChatColor.GOLD + "You need: ")
          for (print <- qTNames.indices) {
            if (qTNames(print) != null) {
              player.sendMessage(ChatColor.GOLD + qTNames(print))
            }
          }
          return
        }

        index = contains(qTNames, player.getInventory.getItemInMainHand.getItemMeta.getDisplayName)
        if(player.getInventory.getItemInMainHand.getItemMeta.getDisplayName != null){
          if(player.getInventory.getItemInMainHand.getItemMeta.getDisplayName.equals(qTNames(index))){
            val give = new ItemStack(qGives(index), 1, damV(index).toShort)
            val itemMeta = give.getItemMeta
            itemMeta.setDisplayName(qGNames(index))
            give.setItemMeta(itemMeta)

            if(qGNames(index).equals("Biggoron's Sword")){
              give.addEnchantment(Enchantment.DAMAGE_ALL, 5)
              give.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5)
            }

            player.sendMessage(ChatColor.AQUA + bookMeta.getPage(index + 2))
            player.getInventory.setItemInMainHand(give)
          }
        }
      }
    }
    else if(parts(0).equals("Plot")){
      val mat = Material.getMaterial(parts(1).toUpperCase)
      val playerInventory = player.getInventory

      val index = getItem(playerInventory, mat, parts(2))

      if(index > -1){
        player.sendMessage(ChatColor.RED + "You already have this item")
        return
      }

      val adultItem = new ItemStack(mat, 1, parts(3).toShort)
      val adultItemMeta = adultItem.getItemMeta
      adultItemMeta.setDisplayName(parts(2))
      adultItem.setItemMeta(adultItemMeta)
      player.getInventory.addItem(adultItem)
      player.sendMessage(ChatColor.AQUA + bookMeta.getPage(2))
    }
    else if(parts(0).equals("Replace")){
      var index = -1

      if(parts(1).equals("Nothing")){
        if(getItem(player.getInventory, Material.getMaterial(parts(3)), parts(4)) == -1){
          index = getEmpty(player.getInventory)
        }
        else{
          player.sendMessage(ChatColor.RED + "You already have this item")
        }
      }
      else{
        index = getItem(player.getInventory, Material.getMaterial(parts(1).toUpperCase), parts(2))
      }

      if(index > -1){
        val newItem = new ItemStack(Material.getMaterial(parts(3).toUpperCase()), 1)
        val itemMeta = newItem.getItemMeta
        itemMeta.setDisplayName(parts(4))
        newItem.setItemMeta(itemMeta)

        val enchants = parts(5).split(",")
        val levels = parts(6).split(",")

        if(!enchants(0).equals("None")){
          for(i <- 0 until enchants.length){
            val enchantment = Enchantment.getByName(enchants(i).toUpperCase)
            if(enchantment != null){
              newItem.addEnchantment(enchantment, Integer.parseInt(levels(i)))
            }
          }
        }

        player.getInventory.setItem(index, newItem)
        player.sendMessage(ChatColor.AQUA + bookMeta.getPage(2))
      }
      else{
        player.sendMessage(ChatColor.RED + "You need the: " + parts(2))
      }
    }
  }

  def contains(array: Array[String], `type`: String): Int = {
    for(i <- array.indices){
      if(array(i) == null){
        return -1
      }
      if(array(i).equals(`type`)){
        return i
      }
    }
    -1
  }

  def getItem(inventory: Inventory, material: Material, name: String): Int ={
    for(i <- 0 until 36){
      val item = inventory.getItem(i)
      if(item != null){
        if(item.getType.equals(material) &&
          item.getItemMeta != null &&
          item.getItemMeta.getDisplayName != null){
          if(item.getItemMeta.getDisplayName.equals(name)){
            return i
          }
        }
      }
    }
    -1
  }

  def getEmpty(inventory: Inventory): Int = {
    for(i <- 0 until 36){
      if(inventory.getItem(i) == null){
        return i
      }
    }
    -1
  }

  @EventHandler
  def antiArmorStandTheft(event: PlayerInteractAtEntityEvent): Unit ={
    if(event.getRightClicked.getType.equals(EntityType.ARMOR_STAND)){
      if(event.getPlayer.hasPermission("permissions.restrict.nointeract")){
        event.setCancelled(true)
      }
    }
  }
}
