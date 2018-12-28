package org.hexworks.cavesofzircon.commands

import org.hexworks.amethyst.api.EntityType
import org.hexworks.cavesofzircon.attributes.types.Player
import org.hexworks.cavesofzircon.extensions.GameCommand
import org.hexworks.cavesofzircon.extensions.GameEntity
import org.hexworks.cavesofzircon.world.GameContext
import org.hexworks.zircon.api.data.impl.Position3D

/**
 * A [Command] representing moving [source] down at [position].
 */
data class MoveUp(override val context: GameContext,
                  override val source: GameEntity<Player>,
                  val position: Position3D) : GameCommand<EntityType> {

    override fun toString() = "trying to move up."
}
