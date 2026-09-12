/**
 * SkillAPI
 * com.sucy.skill.storage.StaleDocumentException
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.storage;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Raised when a write was rejected because the stored revision moved on.
 *
 * <p>This is a correctness signal, not a failure: it means another writer
 * committed a newer version of the same player, and applying this write would
 * have destroyed it. The whole batch is rolled back, so the store is left
 * exactly as it was before the attempt.</p>
 */
public class StaleDocumentException extends SQLException {
    private static final long serialVersionUID = 1L;

    private final Set<String> conflicts;

    /**
     * @param conflicts player identifiers whose stored revision had moved on
     */
    public StaleDocumentException(Set<String> conflicts) {
        super("Player data changed elsewhere and was not overwritten: " + conflicts);
        this.conflicts = Collections.unmodifiableSet(new LinkedHashSet<String>(conflicts));
    }

    /**
     * @return identifiers of the players whose writes were rejected
     */
    public Set<String> getConflicts() {
        return conflicts;
    }
}
