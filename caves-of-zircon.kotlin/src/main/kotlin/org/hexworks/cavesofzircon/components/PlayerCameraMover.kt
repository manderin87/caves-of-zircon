package org.hexworks.cavesofzircon.components

import org.hexworks.cavesofzircon.commands.Command
import org.hexworks.cavesofzircon.commands.MoveCamera
import org.hexworks.cavesofzircon.extensions.whenCommandIs
import org.hexworks.cobalt.logging.api.LoggerFactory
import org.hexworks.zircon.api.input.InputType.*

object PlayerCameraMover : BaseComponent() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun executeCommand(command: Command) = command.whenCommandIs<MoveCamera> {
        logger.info("Moving camera.")
        val (world, _, input, player) = command.context
        val screenPos = world.findPositionOfEntity(player).get() - world.visibleOffset()
        val halfHeight = world.visibleSize().yLength / 2
        val halfWidth = world.visibleSize().xLength / 2
        when (input.inputType()) {
            ArrowUp -> {
                if (screenPos.y < halfHeight) {
                    world.scrollOneBackward()
                }
            }
            ArrowDown -> {
                if (screenPos.y > halfHeight) {
                    world.scrollOneForward()
                }
            }
            ArrowLeft -> {
                if (screenPos.x < halfWidth) {
                    world.scrollOneLeft()
                }
            }
            ArrowRight -> {
                if (screenPos.x > halfWidth) {
                    world.scrollOneRight()
                }
            }
            else -> {
            }
        }
    }
}
