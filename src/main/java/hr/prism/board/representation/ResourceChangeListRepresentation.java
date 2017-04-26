package hr.prism.board.representation;

import java.util.LinkedHashMap;
import java.util.Objects;

public class ResourceChangeListRepresentation extends LinkedHashMap<String, ResourceChangeListRepresentation.ResourceChangeRepresentation> {
    
    public ResourceChangeListRepresentation put(String property, Object oldValue, Object newValue) {
        super.put(property, new ResourceChangeRepresentation().setOldValue(oldValue).setNewValue(newValue));
        return this;
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
    
        @Override
        public int hashCode() {
            return Objects.hash(oldValue, newValue);
        }
    
        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
        
            ResourceChangeRepresentation that = (ResourceChangeRepresentation) object;
            return Objects.equals(oldValue, that.getOldValue()) && Objects.equals(newValue, that.getNewValue());
        }
        
    }
    
}
