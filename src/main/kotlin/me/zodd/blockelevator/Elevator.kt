package me.zodd.blockelevator

import net.kyori.adventure.sound.Sound
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.tag.BlockTypeTags
import org.spongepowered.api.world.server.ServerLocation

class Elevator(private val originalPosition: ServerLocation) {

    fun nextElevatorUp(): ServerLocation? {
        return loopElevator {
            add(0.0, it.toDouble(), 0.0)
        }
    }

    fun nextElevatorDown(): ServerLocation? {
        return loopElevator {
            sub(0.0, it.toDouble(), 0.0)
        }
    }

    private fun loopElevator(position: ServerLocation.(Int) -> ServerLocation): ServerLocation? {
        /*
         * Starts at 2 because that's the minimum distance a teleport can safely take place
         */
        (2..<BlockElevator.elevatorConfig.maxElevatorDistance + 1).forEach {
            val checkedPos = position(originalPosition, it)
            if (isElevatorBlock(checkedPos) && isSafeTeleport(checkedPos)) {
                return checkedPos
            }
        }
        return null
    }

    private fun isSafeTeleport(pos: ServerLocation): Boolean {
        return pos.add(0.0, 1.0, 0.0).isSafeBlock() &&
                pos.add(0.0, 2.0, 0.0).isSafeBlock()
    }

    private fun ServerLocation.isSafeBlock(): Boolean {
        return blockType().`is`(BlockTypeTags.AIR) || blockType().isAnyOf(
            *BlockElevator.elevatorConfig.safeBlocksAsBlocks().toTypedArray()
        )
    }

    fun isElevatorBlock(loc: ServerLocation): Boolean {
        return loc.blockType() == BlockElevator.elevatorConfig.elevatorAsBlock()
    }

    fun sendElevatorNoise(loc: ServerLocation) {

        val sound = Sound.sound().apply {
            type(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP.get())
            source(Sound.Source.BLOCK)
            pitch(1f)
        }.build()

        loc.world().playSound(sound)
    }
}

