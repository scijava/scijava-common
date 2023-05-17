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

import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;


/**
 * Registers a component with it's Container's EventService while keeping track of the component's container.
 * <p/>
 * Registering with a component's ContainerEventService is tricky since components may not be in their hierarchy when
 * they want to register with it, or components may move (though rarely).  This class subscribes a component with it's
 * container event service.  If it is unavailable, the registrar waits until the component's Container becomes available
 * and subscribes at that time.  If the component changes Containers, the registrar unsubscribes the component from its
 * old container and subscribes it to the new one.
 *
 * @author Michael Bushe michael@bushe.com
 */
class ContainerEventServiceRegistrar {
   private JComponent jComp;
   private IEventSubscriber eventSubscriber;
   private VetoEventListener vetoSubscriber;
   private Class[] eventClasses;
   private IEventTopicSubscriber eventTopicSubscriber;
   private VetoTopicEventListener vetoTopicSubscriber;
   private String[] topics;
   private EventService containerEventService;

   /**
    * Create a registrar that will keep track of the container event service, typically used in the publish-only cases
    * where the getContainerEventServer() call will be made before publication.
    *
    * @param jComp the component whose container to monitor
    */
   public ContainerEventServiceRegistrar(JComponent jComp) {
      this(jComp, null, null, null, null, null, null);
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the subscriber to the
    * eventClass when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventSubscriber the subscriber to register to the Container EventServer
    * @param eventClasses the class(es) to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, IEventSubscriber eventSubscriber, Class... eventClasses) {
      this(jComp, eventSubscriber, null, eventClasses, null, null, null);
   }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the subscriber to the topic
    * when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventTopicSubscriber the topic subscriber to register to the Container EventServer
    * @param topics the event topic name to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, IEventTopicSubscriber eventTopicSubscriber, String... topics) {
      this(jComp, null, null, null, eventTopicSubscriber, null, topics);
   }

    /**
     * Create a registrar that will keep track of the container event service, and subscribeStrongly the veto subscriber
     * to the topics when the ContainerEventService is available and when it changes.
     *
     * @param jComp the component whose container to monitor
     * @param vetoSubscriber the veto subscriber to register to the Container EventServer
     * @param eventClasses the classes of event to register for
     */
    public ContainerEventServiceRegistrar(JComponent jComp, VetoEventListener vetoSubscriber, Class... eventClasses) {
       this(jComp, null, vetoSubscriber, eventClasses, null, null, null);
    }

    /**
     * Create a registrar that will keep track of the container event service, and subscribeStrongly the veto subscriber
     * to the topics when the ContainerEventService is available and when it changes.
     *
     * @param jComp the component whose container to monitor
     * @param vetoTopicSubscriber the veto subscriber to register to the Container EventServer
     * @param topics the event topic(s) to register for
     */
    public ContainerEventServiceRegistrar(JComponent jComp, VetoTopicEventListener vetoTopicSubscriber, String... topics) {
       this(jComp, null, null, null, null, vetoTopicSubscriber, topics);
    }

    /**
     * Create a registrar that will keep track of the container event service, and subscribe the subscriber to the topics
     * and the event classes when the ContainerEventService is available and when it changes.
     *
     * @param jComp the component whose container to monitor
     * @param eventSubscriber the subscriber to register to the Container EventServer
     * @param eventClasses the classes of event to register for
     * @param eventTopicSubscriber the topic subscriber to keep registered to the topic(s)
     * @param topics the event topic names to register for
     */
    public ContainerEventServiceRegistrar(JComponent jComp, IEventSubscriber eventSubscriber, Class[] eventClasses,
            IEventTopicSubscriber eventTopicSubscriber, String[] topics) {
        this(jComp, eventSubscriber, null, eventClasses, eventTopicSubscriber, null, topics);
    }

   /**
    * Create a registrar that will keep track of the container event service, and subscribe the subscriber to the topics
    * and the event classes when the ContainerEventService is available and when it changes.
    *
    * @param jComp the component whose container to monitor
    * @param eventSubscriber the subscriber to register to the Container EventServer
    * @param vetoSubscriber a veto subscriber for the eventClasses
    * @param eventClasses the classes of event to register for
    * @param eventTopicSubscriber the topic subscriber to keep registered to the topic(s)
    * @param vetoTopicSubscriber a veto subscriber for the topics
    * @param topics the event topic names to register for
    */
   public ContainerEventServiceRegistrar(JComponent jComp, IEventSubscriber eventSubscriber, VetoEventListener vetoSubscriber,
           Class[] eventClasses, IEventTopicSubscriber eventTopicSubscriber, VetoTopicEventListener vetoTopicSubscriber,
           String[] topics) {
      this.jComp = jComp;
      this.eventSubscriber = eventSubscriber;
      this.vetoSubscriber = vetoSubscriber;
      this.eventClasses = eventClasses;
      this.eventTopicSubscriber = eventTopicSubscriber;
      this.vetoTopicSubscriber = vetoTopicSubscriber;
      this.topics = topics;

      if (jComp == null) {
         throw new NullPointerException("JComponent is null");
      }
      updateContainerEventService();
      jComp.addHierarchyListener(new HierarchyListener() {
         public void hierarchyChanged(HierarchyEvent e) {
            updateContainerEventService();
         }
      });
      jComp.addContainerListener(new ContainerListener() {
         public void componentAdded(ContainerEvent e) {
            updateContainerEventService();
         }

         public void componentRemoved(ContainerEvent e) {
            updateContainerEventService();
         }
      });
      jComp.addAncestorListener(new AncestorListener() {
         public void ancestorAdded(AncestorEvent event) {
            updateContainerEventService();
         }

         public void ancestorMoved(AncestorEvent event) {
             //ignore - not necessary to keep track of movement
         }

         public void ancestorRemoved(AncestorEvent event) {
            updateContainerEventService();
         }
      });
   }

   /**
    * Called by this class when the container may have changed.
    * <p/>
    * Override this method and call super if your class wants to be notified when the container changes (compare the
    * references of getContainerEventService() around the calls to super.updateContainerEventService()).
    */
   protected void updateContainerEventService() {
      if (containerEventService != null) {
         if (eventClasses != null) {
            for (int i = 0; i < eventClasses.length; i++) {
               Class eventClass = eventClasses[i];
               if (eventSubscriber != null) {
                  containerEventService.unsubscribe(eventClass, eventSubscriber);
               }
               if (vetoSubscriber != null) {
                  containerEventService.unsubscribeVeto(eventClass, vetoSubscriber);
               }
            }
         }
         if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
               String topic = topics[i];
               if (eventTopicSubscriber != null) {
                  containerEventService.unsubscribe(topic, eventTopicSubscriber);
               }
               if (vetoTopicSubscriber != null) {
                  containerEventService.unsubscribeVeto(topic, vetoTopicSubscriber);
               }
            }
         }
      }

      containerEventService = ContainerEventServiceFinder.getEventService(jComp);
      if (containerEventService != null) {
         if (eventClasses != null) {
            for (int i = 0; i < eventClasses.length; i++) {
               Class eventClass = eventClasses[i];
                if (eventSubscriber != null) {
                    containerEventService.subscribe(eventClass, eventSubscriber);
                }
                if (vetoSubscriber != null) {
                    containerEventService.subscribeVetoListener(eventClass, vetoSubscriber);
                }
            }
         }
         if (topics != null) {
            for (int i = 0; i < topics.length; i++) {
               String topic = topics[i];
                if (eventTopicSubscriber != null) {
                    containerEventService.subscribe(topic, eventTopicSubscriber);
                }
                if (vetoTopicSubscriber != null) {
                    containerEventService.subscribeVetoListener(topic, vetoTopicSubscriber);
                }
            }
         }
      }
   }

   /**
    * @return the container event service, if null, it tries to find it, but it still may be null if this object is not
    *         in a container.
    */
   public EventService getContainerEventService() {
      if (containerEventService != null) {
         return containerEventService;
      } else {
         updateContainerEventService();
         return containerEventService;
      }
   }
}
