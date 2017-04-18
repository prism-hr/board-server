package hr.prism.board.representation;

import java.util.LinkedHashMap;
import java.util.Objects;

public class ResourceChangeListRepresentation extends LinkedHashMap<String, ResourceChangeListRepresentation.ResourceChangeRepresentation> {
    
    public void put(String property, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            super.put(property, new ResourceChangeRepresentation().setOldValue(oldValue).setNewValue(newValue));
        }
    }
    
    public static class ResourceChangeRepresentation {
        
        private Object oldValue;
        
        private Object newValue;
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public ResourceChangeRepresentation setOldValue(Object oldValue) {
            this.oldValue = oldValue;
            return this;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public ResourceChangeRepresentation setNewValue(Object newValue) {
            this.newValue = newValue;
            return this;
        }
        
    }
    
}
