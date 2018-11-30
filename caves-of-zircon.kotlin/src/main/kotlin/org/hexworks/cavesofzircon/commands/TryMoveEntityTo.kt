package org.hexworks.cavesofzircon.commands

import org.hexworks.cavesofzircon.entities.Entity
import org.hexworks.cavesofzircon.world.Context
import org.hexworks.zircon.api.data.impl.Position3D

data class TryMoveEntityTo(override val context: Context,
                           override val source: Entity,
                           val position: Position3D) : Command {

    override fun toString() = "Trying move entity to $position."
}