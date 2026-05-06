package com.oleksii.pipecut.core

/**
 * Module identity marker for `:core`.
 *
 * Exists so smoke tests can verify that the module is wired correctly and
 * exposes a stable, public symbol. Real domain types arrive in PR2.
 */
object PipeCutCore {
    const val VERSION: String = "0.1.0"
}
