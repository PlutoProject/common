package ink.pmc.common.member

import ink.pmc.common.utils.chat.replace
import ink.pmc.common.utils.concurrent.submitAsync
import org.incendo.cloud.parser.standard.StringParser
import java.time.Duration
import java.util.*

val memberAddCommand = commandManager.commandBuilder("member")
    .permission("member.add")
    .literal("add")
    .required("name", StringParser.stringParser())
    .handler {
        submitAsync {
            val sender = it.sender()
            val name = it.get<String>("name").lowercase()
            sender.sendMessage(LOOKUP)

            val uuid = getUUIDFromMojang(name)

            if (uuid == null) {
                sender.sendMessage(LOOKUP_FAILED)
                return@submitAsync
            }

            if (memberManager.exist(uuid)) {
                sender.sendMessage(MEMBER_ALREADY_EXIST)
                return@submitAsync
            }

            memberManager.createAndRegister {
                this.name = name
                this.uuid = uuid
                this.joinTime = Date()
            }

            sender.sendMessage(MEMBER_ADD_SUCCEED.replace("<player>", name))
        }
    }

val memberRemoveCommand = commandManager.commandBuilder("member")
    .permission("member.remove")
    .literal("remove")
    .required("name", StringParser.stringParser())
    .handler {
        submitAsync {
            val sender = it.sender()
            val name = it.get<String>("name").lowercase()
            sender.sendMessage(LOOKUP)

            val uuid = getUUIDFromMojang(name)

            if (uuid == null) {
                sender.sendMessage(LOOKUP_FAILED)
                return@submitAsync
            }

            if (proxyServer.allPlayers.any { it.uniqueId == uuid }) {
                sender.sendMessage(MEMBER_REMOVE_FAILED_ONLINE)
                return@submitAsync
            }

            if (memberManager.nonExist(uuid)) {
                sender.sendMessage(MEMBER_NOT_EXIST)
                return@submitAsync
            }

            memberManager.remove(uuid)
            memberManager.syncAll()

            sender.sendMessage(MEMBER_REMOVE_SUCCEED.replace("<player>", name))
        }
    }

val memberLookupCommand = commandManager.commandBuilder("member")
    .permission("member.lookup")
    .literal("lookup")
    .required("name", StringParser.stringParser())
    .handler {
        submitAsync {
            val sender = it.sender()
            val name = it.get<String>("name").lowercase()
            sender.sendMessage(LOOKUP)

            val uuid = getUUIDFromMojang(name)

            if (uuid == null) {
                sender.sendMessage(LOOKUP_FAILED)
                return@submitAsync
            }

            val member = memberManager.get(uuid)

            if (member == null) {
                sender.sendMessage(MEMBER_NOT_EXIST)
                return@submitAsync
            }

            val message = MEMBER_LOOKUP
                .replace("<player>", name)
                .replace("<uuid>", uuid.toString())
                .replace("<bio>", member.bio.toString())
                .replace("<joinTime>", member.joinTime.toString())
                .replace("<lastJoinTime>", member.lastJoinTime.toString())
                .replace("<lastQuitTime>", member.lastQuitTime.toString())
                .replace("<data>", member.data.data.toString())
                .replace("<totalPlayTime>", Duration.ofMillis(member.totalPlayTime).toString())

            sender.sendMessage(message)
        }
    }