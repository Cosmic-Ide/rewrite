/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.rewrite.util

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.cosmicide.rewrite.plugin.api.HookManager

object PermissionUtils {

    fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        HookManager.context.get()!!,
        permission
    ) == PackageManager.PERMISSION_GRANTED

}