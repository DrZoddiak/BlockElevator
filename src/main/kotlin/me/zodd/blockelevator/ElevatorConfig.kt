package me.zodd.blockelevator

import org.spongepowered.api.ResourceKey
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.registry.RegistryTypes
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import kotlin.jvm.optionals.getOrNull

@ConfigSerializable
data class ElevatorConfig(
    @field:Comment("Maximum distance to search for elevators")
    val maxElevatorDistance: Int = 10,
    @field:Comment("The block to use for elevators")
    val elevatorBlock: String = "minecraft:iron_block",
    @field:Comment("A list of safe blocks to teleport into")
    val safeBlocks: List<String> = listOf(
        "minecraft:torch",
        "minecraft:wall_torch",
        "minecraft:soul_torch",
        "minecraft:soul_wall_torch",
        "minecraft:redstone_torch",
        "minecraft:redstone_wall_torch"
    )
) {

    fun elevatorAsBlock(): BlockType? {
        return elevatorBlock.asBlock()
    }

    // Load valid entries, others ignored.
    fun safeBlocksAsBlocks(): List<BlockType> {
        return safeBlocks.mapNotNull { it.asBlock() }
    }

    private fun String.asBlock(): BlockType? {
        return split(":", limit = 2).let {
            runCatching {
                it[0] to it[1]
            }.getOrNull()
        }?.let { ResourceKey.of(it.first, it.second) }?.let {
            Sponge.server().registry(RegistryTypes.BLOCK_TYPE)
                .findValue<BlockType>(it)
                .getOrNull()
        }
    }
}