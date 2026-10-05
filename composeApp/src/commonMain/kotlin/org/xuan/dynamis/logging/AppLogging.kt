package org.xuan.dynamis.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * Debug builds log everything. Release builds keep only warnings and errors, which also keeps
 * request URLs (they contain the user's coordinates) and location details out of production logs.
 */
fun configureLogging(isDebug: Boolean) {
    Logger.setMinSeverity(if (isDebug) Severity.Debug else Severity.Warn)
}
