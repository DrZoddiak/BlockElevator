package me.zodd.blockelevator

import com.google.common.cache.CacheBuilder
import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.data.Keys
import org.spongepowered.api.entity.living.player.server.ServerPlayer
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.filter.cause.Root
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.reference.ConfigurationReference
import org.spongepowered.math.vector.Vector3d
import org.spongepowered.plugin.PluginContainer
import org.spongepowered.plugin.builtin.jvm.Plugin
import java.util.UUID
import java.util.concurrent.TimeUnit

@Plugin("BlockElevator")
class BlockElevator @Inject internal constructor(
    private val container: PluginContainer,
    private val logger: Logger,
    @DefaultConfig(sharedRoot = false)
    val reference: ConfigurationReference<CommentedConfigurationNode>,
) {

    companion object {
        lateinit var elevatorConfig: ElevatorConfig
    }

    @Listener
    fun onConstructPlugin(event: ConstructPluginEvent?) {
        logger.info("Constructing BlockElevator")
        elevatorConfig = loadConfig()
    }

    private fun loadConfig(): ElevatorConfig {
        val rootNode = reference.referenceTo(ElevatorConfig::class.java)
        reference.save(rootNode.node())
        return rootNode.get() ?: throw ConfigurateException("Failed to load config!")
    }

    private val jumpCache: com.google.common.cache.Cache<UUID, String> = CacheBuilder.newBuilder()
        .expireAfterWrite(250, TimeUnit.MILLISECONDS)
        .build()

    @Listener
    fun onPlayerJump(event: MoveEntityEvent, @Root player: ServerPlayer) {
        if (jumpCache.getIfPresent(player.uniqueId()) != null) return

        takeIf { hasElevatorPermission(player) } ?: return

        takeUnless { player.onGround().get() } ?: return

        val elevator = player.serverLocation().sub(0.0, 1.0, 0.0).let {
            Elevator(it).takeIf { block ->
                block.isElevatorBlock(it)
            } ?: return
        }

        // If player hasn't increased Y, return
        if (event.originalPosition().y() >= event.destinationPosition().y()) return

        elevator.nextElevatorUp()?.let { loc ->
            loc.add(0.0, 1.0, 0.0).position().let {
                event.setDestinationPosition(it)
            }
        }
        player.offer(Keys.VELOCITY, Vector3d.ZERO)
        elevator.sendElevatorNoise(player.serverLocation())
        jumpCache.put(player.uniqueId(), "")
    }

    @Listener
    fun onPlayerSneak(event: ChangeDataHolderEvent.ValueChange, @Root player: ServerPlayer) {
        takeIf { hasElevatorPermission(player) } ?: return
        // If the player isn't touching the ground, we can skip everything else
        takeIf { player.onGround().get() } ?: return

        event.endResult().replacedData().firstOrNull { it.key().key() == Keys.IS_SNEAKING } ?: return

        val blockBelow = player.serverLocation().sub(0.0, 1.0, 0.0)

        val elevator = Elevator(blockBelow)

        elevator.takeIf { it.isElevatorBlock(blockBelow) }?.nextElevatorDown()?.let {
            player.setLocation(it.add(0.0, 1.0, 0.0))
            elevator.sendElevatorNoise(player.serverLocation())
        } ?: return

        /*
         * If the event is not cancelled then the player will
         * chain elevators.
         */
        event.isCancelled = true
    }

    private fun hasElevatorPermission(subject: Subject): Boolean {
        return subject.hasPermission("blockelevator.use.base")
    }
}




