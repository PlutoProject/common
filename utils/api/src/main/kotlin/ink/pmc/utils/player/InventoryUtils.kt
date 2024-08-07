package ink.pmc.utils.player

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/*
* 代码来自 https://gist.github.com/graywolf336/8153678。
* */

/**
 * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
 *
 * @param playerInventory to turn into an array of strings.
 * @return Array of strings: [ main content, armor content ]
 * @throws IllegalStateException
 */
fun playerInventoryToBase64(playerInventory: PlayerInventory): Pair<String, String> {
    //get the main content part, this doesn't return the armor
    val content = inventoryToBase64(playerInventory)
    val armor = itemStackArrayToBase64(playerInventory.armorContents)

    return content to armor
}

/**
 *
 * A method to serialize an [ItemStack] array to Base64 String.
 *
 *
 *
 *
 * Based off of [.inventoryToBase64].
 *
 * @param items to turn into a Base64 String.
 * @return Base64 string of the items.
 * @throws IllegalStateException
 */
fun itemStackArrayToBase64(items: Array<ItemStack?>): String {
    try {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)


        // Write the size of the inventory
        dataOutput.writeInt(items.size)


        // Save every element in the list
        for (i in items.indices) {
            dataOutput.writeObject(items[i])
        }


        // Serialize that array
        dataOutput.close()
        return Base64Coder.encodeLines(outputStream.toByteArray())
    } catch (e: Exception) {
        throw IllegalStateException("Unable to save provider stacks.", e)
    }
}

/**
 * A method to serialize an inventory to Base64 string.
 *
 *
 *
 *
 * Special thanks to Comphenix in the Bukkit forums or also known
 * as aadnk on GitHub.
 *
 * [Original Source](https://gist.github.com/aadnk/8138186)
 *
 * @param inventory to serialize
 * @return Base64 string of the provided inventory
 * @throws IllegalStateException
 */
fun inventoryToBase64(inventory: Inventory): String {
    try {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)


        // Write the size of the inventory
        dataOutput.writeInt(inventory.size)


        // Save every element in the list
        for (i in 0..<inventory.size) {
            dataOutput.writeObject(inventory.getItem(i))
        }


        // Serialize that array
        dataOutput.close()
        return Base64Coder.encodeLines(outputStream.toByteArray())
    } catch (e: Exception) {
        throw IllegalStateException("Unable to save provider stacks.", e)
    }
}

/**
 *
 * A method to get an [Inventory] from an encoded, Base64, string.
 *
 *
 *
 *
 * Special thanks to Comphenix in the Bukkit forums or also known
 * as aadnk on GitHub.
 *
 * [Original Source](https://gist.github.com/aadnk/8138186)
 *
 * @param data Base64 string of data containing an inventory.
 * @return Inventory created from the Base64 string.
 * @throws IOException
 */
fun inventoryFromBase64(data: String): Inventory {
    try {
        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
        val dataInput = BukkitObjectInputStream(inputStream)
        val inventory = Bukkit.getServer().createInventory(null, dataInput.readInt())

        // Read the serialized inventory
        for (i in 0..<inventory.size) {
            val obj = dataInput.readObject()
            inventory.setItem(i, if (obj == null) null else obj as ItemStack)
        }

        dataInput.close()
        return inventory
    } catch (e: ClassNotFoundException) {
        throw IOException("Unable to decode class type.", e)
    }
}

/**
 * Gets an array of ItemStacks from Base64 string.
 *
 *
 *
 *
 * Base off of [.inventoryFromBase64].
 *
 * @param data Base64 string to convert to ItemStack array.
 * @return ItemStack array created from the Base64 string.
 * @throws IOException
 */
fun itemStackArrayFromBase64(data: String): Array<ItemStack?> {
    try {
        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
        val dataInput = BukkitObjectInputStream(inputStream)
        val items = arrayOfNulls<ItemStack>(dataInput.readInt())


        // Read the serialized inventory
        for (i in items.indices) {
            items[i] = dataInput.readObject() as ItemStack
        }

        dataInput.close()
        return items
    } catch (e: ClassNotFoundException) {
        throw IOException("Unable to decode class type.", e)
    }
}

/**
 * Gets one [ItemStack] from Base64 string.
 *
 * @param data Base64 string to convert to [ItemStack].
 * @return [ItemStack] created from the Base64 string.
 * @throws IOException
 */
fun itemStackFromBase64(data: String?): ItemStack {
    try {
        val inputStream = ByteArrayInputStream(Base64Coder.decode(data))
        val dataInput = BukkitObjectInputStream(inputStream)

        // Read the serialized inventory
        val item = dataInput.readObject() as ItemStack

        dataInput.close()
        return item
    } catch (e: ClassNotFoundException) {
        throw IOException("Unable to decode class type.", e)
    }
}

/**
 * A method to serialize one [ItemStack] to Base64 String.
 *
 * @param item to turn into a Base64 String.
 * @return Base64 string of the provider.
 * @throws IllegalStateException
 */
fun itemStackToBase64(item: ItemStack?): String {
    try {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)

        // Save every element
        dataOutput.writeObject(item)

        // Serialize that array
        dataOutput.close()
        return String(Base64Coder.encode(outputStream.toByteArray()))
    } catch (e: java.lang.Exception) {
        throw java.lang.IllegalStateException("Unable to save provider stacks.", e)
    }
}