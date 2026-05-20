package com.example.taskpulse.domain.lifecycle

object EntryLifecyclePolicy {
    /** Días tras la fecha del calendario antes de archivar (desaparece de Home). */
    const val ARCHIVE_DAYS_AFTER_DUE = 2L

    /** Sin fecha: archivar tras este número de días desde la creación. */
    const val ARCHIVE_UNDATED_AFTER_DAYS = 14L

    const val MAX_ARCHIVED_ENTRIES = 50
}
