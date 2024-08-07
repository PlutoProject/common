package ink.pmc.transfer

import ink.pmc.transfer.api.Destination
import ink.pmc.transfer.api.DestinationStatus

abstract class AbstractDestination : Destination {

    abstract override var status: DestinationStatus
    abstract override var playerCount: Int
    abstract override var maxPlayerCount: Int

}