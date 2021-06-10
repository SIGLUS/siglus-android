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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Program;

import java.util.ArrayList;
import java.util.List;

@RunWith(LMISTestRunner.class)
public class ProgramCacheManagerTest {

    @Before
    public void setup(){
        ProgramCacheManager.PROGRAMS_CACHE.clear();
    }

    @Test
    public void addProgramsTest(){
        // when
        ProgramCacheManager.addPrograms(createPrograms());

        // then
        MatcherAssert.assertThat(ProgramCacheManager.PROGRAMS_CACHE.size(), Matchers.is(1));
    }

    @Test
    public void getProgramTest(){
        // given
        ProgramCacheManager.addPrograms(createPrograms());

        // when
        final Program exitingProgram = ProgramCacheManager.getPrograms("123");
        final Program notExitingProgram = ProgramCacheManager.getPrograms("321");

        // then
        MatcherAssert.assertThat(exitingProgram,Matchers.notNullValue());
        MatcherAssert.assertThat(notExitingProgram,Matchers.nullValue());
    }

    private List<Program> createPrograms(){
        final ArrayList<Program> programs = new ArrayList<>();
        final Program program = new Program();
        program.setProgramCode("123");
        program.setParentCode("123");
        programs.add(program);
        return programs;
    }
}