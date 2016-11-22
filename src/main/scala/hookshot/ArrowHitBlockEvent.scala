package hookshot

import org.bukkit.block.Block
import org.bukkit.entity.Arrow
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

object ArrowHitBlockEvent {
  val handlers: HandlerList = new HandlerList

  def getHandlerList: HandlerList = {
    handlers
  }
}

class ArrowHitBlockEvent(val arrow: Arrow, val b: Block, val hookType: String) extends BlockEvent(b) {
  def getArrow: Arrow = {
    this.arrow
  }

  def getHookType: String = {
    this.hookType
  }

  def getHandlers: HandlerList = {
    ArrowHitBlockEvent.getHandlerList
  }
}
