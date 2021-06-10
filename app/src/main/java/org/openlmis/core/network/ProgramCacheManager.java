/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */

package org.openlmis.core.network;

import org.openlmis.core.model.Program;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import androidx.annotation.Nullable;

/**
 * cache program from facility info api response
 * note: only for network response adapter,
 *      if you want get program in other place, please use {@link org.openlmis.core.model.repository.ProgramRepository}
 */
public final class ProgramCacheManager {

    static final Map<String, Program> PROGRAMS_CACHE = new ConcurrentHashMap<>();

    private ProgramCacheManager() {
    }

    public static void addPrograms(List<Program> programs) {
        if (programs == null) return;
        for (Program program : programs) {
            PROGRAMS_CACHE.put(program.getProgramCode(), program);
        }
    }

    @Nullable
    public static Program getPrograms(@Nullable String programCode) {
        if (PROGRAMS_CACHE.containsKey(programCode)) return PROGRAMS_CACHE.get(programCode);
        return null;
    }
}
