package org.scijava.event.bushe;

import java.util.List;
import java.util.ArrayList;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

/**
 *
 */
public class Issue15Subscriber2 extends JDialog {

    private JTextField textField;
   private long timesCalled;

   /**
     * A new setup has been selected.
     * @param e
     *      setup changed event notification
     */
    @EventSubscriber(eventClass = List.class)
    public void handleEvent(List e) {
         timesCalled++;
        textField.setText(e+"");
    }
    
    private ActionListener buttonListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            EventBus.publish(new ArrayList());
        }
    };
    
    public Issue15Subscriber2() {
        super();
        
        AnnotationProcessor.process(this);
        
        setLayout(new GridLayout(1, 2));
        
        JButton button = new JButton("Push Me");
        button.addActionListener(buttonListener);
        textField = new JTextField("");
        
        add(button);
        add(textField);
    }
    
    public static void main(String args[]) {
        
        Issue15Subscriber s = new Issue15Subscriber();
        
        Issue15Subscriber2 dialog = new Issue15Subscriber2();
        System.err.println(EventBus.getSubscribers(List.class).size());
        dialog.setVisible(true);
    }

   public long getTimesCalled() {
      return timesCalled;
   }
}
