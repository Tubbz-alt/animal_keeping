/******************************************************************************
 Copyright (c) 2017 Neuroethology Lab, University of Tuebingen,
 Jan Grewe <jan.grewe@g-node.org>,
 Dennis Huben <dennis.huben@rwth-aachen.de>

 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list
 of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this
 list of conditions and the following disclaimer in the documentation and/or other
 materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its contributors may
 be used to endorse or promote products derived from this software without specific
 prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGE.

 * Created by jan on 27.12.16.

 *****************************************************************************/
package animalkeeping.logging;

import animalkeeping.ui.Main;
import org.hibernate.Session;

public class ChangeLogUtil {

    public static void LogIt(String action, ChangeLogInterface entity ){
        String user = Main.getCredentials().getUser();
        ChangeLog auditRecord = new ChangeLog(action, entity.getType(),entity.getId(), user);
        doLog(auditRecord);
    }

    public static void LogIt(String action, ChangeLogInterface entity, String changeSet){
        String user = Main.getCredentials().getUser();
        ChangeLog auditRecord = new ChangeLog(action, entity.getType(),entity.getId(), user, changeSet);
        doLog(auditRecord);
    }

    private static void doLog(ChangeLog changeLog) {
        try (Session tempSession = Main.sessionFactory.openSession()) {
            tempSession.beginTransaction();
            tempSession.save(changeLog);
            tempSession.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

