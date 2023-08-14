/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.rewrite.plugin.api

import android.util.Log
import de.Maxr1998.modernpreferences.PreferenceScreen
import org.cosmicide.rewrite.util.MultipleDexClassLoader
import java.io.File
import java.lang.reflect.Modifier

object PluginLoader {
    @JvmStatic
    val loader = MultipleDexClassLoader.INSTANCE

    @JvmStatic
    val plugins = mutableListOf<Class<*>>()

    @JvmStatic
    val prefsMethods = mutableListOf<PreferenceScreen.Builder.() -> Unit>()

    @JvmStatic
    fun loadPlugin(path: File, plugin: Plugin) {
        val pluginFile =
            path.resolve("classes.dex")
        if (path.resolve("config.json").exists().not()) {
            Log.e("Plugin", "Plugin ${plugin.name} is missing config.json")
            return
        }
        if (pluginFile.exists().not()) {
            Log.e("Plugin", "Plugin ${plugin.name} is missing classes.dex")
            return
        }
        runCatching {
            loader.loadDex(pluginFile)
            val className = plugin.name.lowercase() + ".Main"
            val clazz = loader.loader.loadClass(className)
            val method = clazz.getDeclaredMethod("main", Array<String>::class.java)
            if (Modifier.isStatic(method.modifiers)) {
                method.invoke(null, arrayOf<String>())
            } else if (Modifier.isPublic(method.modifiers)) {
                method.invoke(
                    clazz.getDeclaredConstructor().newInstance(),
                    arrayOf<String>()
                )
            }

            if (clazz.declaredMethods.any { it.name == "registerPreferences" }) {
                val prefMethod = clazz.getDeclaredMethod(
                    "registerPreferences",
                    PreferenceScreen.Builder::class.java
                )
                if (Modifier.isStatic(method.modifiers)) {
                    Log.d("Plugin", "Registering preferences for plugin ${plugin.name}")
                    prefsMethods.add {
                        prefMethod.invoke(null, this)
                    }
                } else {
                    Log.e("Plugin", "registerPreferences method is not static")
                }
            } else {
                Log.d("Plugin", "Plugin ${plugin.name} does not have a registerPreferences method")
            }
        }.onSuccess {
            Log.d("Plugin", "Loaded plugin ${plugin.name}")
        }.onFailure {
            Log.e("Plugin", "Failed to load plugin ${plugin.name}", it)
        }
    }
}