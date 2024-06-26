package ink.pmc.member.storage

import ink.pmc.member.javers
import org.javers.core.diff.Diff

abstract class Diffable<T> {

    fun diff(old: T?): Diff {
        return javers.compare(old, this)
    }

    abstract fun applyDiff(diff: Diff): Diffable<T>

}