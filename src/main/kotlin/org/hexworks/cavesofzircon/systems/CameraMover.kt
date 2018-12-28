package org.hexworks.cavesofzircon.systems

import org.hexworks.amethyst.api.EntityType
import org.hexworks.amethyst.api.base.BaseSystem
import org.hexworks.cavesofzircon.commands.CameraMoveDirection.*
import org.hexworks.cavesofzircon.commands.MoveCamera
import org.hexworks.cavesofzircon.extensions.GameCommand
import org.hexworks.cavesofzircon.extensions.position
import org.hexworks.cavesofzircon.extensions.whenCommandIs
import org.hexworks.cavesofzircon.world.GameContext

object CameraMover : BaseSystem<GameContext>() {

    override fun executeCommand(command: GameCommand<out EntityType>) = command.whenCommandIs<MoveCamera> { cmd ->
        val world = cmd.context.world
        val player = cmd.context.player
        val screenPos = player.position - world.visibleOffset()
        val halfHeight = world.visibleSize().yLength / 2
        val halfWidth = world.visibleSize().xLength / 2
        when (cmd.cameraMoveDirection) {
            UP -> {
                if (screenPos.y < halfHeight) {
                    world.scrollOneBackward()
                }
            }
            DOWN -> {
                if (screenPos.y > halfHeight) {
                    world.scrollOneForward()
                }
            }
            LEFT -> {
                if (screenPos.x < halfWidth) {
                    world.scrollOneLeft()
                }
            }
            RIGHT -> {
                if (screenPos.x > halfWidth) {
                    world.scrollOneRight()
                }
            }
        }
    }
}
