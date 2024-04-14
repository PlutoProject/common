package ink.pmc.common.member.commands

import com.mongodb.client.model.Filters.eq
import com.velocitypowered.api.command.CommandSource
import ink.pmc.common.member.*
import ink.pmc.common.member.api.AuthType
import ink.pmc.common.member.velocity.commandManager
import ink.pmc.common.member.velocity.proxy
import ink.pmc.common.utils.bedrock.xuid
import ink.pmc.common.utils.chat.replace
import ink.pmc.common.utils.command.VelocityCommand
import ink.pmc.common.utils.concurrent.submitAsync
import ink.pmc.common.utils.visual.mochaYellow
import kotlinx.coroutines.flow.firstOrNull
import net.kyori.adventure.text.Component
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.parser.flag.FlagContext
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.suggestion.Suggestion
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture

object MemberCommand : VelocityCommand() {

    private val authTypeArg = CommandComponent.builder<CommandSource, String>()
        .suggestionProvider { _, _ ->
            CompletableFuture.completedFuture(
                listOf(
                    Suggestion.simple("OFFICIAL"),
                    Suggestion.simple("LITTLESKIN"),
                    Suggestion.simple("BEDROCK_ONLY"),
                )
            )
        }
        .parser(StringParser.stringParser())
        .defaultValue(DefaultValue.constant("OFFICIAL"))
        .name("authType")
        .optional()

    private val onlinePlayersArg
        get() = CommandComponent.builder<CommandSource, String>()
            .suggestionProvider { _, _ ->
                CompletableFuture.completedFuture(
                    proxy.allPlayers.map { Suggestion.simple(it.username) }
                )
            }
            .parser(StringParser.stringParser())
            .name("name")
            .required()

    private val memberCreate = commandManager.commandBuilder("member")
        .permission("member.create")
        .literal("create")
        .required("name", StringParser.stringParser())
        .flag(commandManager.flagBuilder("auth").withAliases("a").withComponent(authTypeArg))
        .handler {
            submitAsync {
                val sender = it.sender()
                val name = it.get<String>("name").lowercase()
                val authType = parseAuthType(it.flags())

                if (authType == null) {
                    sender.sendMessage(MEMBER_LOOKUP_FAILED_UNKNOWN_AUTH_TYPE)
                    return@submitAsync
                }

                sender.sendMessage(LOOKUP)
                val uuid = authType.fetcher.fetch(name)

                if (uuid == null) {
                    sender.sendMessage(LOOKUP_FAILED)
                    return@submitAsync
                }

                if (memberService.exist(uuid)) {
                    sender.sendMessage(MEMBER_ALREADY_EXIST)
                    return@submitAsync
                }

                memberService.create(name, authType)
                sender.sendMessage(MEMBER_ADD_SUCCEED.replace("<player>", Component.text(name).color(mochaYellow)))
            }
        }

    private val memberModifyExemptWhitelist = commandManager.commandBuilder("member")
        .permission("member.modify.exemptwhitelist")
        .literal("modify")
        .literal("exemptwhitelist")
        .argument(onlinePlayersArg)
        .flag(commandManager.flagBuilder("auth").withAliases("a").withComponent(authTypeArg))
        .handler {
            submitAsync {
                val sender = it.sender()
                val name = it.get<String>("name").lowercase()
                val authType = parseAuthType(it.flags())

                if (authType == null) {
                    sender.sendMessage(MEMBER_LOOKUP_FAILED_UNKNOWN_AUTH_TYPE)
                    return@submitAsync
                }

                if (!memberService.exist(name, authType)) {
                    sender.sendMessage(MEMBER_NOT_EXIST)
                    return@submitAsync
                }

                val member = memberService.lookup(name, authType)!!.refresh()!!

                if (!member.isWhitelisted) {
                    sender.sendMessage(
                        MEMBER_EXEMPT_WHITELIST_FAILED_NOT_WHITELISTED.replace(
                            "<player>", Component.text(name).color(
                                mochaYellow
                            )
                        )
                    )
                    return@submitAsync
                }

                member.exemptWhitelist()
                member.update()

                val player = proxy.getPlayer(member.id)

                if (player.isPresent) {
                    player.get().disconnect(NOT_WHITELISTED)
                }

                sender.sendMessage(
                    MEMBER_EXEMPT_WHITELIST_SUCCEED.replace(
                        "<player>",
                        Component.text(name).color(mochaYellow)
                    )
                )
            }
        }

    private val memberModifyGrantWhitelist = commandManager.commandBuilder("member")
        .permission("member.modify.grantwhitelist")
        .literal("modify")
        .literal("grantwhitelist")
        .argument(onlinePlayersArg)
        .flag(commandManager.flagBuilder("auth").withAliases("a").withComponent(authTypeArg))
        .handler {
            submitAsync {
                val sender = it.sender()
                val name = it.get<String>("name").lowercase()
                val authType = parseAuthType(it.flags())

                if (authType == null) {
                    sender.sendMessage(MEMBER_LOOKUP_FAILED_UNKNOWN_AUTH_TYPE)
                    return@submitAsync
                }

                if (!memberService.exist(name, authType)) {
                    sender.sendMessage(MEMBER_NOT_EXIST)
                    return@submitAsync
                }

                val member = memberService.lookup(name, authType)!!.refresh()!!

                if (member.isWhitelisted) {
                    sender.sendMessage(
                        MEMBER_GRANT_WHITELIST_FAILED_ALREADY_WHITELISTED.replace(
                            "<player>", Component.text(name).color(
                                mochaYellow
                            )
                        )
                    )
                    return@submitAsync
                }

                member.grantWhitelist()
                member.update()

                sender.sendMessage(
                    MEMBER_GRAND_WHITELIST_SUCCEED.replace(
                        "<player>",
                        Component.text(name).color(mochaYellow)
                    )
                )
            }
        }

    private val memberModifyLinkBeAccount = commandManager.commandBuilder("member")
        .permission("member.modify.linkbedrock")
        .literal("modify")
        .literal("linkbedrock")
        .argument(onlinePlayersArg)
        .required("gamertag", StringParser.stringParser())
        .flag(commandManager.flagBuilder("auth").withAliases("a").withComponent(authTypeArg))
        .handler {
            submitAsync {
                val sender = it.sender()
                val name = it.get<String>("name").lowercase()
                val authType = parseAuthType(it.flags())
                val gamertag = it.get<String>("gamertag").lowercase()

                if (authType == null) {
                    sender.sendMessage(MEMBER_LOOKUP_FAILED_UNKNOWN_AUTH_TYPE)
                    return@submitAsync
                }

                if (!memberService.exist(name, authType)) {
                    sender.sendMessage(MEMBER_NOT_EXIST)
                    return@submitAsync
                }

                if (authType.isBedrock) {
                    sender.sendMessage(MEMBER_LINK_BE_FAILED_BE_ONLY)
                    return@submitAsync
                }

                val member = memberService.lookup(name, authType)!!.refresh()!!

                if (member.bedrockAccount != null) {
                    sender.sendMessage(MEMBER_LINK_BE_FAILED_ALREADY_LINKED)
                    return@submitAsync
                }

                val xuid = AuthType.BEDROCK_ONLY.fetcher.fetch(gamertag)?.xuid

                if (xuid == null) {
                    sender.sendMessage(MEMBER_LINK_BE_FAILED_NOT_EXISTED)
                    return@submitAsync
                }

                val beStorage = memberService.bedrockAccounts.find(eq("xuid", xuid)).firstOrNull()

                if (beStorage != null) {
                    val linkedMember = memberService.lookup(beStorage.linkedWith)!!
                    sender.sendMessage(
                        MEMBER_LINK_BE_FAILED_ACCOUNT_ALREADY_EXISTED
                            .replace("<gamertag>", Component.text(beStorage.gamertag).color(mochaYellow))
                            .replace("<xuid>", Component.text(beStorage.xuid).color(mochaYellow))
                            .replace("<other>", Component.text(linkedMember.rawName).color(mochaYellow))
                    )
                    return@submitAsync
                }

                member.linkBedrock(xuid, gamertag)
                member.update()

                sender.sendMessage(MEMBER_LINK_BE_SUCCEED.replace("<player>", member.rawName))
            }
        }

    private val memberModifyUnlinkBeAccount = commandManager.commandBuilder("member")
        .permission("member.modify.unlinkbedrock")
        .literal("modify")
        .literal("unlinkbedrock")
        .argument(onlinePlayersArg)
        .flag(commandManager.flagBuilder("auth").withAliases("a").withComponent(authTypeArg))
        .flag(commandManager.flagBuilder("force").withAliases("f"))
        .handler {
            submitAsync {
                val sender = it.sender()
                val name = it.get<String>("name").lowercase()
                val authType = parseAuthType(it.flags())
                val force = it.flags().isPresent("force")

                if (authType == null) {
                    sender.sendMessage(MEMBER_LOOKUP_FAILED_UNKNOWN_AUTH_TYPE)
                    return@submitAsync
                }

                if (!memberService.exist(name, authType)) {
                    sender.sendMessage(MEMBER_NOT_EXIST)
                    return@submitAsync
                }

                if (authType.isBedrock && !force) {
                    sender.sendMessage(MEMBER_UNLINK_BE_FAILED_ALREADY_BE_ONLY)
                    return@submitAsync
                }

                val member = memberService.lookup(name, authType)!!.refresh()!!

                if (member.bedrockAccount == null) {
                    sender.sendMessage(MEMBER_UNLINK_BE_FAILED_NOT_LINKED)
                    return@submitAsync
                }

                member.unlinkBedrock()
                member.update()

                sender.sendMessage(MEMBER_UNLINK_BE_SUCCEED.replace("<player>", member.rawName))
            }
        }

    private val memberLookup = commandManager.commandBuilder("member")
        .permission("member.lookup")
        .literal("lookup")
        .argument(onlinePlayersArg)
        .flag(commandManager.flagBuilder("auth").withAliases("a").withComponent(authTypeArg))
        .handler {
            submitAsync {
                val sender = it.sender()
                val name = it.get<String>("name").lowercase()
                val authType = parseAuthType(it.flags())

                if (authType == null) {
                    sender.sendMessage(MEMBER_LOOKUP_FAILED_UNKNOWN_AUTH_TYPE)
                    return@submitAsync
                }

                if (!memberService.exist(name, authType)) {
                    sender.sendMessage(MEMBER_NOT_EXIST)
                    return@submitAsync
                }

                val member = memberService.lookup(name, authType)!!.refresh()!!

                sender.sendMessage(
                    MEMBER_LOOKUP
                        .replace("<uid>", member.uid.toString())
                        .replace("<id>", member.id.toString())
                        .replace("<name>", member.name)
                        .replace("<rawName>", member.rawName)
                        .replace("<whitelistStatus>", member.whitelistStatus.toString())
                        .replace("<authType>", member.authType.toString())
                        .replace("<createdAt>", formattedTime(member.createdAt))
                        .replace("<lastJoinedAt>", formattedTime(member.lastJoinedAt))
                        .replace("<lastQuitedAt>", formattedTime(member.lastJoinedAt))
                        .replace("<dataContainer>", member.dataContainer.contents.toString())
                        .replace("<bedrockAccount>", nullableString(member.bedrockAccount?.gamertag))
                        .replace("<bio>", nullableString(member.bio))
                )
            }
        }

    private fun parseAuthType(flagContext: FlagContext): AuthType? {
        if (!flagContext.isPresent("auth")) {
            return AuthType.OFFICIAL
        }

        val type: AuthType

        try {
            type = AuthType.valueOf(flagContext.get<String>("auth")!!.uppercase())
        } catch (e: Exception) {
            return null
        }

        return type
    }

    private fun formattedTime(instant: Instant?): String {
        if (instant == null) {
            return "无"
        }

        val zonedDateTime = instant.atZone(ZoneId.of("Asia/Shanghai"))
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd a hh:mm")
            .withLocale(Locale.forLanguageTag("zh-CN"))
            .withZone(ZoneId.of("Asia/Shanghai"))
        return formatter.format(zonedDateTime)
    }

    private fun nullableString(string: String?): String {
        if (string == null) {
            return "无"
        }

        return string
    }

    init {
        command(memberCreate)
        command(memberModifyExemptWhitelist)
        command(memberModifyGrantWhitelist)
        command(memberLookup)
        command(memberModifyLinkBeAccount)
        command(memberModifyUnlinkBeAccount)
    }

}