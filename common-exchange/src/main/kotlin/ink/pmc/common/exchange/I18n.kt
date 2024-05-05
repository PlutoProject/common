package ink.pmc.common.exchange

import ink.pmc.common.utils.visual.*
import net.kyori.adventure.text.Component

val MATERIAL_NOT_AVAILABLE_NAME
    get() = Component.text("<material> 不可购买").color(mochaMaroon)

val MATERIAL_NOT_AVAILABLE_LORE
    get() = listOf(Component.text("若你觉得有必要添加进兑换商店，可以向我们反馈").color(mochaSubtext0))

val TICKETS_LOOKUP_SELF
    get() = Component.text("你目前拥有 <amount> 个兑换券").color(mochaPink)

val TICKETS_LOOKUP_OTHER
    get() = Component.text("玩家 <player> 拥有 <amount> 个兑换券").color(mochaPink)

val TICKETS_SET_SUCCEED
    get() = Component.text("已将玩家 <player> 的兑换券设置为 <amount> 个").color(mochaGreen)

val TICKETS_DEPOSIT_SUCCEED
    get() = Component.text("已为玩家 <player> 增加 <amount> 个兑换券").color(mochaGreen)

val TICKETS_WITHDRAW_SUCCEED
    get() = Component.text("已为玩家 <player> 减少 <amount> 个兑换券").color(mochaGreen)

val TICKETS_WITHDRAW_FAILED_NOT_ENOUGH
    get() = Component.text("玩家 <player> 没有这么多兑换券").color(mochaMaroon)

val EXCHANGE_START_SUCCEED
    get() = Component.text("正在将你传送到兑换商店...").color(mochaGreen)

val EXCHANGE_START_FAILED_ALREADY_IN
    get() = Component.text("你已经正在兑换商店中了！").color(mochaMaroon)

val EXCHANGE_END_SUCCEED
    get() = Component.text("正在将你传送到原本的位置...").color(mochaGreen)

val EXCHANGE_END_FAILED_NOT_IN
    get() = Component.text("你并不在兑换商店中！").color(mochaMaroon)

val CHECKOUT_SUCCEED
    get() = Component.text("结账成功！此次购买花费了 <amount> 个兑换券").color(mochaGreen)

val CHECKOUT_OVER_SIZE
    get() = Component.text("<amount> 个物品由于没有足够的背包空间，被丢到了地上").color(mochaSubtext0)

val CHECKOUT_FAILED_NOT_IN_EXCHANGE
    get() = Component.text("你并不在兑换商店中，无法结账").color(mochaMaroon)

val CHECKOUT_FAILED_TICKETS_NOT_ENOUGH
    get() = Component.text("你的兑换券不足，需要 <amount> 个兑换券").color(mochaMaroon)
        .appendNewline()
        .append(Component.text("请减少背包内的待购买物品，然后再试").color(mochaSubtext0))

val CHECKOUT_FAILED_UNKNOWN_ISSUE
    get() = Component.text("出现未知错误，请联系管理员").color(mochaMaroon)

val EXCHANGE_ADMIN_START_FAILED_ALREADY_IN
    get() = Component.text("玩家 <player> 已经在兑换商店中了").color(mochaMaroon)

val EXCHANGE_ADMIN_START_SUCCEED
    get() = Component.text("正在将玩家 <player> 传送到兑换商店...").color(mochaGreen)

val EXCHANGE_ADMIN_END_FAILED_NOT_IN
    get() = Component.text("玩家 <player> 不在兑换商店中").color(mochaMaroon)

val EXCHANGE_ADMIN_END_SUCCEED
    get() = Component.text("正在将玩家 <player> 传送出兑换商店...").color(mochaGreen)

val EXCHANGE_ADMIN_CHECKOUT_SUCCEED
    get() = Component.text("正在为玩家 <player> 结账...").color(mochaGreen)

val EXCHANGE_ADMIN_MARK_AS_CHECKOUT_SIGN_SUCCEED
    get() = Component.text("已成功将指向的方块标记为结账告示牌").color(mochaGreen)

val EXCHANGE_ADMIN_MARK_AS_CHECKOUT_SIGN_FAILED_ALREADY_IS
    get() = Component.text("该告示牌已经被标记为结账告示牌").color(mochaMaroon)

val EXCHANGE_ADMIN_MARK_AS_CHECKOUT_SIGN_FAILED_NOT_SIGN
    get() = Component.text("指向的方块不是告示牌").color(mochaMaroon)

val EXCHANGE_ADMIN_MARK_AS_CHECKOUT_SIGN_NOT_FOUNT_IN_RANGE
    get() = Component.text("交互距离内未找到方块").color(mochaMaroon)

val EXCHANGE_ADMIN_MARK_AS_CHECKOUT_SIGN_FAILED_NOT_IN_EXCHANGE
    get() = Component.text("你不在兑换商店中").color(mochaMaroon)