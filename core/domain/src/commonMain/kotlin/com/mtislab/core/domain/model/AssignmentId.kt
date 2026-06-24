package com.mtislab.core.domain.model

import kotlin.jvm.JvmInline

/**
 * Stable identifier for a single bundle assignment on an eSIM.
 *
 * Same-SKU stacks produce multiple entries that share `bundleName` but carry
 * distinct `AssignmentId`s. This is the canonical list / diff / animation key.
 */
@JvmInline
value class AssignmentId(val raw: String)
