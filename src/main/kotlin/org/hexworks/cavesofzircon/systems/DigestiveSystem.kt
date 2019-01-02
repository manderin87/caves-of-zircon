package org.hexworks.cavesofzircon.systems

import org.hexworks.amethyst.api.Consumed
import org.hexworks.amethyst.api.Pass
import org.hexworks.amethyst.api.Response
import org.hexworks.amethyst.api.base.BaseActor
import org.hexworks.amethyst.api.entity.EntityType
import org.hexworks.cavesofzircon.attributes.Hunger
import org.hexworks.cavesofzircon.attributes.types.nutritionalValue
import org.hexworks.cavesofzircon.commands.Destroy
import org.hexworks.cavesofzircon.commands.Eat
import org.hexworks.cavesofzircon.commands.Exert
import org.hexworks.cavesofzircon.extensions.*
import org.hexworks.cavesofzircon.world.GameContext

object DigestiveSystem : BaseActor<GameContext>(Hunger::class) {

    override fun executeCommand(command: GameCommand<out EntityType>): Response {
        var response: Response = command.whenCommandIs<Eat> { (_, entity, food) ->
            entity.attribute<Hunger>().currentValue += food.nutritionalValue.value
            Consumed
        }
        response = command.whenCommandIs<Exert> { (context, entity, force) ->
            val hunger = entity.attribute<Hunger>()
            hunger.currentValue -= force
            checkStarvation(context, entity, hunger)
            Consumed
        }
        return response
    }

    override fun update(entity: GameEntity<out EntityType>, context: GameContext): Boolean {
        val hunger = entity.attribute<Hunger>()
        hunger.currentValue -= 2
        checkStarvation(context, entity, hunger)
        return true
    }

    private fun checkStarvation(context: GameContext, entity: GameEntity<EntityType>, hunger: Hunger) {
        if (hunger.currentValue <= 0) {
            logGameEvent("$entity died of starvation.")
            entity.executeCommand(Destroy(
                    context = context,
                    source = entity,
                    cause = "starvation"))
        }
    }
}
