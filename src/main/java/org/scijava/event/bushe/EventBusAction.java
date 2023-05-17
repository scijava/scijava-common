/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scijava.event.bushe;

import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;

/**
 * When fired, this action publishes events on the EventBus.
 * <p/>
 *
 * @author Michael Bushe michael@bushe.com
 * @see EventServiceAction
 */
public class EventBusAction extends EventServiceAction {
   public EventBusAction() {
      this(null, null);
   }

   public EventBusAction(String actionName, ImageIcon icon) {
      super(actionName, icon);
   }

   protected EventService getEventService(ActionEvent event) {
      return EventBus.getGlobalEventService();
   }
}

