package org.hexworks.cavesofzircon.world

import org.hexworks.cavesofzircon.blocks.GameBlock
import org.hexworks.cavesofzircon.blocks.GameTile
import org.hexworks.cavesofzircon.builders.GameBlockFactory
import org.hexworks.cavesofzircon.entities.Entity
import org.hexworks.cavesofzircon.events.EntityAddedToWorld
import org.hexworks.cavesofzircon.events.EntityRemovedFromWorld
import org.hexworks.cobalt.datatypes.Identifier
import org.hexworks.cobalt.datatypes.Maybe
import org.hexworks.cobalt.datatypes.extensions.map
import org.hexworks.cobalt.logging.api.LoggerFactory
import org.hexworks.zircon.api.Positions
import org.hexworks.zircon.api.data.impl.Position3D
import org.hexworks.zircon.api.data.impl.Size3D
import org.hexworks.zircon.api.game.Cell3D
import org.hexworks.zircon.api.game.base.BaseGameArea
import org.hexworks.zircon.internal.Zircon
import org.hexworks.zircon.internal.util.TreeMap
import org.hexworks.zircon.platform.factory.TreeMapFactory


class World(tiles: Map<Position3D, GameBlock>,
            visibleSize: Size3D,
            actualSize: Size3D) : BaseGameArea<GameTile, GameBlock>(visibleSize, actualSize) {

    override val defaultBlock = GameBlockFactory.floor()

    private val logger = LoggerFactory.getLogger(javaClass)
    private val blocks: TreeMap<Position3D, GameBlock> = TreeMapFactory.create()
    private val entityPositionLookup = mutableMapOf<Identifier, Position3D>()

    init {
        tiles.forEach { pos, block ->
            val pos3D = pos
            setBlockAt(pos3D, block)
            block.getEntities().forEach {
                entityPositionLookup[it.id] = pos3D
            }
        }
    }

    /**
     * Moves the given [Entity] to the given [Position3D].
     * Has no effect if this [World] doesn't contain the given [Entity].
     * @return true if the entity was moved
     */
    fun moveEntity(entity: Entity, position: Position3D): Boolean {
        return if (actualSize().containsPosition(position) && position.x >= 0 && position.y >= 0) {
            logger.info("Entity position is valid. Moving entity to: $position")
            entityPositionLookup[entity.id]?.let {
                removeEntity(entity)
                addEntity(entity, position)
                true
            } ?: false
        } else {
            false
        }
    }

    /**
     * Adds the given [Entity] at the given [Position3D].
     * Has no effect if this world already contains the
     * given [Entity].
     */
    fun addEntity(entity: Entity, position: Position3D) {
        if (entityPositionLookup.containsKey(entity.id).not()) {
            entityPositionLookup[entity.id] = position
            fetchBlockAt(position).map {
                it.addEntity(entity)
                Zircon.eventBus.publish(EntityAddedToWorld(entity))
            }
        }
    }

    fun removeEntity(entity: Entity) {
        logger.info("Trying to remove $entity from world.")
        entityPositionLookup[entity.id]?.let { pos ->
            logger.info("Entity $entity found in world. Removing...")
            blocks[pos]?.removeEntity(entity)
            entityPositionLookup.remove(entity.id)
            Zircon.eventBus.publish(EntityRemovedFromWorld(entity))
        }
    }

    /**
     * Finds the [Position3D] of the given [Entity].
     */
    fun findPositionOfEntity(entity: Entity): Maybe<Position3D> {
        return Maybe.ofNullable(entityPositionLookup[entity.id])
    }

    /**
     * Finds an empty location within the given area (offset and size) on this [World].
     */
    fun findEmptyLocation(offset: Position3D = visibleOffset(), size: Size3D = visibleSize()): Maybe<Position3D> {
        var position = Maybe.empty<Position3D>()
        val maxTries = 10
        var currentTry = 0
        while (position.isPresent.not() && currentTry < maxTries) {
            val pos = Positions.create3DPosition(
                    x = (Math.random() * size.xLength).toInt() + offset.x,
                    y = (Math.random() * size.yLength).toInt() + offset.y,
                    z = 0)
            fetchBlockAt(pos).map {
                if (it.isOccupied().not()) {
                    position = Maybe.of(pos)
                }
            }
            currentTry++
        }
        logger.info("Empty location found: '${position.get()}'")
        return position
    }

    // implementations for GameArea
    override fun layersPerBlock() = 2

    override fun hasBlockAt(position: Position3D) = blocks.containsKey(position)

    override fun fetchBlockAt(position: Position3D): Maybe<GameBlock> {
        return Maybe.ofNullable(blocks[position])
    }

    override fun fetchBlockOrDefault(position: Position3D) =
            blocks.getOrDefault(position, defaultBlock)

    override fun fetchBlocks(): Iterable<Cell3D<GameTile, GameBlock>> {
        return blocks.map { Cell3D.create(it.key, it.value) }
    }

    override fun setBlockAt(position: Position3D, block: GameBlock) {
        require(actualSize().containsPosition(position)) {
            "The supplied position ($position) is not within the size (${actualSize()}) of this game area."
        }
        val layerCount = block.layers.size
        require(layerCount == layersPerBlock()) {
            "The number of layers per block for this game area is ${layersPerBlock()}." +
                    " The supplied layers have a size of $layerCount."
        }
        blocks[position] = block
    }
}
